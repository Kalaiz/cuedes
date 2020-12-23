package com.kalai.cuedes.entry

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.leanback.app.OnboardingSupportFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.kalai.cuedes.*
import com.kalai.cuedes.databinding.ViewBackgroundOnboardBinding
import com.kalai.cuedes.databinding.ViewOnboardBinding
import com.kalai.cuedes.entry.OnBoardFragment.PageContent.*
import com.kalai.cuedes.location.LocationFragment.Companion.locationRequestBalanced
import com.kalai.cuedes.location.LocationFragment.Companion.locationRequestHighAccuracy
import kotlinx.coroutines.*


class OnBoardFragment :OnboardingSupportFragment()  {

    /*TODO need to use ViewPager2 instead*/
    private lateinit var contentBinding: ViewOnboardBinding
    private lateinit var backgroundBinding:ViewBackgroundOnboardBinding
    private lateinit var titles:Array<String>
    private lateinit var descriptions:Array<String>
    private lateinit var pageNavigatorView: View
    private lateinit var getStartedButton: View
    private lateinit var googleApiAvailability: GoogleApiAvailability
    private lateinit var locationServiceResolvable: ResolvableApiException
    private lateinit var isLocationServicePermissionGrantedAsync:Deferred<Boolean> // Checks if location needed or not asynchronously

    enum class PageContent{INTRO,PLAY_SERVICE,DEVICE_PERMISSION,GOOGLE_LOCATION_SERVICE_PERMISSION,END}


    companion object{
        private val PAGE = mapOf(INTRO to 0, PLAY_SERVICE to 1,
            DEVICE_PERMISSION to 2, GOOGLE_LOCATION_SERVICE_PERMISSION to 3,END to 4)
        private const val TAG = "OnBoardFragment"
        private val PERMISSION_CODES = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        private const val DEVICE_PERMISSION_REQ_CODE = 1
        private const val GOOGLE_LOCATION_SERVICE_REQ_CODE = 2
    }


    private val deviceSnackBarCallback =  object:BaseTransientBottomBar.BaseCallback<Snackbar>(){
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            userNeverAskAgainIntent()
        }
    }

    override fun getPageCount(): Int = titles.size

    override fun getPageTitle(pageIndex: Int): CharSequence = titles[pageIndex]

    override fun getPageDescription(pageIndex: Int): CharSequence = descriptions[pageIndex]

    override fun onProvideTheme(): Int = R.style.ThemeOverlay_OnBoardFragment


    override fun onCreateBackgroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        if(inflater!=null){
            backgroundBinding =  ViewBackgroundOnboardBinding.inflate(inflater, container, false)
            backgroundBinding.backImageView.setOnClickListener { moveToPreviousPage() }
        }
        return if(this::backgroundBinding.isInitialized) backgroundBinding.root else null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG,"onCreateView")
        titles = inflater.context?.resources?.getStringArray(R.array.onboard_titles)?: arrayOf()
        descriptions = inflater.context?.resources?.getStringArray(R.array.onboard_description) ?: arrayOf()
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup?): View? {
        Log.d(TAG,"onCreateContentView")
        googleApiAvailability = GoogleApiAvailability.getInstance()
        contentBinding =  ViewOnboardBinding.inflate(inflater, container, false).apply {
            devicePermissionButton.setOnClickListener { getDevicePermissions() }
            playServiceRequirementButton.setOnClickListener{ googleApiAvailability.makeGooglePlayServicesAvailable(activity)}
            googleLocationPermissionButton.setOnClickListener {  getGoogleLocationServicesPermission()}
            isLocationServicePermissionGrantedAsync = lifecycleScope.async{isLocationServicePermissionGranted()}
        }
        return if(this::contentBinding.isInitialized) contentBinding.root else null
    }


    override fun onCreateForegroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        Log.d(TAG,"onCreateForegroundView")
        return null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onViewCreated")
        with(view.rootView){
            pageNavigatorView = findViewById<View>(androidx.leanback.R.id.page_indicator)
            getStartedButton =  findViewById<View>(androidx.leanback.R.id.button_start) }
        getStartedButton.setOnClickListener { startMainActivity() }
    }


    private fun startMainActivity(){
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"onResume")
        if(this::contentBinding.isInitialized && this::backgroundBinding.isInitialized) {
            /*After receiving relevant permission; need to refresh page*/
            showPage(currentPageIndex)
        }
    }


    override fun onPageChanged(newPage: Int, previousPage: Int) {
        super.onPageChanged(newPage, previousPage)
        Log.d(TAG, "newpage $newPage currentPage$previousPage " )
        hidePage(previousPage)
        showPage(newPage)
    }


    private fun showPage(pageIndex: Int){
        with(contentBinding) {
            when (pageIndex) {
                PAGE[INTRO] -> {
                    backgroundBinding.backImageView.hide()
                    pageNavigatorView.show()
                }
                PAGE[PLAY_SERVICE] -> {
                    if (googleApiAvailability.isGooglePlayServicesCompatible(context)) {
                        playServiceRequirementButton.hide()
                        okImageView.show()
                        pageNavigatorView.show()
                    }
                    else{
                        pageNavigatorView.hide()
                        playServiceRequirementButton.show()
                    }
                }
                PAGE[DEVICE_PERMISSION] -> {
                    if (context.isDevicePermissionGranted(PERMISSION_CODES)) {
                        devicePermissionButton.hide()
                        okImageView.show()
                        pageNavigatorView.show() }
                    else {
                        devicePermissionButton.show()
                        pageNavigatorView.hide()
                    }
                }
                PAGE[GOOGLE_LOCATION_SERVICE_PERMISSION] -> {
                    lifecycleScope.launch{
                        Log.d(TAG,"Co-routine launch to check permission granted in showPage")
                        if(isLocationServicePermissionGrantedAsync.await()){
                            Log.d(TAG,"Location permission is granted")
                            withContext(Dispatchers.Main){

                                okImageView.show()
                                googleLocationPermissionButton.hide()
                                pageNavigatorView.show()}}
                        else{
                            Log.d(TAG,"Location permission is not granted")
                            withContext(Dispatchers.Main) {
                                googleLocationPermissionButton.show()
                                pageNavigatorView.hide()
                            }
                        }
                    }
                }
                PAGE[END] -> {/*Final Page*/
                    okImageView.show()
                    getStartedButton.hide()
                    lifecycleScope.launch { launch { setOnBoardCompleted() }.join()
                        withContext(Dispatchers.Main){
                            getStartedButton.show()
                        }
                    }


                }
                else -> {}
            }

        }

    }


    private fun getGoogleLocationServicesPermission() {
        if (this::locationServiceResolvable.isInitialized) {
            Log.d(TAG, "getGoogleLocationServicesPermission")
            val contract = object: ActivityResultContract<Unit,Int>(){
                override fun parseResult(resultCode: Int, intent: Intent?):Int{
                    return resultCode
                }

                override fun createIntent(context: Context, input: Unit?): Intent =
                    ActivityResultContracts.StartIntentSenderForResult()
                        .createIntent(context,IntentSenderRequest.Builder(locationServiceResolvable.resolution.intentSender).build())
            }

            registerForActivityResult(contract){
                    requestCode->
                Log.d(TAG,"Callback called and it returned $requestCode")
                if(requestCode==Activity.RESULT_OK){
                    when(requestCode){
                        GOOGLE_LOCATION_SERVICE_REQ_CODE ->{
                            Log.d(TAG,"Location Service Permission Granted")
                            isLocationServicePermissionGrantedAsync = CompletableDeferred(true)
                        }
                    }

                }
            }.launch(null)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG,"OnActivityResult is called")
        if(resultCode==Activity.RESULT_OK){
            when(requestCode){
                GOOGLE_LOCATION_SERVICE_REQ_CODE ->{
                    Log.d(TAG,"Location Service Permission Granted")
                    isLocationServicePermissionGrantedAsync = CompletableDeferred(true)
                }
            }
        }
    }

    private suspend fun isLocationServicePermissionGranted(): Boolean{
        val locationSettingsRequestBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequestHighAccuracy)
            .addLocationRequest(locationRequestBalanced)
            .setAlwaysShow(true)
        val task = activity?.let { LocationServices.getSettingsClient(it)
            .checkLocationSettings(locationSettingsRequestBuilder.build()) }
        val isLocationServicePermissionGranted = CompletableDeferred<Boolean>()
        task?.addOnCompleteListener {
            try{
                it.getResult(ApiException::class.java)
                isLocationServicePermissionGranted.complete(true)
            }
            catch (exception: ApiException){
                locationServiceResolvable = exception as ResolvableApiException
                isLocationServicePermissionGranted.complete(false)
            }
        }
        return isLocationServicePermissionGranted.await()
    }


    private fun hidePage(pageIndex:Int){
        with(contentBinding){
            when(pageIndex){
                PAGE[INTRO]-> backgroundBinding.backImageView.hide()
                PAGE[PLAY_SERVICE] -> { okImageView.hide()
                    playServiceRequirementButton.hide()}
                PAGE[DEVICE_PERMISSION] -> {okImageView.hide()
                    devicePermissionButton.hide()}
                PAGE[GOOGLE_LOCATION_SERVICE_PERMISSION] ->{  okImageView.hide()
                    googleLocationPermissionButton.hide()}
            }
        }
    }


    private fun getDevicePermissions(){
        /*Getting  Relevant Permissions*/
        if(!context.isDevicePermissionGranted(PERMISSION_CODES)){
           val requestPermission =  registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){}
            requestPermission.launch(PERMISSION_CODES)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || (grantResults.any { it == PackageManager.PERMISSION_DENIED })) {
            when(requestCode){
                DEVICE_PERMISSION_REQ_CODE ->  activity?.findViewById<View>(android.R.id.content)?.let {
                    val snackBar = Snackbar.make(it, getString(R.string.onboard_permission_msg), Snackbar.LENGTH_SHORT)
                    if(!PERMISSION_CODES.fold(true,{acc,permissionCode-> acc && shouldShowRequestPermissionRationale(permissionCode)})) {
                        snackBar.addCallback(deviceSnackBarCallback)
                        snackBar.show()
                    }
                }
            }
        }
    }

    private suspend fun setOnBoardCompleted(){
        val dataStore = context?.createDataStore("settings")
        val isOnBoardingKey = preferencesKey<Boolean>("isOnBoard")
        dataStore?.edit {
                settings->
            settings[isOnBoardingKey]= false

        }
    }


    private fun userNeverAskAgainIntent(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri= Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        /*TODO need to use the new new Activity Result API*/
        startActivityForResult(intent, DEVICE_PERMISSION_REQ_CODE)
    }

}


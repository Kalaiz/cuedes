package com.kalai.cuedes.entry.onboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.kalai.cuedes.databinding.FragmentOnboardLocationServicePermissionBinding
import com.kalai.cuedes.entry.onboard.PageContent.*
import com.kalai.cuedes.hide
import com.kalai.cuedes.location.LocationFragment
import com.kalai.cuedes.show
import kotlinx.coroutines.*


class LocationServicePermissionFragment : Fragment() {

    companion object{ private const val TAG = "LocationServiceFragment" }

    private val viewModel: OnBoardViewModel by activityViewModels()
    private lateinit var locationServiceResolvable: ResolvableApiException
    private lateinit var isLocationServicePermissionGrantedAsync: Deferred<Boolean> // Checks if location needed or not asynchronously
    private lateinit var  requestLocationServicePermissionLauncher: ActivityResultLauncher<Unit>
    private lateinit var binding:FragmentOnboardLocationServicePermissionBinding
    private lateinit var  job: Job
    private var  isLocationServicePermissionGranted =false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding  = FragmentOnboardLocationServicePermissionBinding.inflate(inflater,container,false)
        binding.googleLocationPermissionButton.setOnClickListener { getGoogleLocationServicesPermission() }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLocationServicePermissionGrantedAsync = lifecycleScope.async{
            Log.d(TAG,"Async Launched")
            isLocationServicePermissionGranted()}
        isLocationServicePermissionGrantedAsync.invokeOnCompletion {
            lifecycleScope.launch{
                Log.d(TAG,"Completed")
                isLocationServicePermissionGranted = isLocationServicePermissionGrantedAsync.await()
                if(isLocationServicePermissionGrantedAsync.await()){
                    locationPermissionGranted()
                }
                else{
                    locationPermissionNotGranted()
                }
            }
        }

        val resultContract = object: ActivityResultContract<Unit, Int>(){
            override fun parseResult(resultCode: Int, intent: Intent?):Int{
                return resultCode
            }

            override fun createIntent(context: Context, input: Unit?): Intent =
                ActivityResultContracts.StartIntentSenderForResult()
                    .createIntent(context,
                        IntentSenderRequest.Builder(locationServiceResolvable.resolution.intentSender).build())
        }

        requestLocationServicePermissionLauncher =  registerForActivityResult(resultContract){
                requestCode->
            Log.d(TAG,"Callback called and it returned $requestCode")
            if(requestCode== Activity.RESULT_OK){
                job.cancel()
                Log.d(TAG,"Location Service Permission Granted")
                locationPermissionGranted()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG,"OnResume")
        permissionUpdate()
    }

    private fun locationPermissionGranted(){
        binding.okImageView.show()
        binding.googleLocationPermissionButton.hide()
        if(viewModel.isPageNavigationViewable.value?.get(GOOGLE_LOCATION_SERVICE_PERMISSION) != true)
            viewModel.updateIsPageNavigationViewable(GOOGLE_LOCATION_SERVICE_PERMISSION,true)
    }

    private fun locationPermissionNotGranted(){
        binding.googleLocationPermissionButton.show()
        if (viewModel.isPageNavigationViewable.value?.get(
                GOOGLE_LOCATION_SERVICE_PERMISSION
            ) != false)
            viewModel.updateIsPageNavigationViewable(
                GOOGLE_LOCATION_SERVICE_PERMISSION,
                false
            )
    }


    private fun permissionUpdate() {
        if(viewModel.isPageNavigationViewable.value?.get(GOOGLE_LOCATION_SERVICE_PERMISSION) == true|| isLocationServicePermissionGranted){
            locationPermissionGranted()
        }
        else if(!isLocationServicePermissionGranted && this::job.isInitialized&&!job.isActive || !this::job.isInitialized )

            job =  lifecycleScope.launch {
                Log.d(TAG, "Co-routine launch to check permission granted in showPage")
                if (isLocationServicePermissionGrantedAsync.await()) {
                    Log.d(TAG, "Location permission is granted")
                    withContext(Dispatchers.Main) {
                        locationPermissionGranted()
                    }
                } else {
                    Log.d(TAG, "Location permission is not granted")
                    withContext(Dispatchers.Main) {
                        locationPermissionNotGranted()
                    }

                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG,"OnDestryView called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"OnDestroy")
    }

    private fun getGoogleLocationServicesPermission() {
        if (this::locationServiceResolvable.isInitialized) {
            Log.d(TAG, "getGoogleLocationServicesPermission")
            requestLocationServicePermissionLauncher.launch(null)
        }
    }


    private suspend fun isLocationServicePermissionGranted(): Boolean{
        val locationSettingsRequestBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(LocationFragment.locationRequestHighAccuracy)
            .addLocationRequest(LocationFragment.locationRequestBalanced)
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

}
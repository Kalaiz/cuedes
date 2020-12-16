package com.kalai.cuedes.entry

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.OnboardingSupportFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.kalai.cuedes.*
import com.kalai.cuedes.databinding.ViewBackgroundOnboardBinding
import com.kalai.cuedes.databinding.ViewOnboardBinding


class OnBoardFragment :OnboardingSupportFragment()  {

    private lateinit var contentBinding: ViewOnboardBinding
    private lateinit var backgroundBinding:ViewBackgroundOnboardBinding
    private lateinit var titles:Array<String>
    private lateinit var descriptions:Array<String>
    private lateinit var pageNavigatorView: View
    private lateinit var getStartedButton: View
    private lateinit var googleApiAvailability: GoogleApiAvailability



    companion object{
        private val PERMISSION_CODES = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        private const val REQ_CODE = 1
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
        titles = inflater.context?.resources?.getStringArray(R.array.onboard_titles)?: arrayOf()
        descriptions = inflater.context?.resources?.getStringArray(R.array.onboard_description) ?: arrayOf()
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onCreateContentView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        if(inflater!=null){
            googleApiAvailability = GoogleApiAvailability.getInstance()
            contentBinding =  ViewOnboardBinding.inflate(inflater, container, false)
            contentBinding.getDeviceLocationPermissionButton.setOnClickListener { getDevicePermission() }
            contentBinding.getServiceRequirement.setOnClickListener{ googleApiAvailability.makeGooglePlayServicesAvailable(activity)}
        }
        return if(this::contentBinding.isInitialized) contentBinding.root else null
    }

    override fun onCreateForegroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        return null
    }


    override fun onResume() {
        super.onResume()
        if(!this::pageNavigatorView.isInitialized) {
            val pageNavigatorView =
                view?.rootView?.findViewById<View>(androidx.leanback.R.id.page_indicator)
            if (pageNavigatorView != null) {
                this.pageNavigatorView = pageNavigatorView
            }
            val getStartedButton =  view?.rootView?.findViewById<View>(androidx.leanback.R.id.button_start)
            if(getStartedButton!=null){
                this.getStartedButton=getStartedButton
                getStartedButton.setOnClickListener {
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            }
        }
        else {
            with(contentBinding){
                if (context.isDevicePermissionGranted(PERMISSION_CODES)) {
                    doneImageView.show()
                    getDeviceLocationPermissionButton.hide()
                    pageNavigatorView.show()
                }
                if (googleApiAvailability.isGooglePlayServicesCompatible(context)) {
                    doneImageView.show()
                    getServiceRequirement.hide()
                }
            }
        }
    }

    override fun onPageChanged(newPage: Int, previousPage: Int) {
        super.onPageChanged(newPage, previousPage)
        val isPageIncrement = previousPage < newPage

        if (isPageIncrement) {
            backgroundBinding.backImageView.show()
        }
        when (newPage) {
            0 -> {/*Intro Page*/
                backgroundBinding.backImageView.hide()
                pageNavigatorView.show()
            }
            1 -> { /*Device Location Page*/
                with(contentBinding) {
                    if (context?.isDevicePermissionGranted(PERMISSION_CODES) != true) {
                        getDeviceLocationPermissionButton.show()
                        pageNavigatorView.hide()
                    } else {
                        getDeviceLocationPermissionButton.hide()
                        doneImageView.show()
                    }
                }
            }

        }
    }



    private fun showPage(pageIndex: Int){
        with(contentBinding) {
            when (pageIndex) {
                0 -> {
                    backgroundBinding.backImageView.hide()
                    pageNavigatorView.show()
                }
                1 -> {
                    if (context?.isDevicePermissionGranted(PERMISSION_CODES) != true) {
                        getDeviceLocationPermissionButton.show()
                        pageNavigatorView.hide()
                    } else {
                        getDeviceLocationPermissionButton.hide()
                        doneImageView.show()
                    }
                }
                2 -> {
                    with(googleApiAvailability) {
                        if (isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
                            getServiceRequirement.show()
                            doneImageView.show()
                            pageNavigatorView.hide()
                        }
                    }

                }

            }
        }

    }




    private fun hidePage(pageIndex:Int){
        with(contentBinding){
            when(pageIndex){
                0 -> backgroundBinding.backImageView.hide()
                1 -> {doneImageView.hide()
                    getDeviceLocationPermissionButton.hide()}
                2 -> { doneImageView.hide()
                    getServiceRequirement.hide()}
            }
        }
    }




    private fun getDevicePermission(){
        /*Getting  Relevant Permissions*/
        if(context?.isDevicePermissionGranted(PERMISSION_CODES) != true){
            requestPermissions(PERMISSION_CODES, REQ_CODE)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            REQ_CODE -> if (grantResults.isEmpty() || (grantResults.any { it == PackageManager.PERMISSION_DENIED })) {
                activity?.findViewById<View>(android.R.id.content)?.let {
                    val snackBar = Snackbar.make(it, getString(R.string.onboard_permission_msg), Snackbar.LENGTH_SHORT)
                    if(!PERMISSION_CODES.fold(true,{acc,permissionCode-> acc && shouldShowRequestPermissionRationale(permissionCode)})) {
                        snackBar.addCallback(deviceSnackBarCallback)
                        snackBar.show()
                    }
                }
            }
            else{
                pageNavigatorView.show()
            }

        }
    }



    private fun userNeverAskAgainIntent(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri= Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        startActivityForResult(intent, REQ_CODE)
    }


}


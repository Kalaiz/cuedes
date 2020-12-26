package com.kalai.cuedes.entry.onboard

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentOnboardDevicePermissionBinding
import com.kalai.cuedes.entry.onboard.PageContent.*
import com.kalai.cuedes.hide
import com.kalai.cuedes.isDevicePermissionGranted
import com.kalai.cuedes.show


class DevicePermissionFragment : Fragment() {

    companion object{
        private val PERMISSION_CODES = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private lateinit var binding:FragmentOnboardDevicePermissionBinding
    private lateinit var  requestPermission: ActivityResultLauncher<Array<(String)>>
    private val viewModel: OnBoardViewModel by activityViewModels()


    private val deviceSnackBarCallback =  object: BaseTransientBottomBar.BaseCallback<Snackbar>(){
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            userNeverAskAgainIntent()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnboardDevicePermissionBinding.inflate(inflater,container,false)
        binding.devicePermissionButton.setOnClickListener { getDevicePermissions() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionUpdate()
    }

    override fun onResume() {
        super.onResume()
        permissionUpdate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission =  registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                activityResult->
            if(activityResult.values.any{it==false}){
                activity?.findViewById<View>(android.R.id.content)?.let {
                    val snackBar = Snackbar.make(it, getString(R.string.onboard_permission_msg), Snackbar.LENGTH_SHORT)
                    if(!PERMISSION_CODES.fold(true,{ acc, permissionCode-> acc && shouldShowRequestPermissionRationale(permissionCode)})) {
                        snackBar.addCallback(deviceSnackBarCallback)
                        snackBar.show()
                    }
                }
            }
        }


    }

    private fun permissionUpdate(){

        if (context.isDevicePermissionGranted(PERMISSION_CODES)) {
            binding.devicePermissionButton.hide()
            binding.okImageView.show()
            if(viewModel.getIsPageNavigationViewable().value?.get(DEVICE_PERMISSION) != true)
                viewModel.updateIsPageNavigationViewable(DEVICE_PERMISSION,true)
        }
        else {
            binding.devicePermissionButton.show()
            if(viewModel.getIsPageNavigationViewable().value?.get(DEVICE_PERMISSION) != false)
                viewModel.updateIsPageNavigationViewable(DEVICE_PERMISSION,false)

        }

    }

    private fun getDevicePermissions(){
        /*Getting  Relevant Permissions*/
        if(!context.isDevicePermissionGranted(PERMISSION_CODES)){
            requestPermission.launch(PERMISSION_CODES)
        }
    }

    private fun userNeverAskAgainIntent(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri= Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

}
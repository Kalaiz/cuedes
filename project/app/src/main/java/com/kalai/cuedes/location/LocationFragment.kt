package com.kalai.cuedes.location

import android.annotation.SuppressLint
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.kalai.cuedes.R
import com.kalai.cuedes.SharedViewModel
import com.kalai.cuedes.databinding.FragmentLocationBinding
import com.kalai.cuedes.location.selection.SelectionFragment


@SuppressLint("MissingPermission")
class LocationFragment : Fragment() ,OnMapReadyCallback{

    companion object {
        fun newInstance() = LocationFragment()
        private const val TAG = "LocationFragment"
    }

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModel: LocationViewModel by viewModels()


    private lateinit var map: SupportMapFragment
    private lateinit var binding:FragmentLocationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var currentLocation:LatLng
    private var fusedLocationClientInitiated = false
    private lateinit  var fusedLocationClient:FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLocationBinding.inflate(layoutInflater, container, false)
        map = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        map.getMapAsync(this)

        /* Due to Default Location Button being displaced*/
        binding.currentLocationButton.setOnClickListener {
            setCurrentLocation(animated = true)
        }

        activity?.let {
            fusedLocationClient= LocationServices.getFusedLocationProviderClient(it) }


        return binding.root
    }

    override fun onResume() {
        super.onResume()





    }

    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d(TAG,"onMapReady called")
        if (googleMap != null) {
            this.googleMap = googleMap
        }

        googleMap?.isMyLocationEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled=false
        setCurrentLocation(animated = false)

        googleMap?.setOnMapLongClickListener {
                latLng -> Log.d(TAG,latLng.toString());
            googleMap.addMarker(MarkerOptions().position(latLng))

            val selectLocation = SelectionFragment()
            selectLocation.isCancelable = true
            childFragmentManager.commit {
                setReorderingAllowed(true)
                add(selectLocation,"SelectLocation")
            }

        }
    }


    private fun setCurrentLocation(animated:Boolean){
        Log.d(TAG,"setCurrentLocation  called")
        fusedLocationClient.lastLocation.addOnSuccessListener {
            it?.apply {
                currentLocation = LatLng(latitude,longitude)
            }
            if(it==null){
                Log.d(TAG,"LastLocation null")
            }

            if (this::currentLocation.isInitialized && this::googleMap.isInitialized) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 11.0f)
                if (animated) {
                    googleMap.animateCamera(cameraUpdate)
                } else {
                    googleMap.moveCamera(cameraUpdate)
                }

            }
        }
    }


}
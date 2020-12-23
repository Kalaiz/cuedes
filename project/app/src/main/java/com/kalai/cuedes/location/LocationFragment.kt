package com.kalai.cuedes.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentLocationBinding
import com.kalai.cuedes.location.selection.SelectionFragment


@SuppressLint("MissingPermission")
class LocationFragment : Fragment() ,OnMapReadyCallback{

    companion object {
        private const val TAG = "LocationFragment"
        val locationRequestHighAccuracy = LocationRequest().apply {  priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval=5000}
        val locationRequestBalanced = LocationRequest().apply { fastestInterval = 60*1000
            interval=60*1000*60}
    }


    private val locationViewModel: LocationViewModel by activityViewModels()

    private lateinit var geoFencingClient: GeofencingClient
    private lateinit var map: SupportMapFragment
    private lateinit var binding:FragmentLocationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationClient:FusedLocationProviderClient
    private lateinit var currentSelectedMarker: Marker


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLocationBinding.inflate(layoutInflater, container, false)
        map = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        map.getMapAsync(this)

        /* Due to Default Location Button being displaced*/
        binding.currentLocationButton.setOnClickListener {
            setCurrentLocation(animated = true)
        }

        activity?.let {
            fusedLocationClient= LocationServices.getFusedLocationProviderClient(it) }

       activity?.let {  geoFencingClient = LocationServices.getGeofencingClient(it) }

        return binding.root
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d(TAG,"onMapReady called")
        if (googleMap != null) {
            this.googleMap = googleMap
            requestLocation()
        }

        googleMap?.isMyLocationEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled=false

        /*Overriding default behaviour*/
        googleMap?.setOnMarkerClickListener { true }

        googleMap?.setOnMapLongClickListener {
                latLng -> Log.d(TAG,latLng.toString())
          currentSelectedMarker = googleMap.addMarker(MarkerOptions().position(latLng))
            if(latLng!=null) {
                val selectLocation = SelectionFragment(latLng)
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(selectLocation, "SelectLocation")
                }
            }

        }

        locationViewModel.selectedLocation.observe(requireActivity()) {
            latLng -> if(latLng == null){ currentSelectedMarker.remove()}
        }
    }


    private fun requestLocation(){
        Log.d(TAG,"requesting Location")
        val locationRequest = LocationRequest().apply { priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000 }

        val locationCallback =  object:LocationCallback(){
            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                Log.d(TAG,"Location available? ${locationAvailability?.isLocationAvailable}")
                fusedLocationClient.lastLocation.addOnSuccessListener {location ->
                    Log.d(TAG,"Success")
                    location?.let {
                        currentLocation = it
                        Log.d(TAG,"Current Location is  $it")
                        setCurrentLocation(false)
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

        }
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }

    private fun setCurrentLocation(animated:Boolean){
        Log.d(TAG,"setCurrentLocation  called")

            if (this::currentLocation.isInitialized && this::googleMap.isInitialized) {
                val latLng = LatLng(currentLocation.latitude,currentLocation.longitude)
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 11.0f)
                if (animated) {
                    googleMap.animateCamera(cameraUpdate)
                } else {
                    googleMap.moveCamera(cameraUpdate)
                }

            }
        }
    }


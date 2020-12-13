package com.kalai.cuedes.location

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kalai.cuedes.R
import com.kalai.cuedes.SharedViewModel
import com.kalai.cuedes.databinding.FragmentLocationBinding
import com.kalai.cuedes.location.selectlocation.BottomSheetFragment


private const val TAG = "LocationFragment"

class LocationFragment : Fragment() ,OnMapReadyCallback{

    companion object {
        fun newInstance() = LocationFragment()
    }


    private val viewModel: LocationViewModel by viewModels()
    private lateinit var map: SupportMapFragment
    private lateinit var binding:FragmentLocationBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var googleMap: GoogleMap
    private lateinit var  currentLocation:LatLng

    private val fusedLocationClient: FusedLocationProviderClient by lazy{
        LocationServices.getFusedLocationProviderClient(this.activity as Activity)
    }


    @SuppressLint("MissingPermission")
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

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel

    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d(TAG,"Map Ready")

        if (googleMap != null) {
            this.googleMap = googleMap
        }

        googleMap?.isMyLocationEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled=false
        setCurrentLocation(animated = false)

        googleMap?.setOnMapLongClickListener {
                latLng -> Log.d(TAG,latLng.toString());
            googleMap.addMarker(MarkerOptions().position(latLng))

            val selectLocation = BottomSheetFragment()
            activity?.supportFragmentManager?.let {selectLocation.show(it,"SelectLocation")}

        }

    }

    @SuppressLint("MissingPermission")
    fun setCurrentLocation(animated:Boolean){
        fusedLocationClient.lastLocation.addOnCompleteListener {
            it.result.apply { currentLocation = LatLng(latitude,longitude) }
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation,11.0f)
            if (this::googleMap.isInitialized)
                if(animated)
                {googleMap.animateCamera(cameraUpdate)}
                else
                {googleMap.moveCamera(cameraUpdate)}
        }
    }





}
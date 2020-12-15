package com.kalai.cuedes.location

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
    private val fusedLocationClient: FusedLocationProviderClient by lazy{
        LocationServices.getFusedLocationProviderClient(this.activity as Activity)
    }



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

            val selectLocation = SelectionFragment()
            selectLocation.isCancelable = true
            childFragmentManager.commit {
                setReorderingAllowed(true)
                add(selectLocation,"SelectLocation")
            }

        }

    }


    private fun setCurrentLocation(animated:Boolean){
        startLocationUpdates()
        fusedLocationClient.lastLocation.addOnSuccessListener {
            it?.apply {
                currentLocation = LatLng(latitude,longitude)
            }

            if (this::currentLocation.isInitialized && this::googleMap.isInitialized) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 11.0f)
                if (animated) {
                    googleMap.animateCamera(cameraUpdate)
                } else {
                    googleMap.moveCamera(cameraUpdate)
                }

            }
            else{

            }
        }
    }


    private fun startLocationUpdates() {
        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval=10000
            fastestInterval=1000
        }

        val locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                fusedLocationClientInitiated = locationResult!=null
            }

        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }
    override fun onResume() {
        super.onResume()

    }


}
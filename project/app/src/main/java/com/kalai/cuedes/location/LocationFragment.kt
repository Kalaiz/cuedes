package com.kalai.cuedes.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentLocationBinding
import com.kalai.cuedes.location.selection.SelectionBottomFragment


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
    private lateinit var map: SupportMapFragment
    private lateinit var binding:FragmentLocationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var currentSelectedMarker: Marker


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLocationBinding.inflate(layoutInflater, container, false)
        map = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        map.getMapAsync(this)

/*       activity?.let {  geoFencingClient = LocationServices.getGeofencingClient(it) }*/

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* Due to Default Location Button being displaced*/
        binding.currentLocationButton.setOnClickListener {
            locationViewModel.getCurrentLocationUpdate()
        }

        locationViewModel.cameraMovement.observe(viewLifecycleOwner, Observer {cameraMovement->
            Log.d(TAG,"Camera Movement changed")
            if (this::googleMap.isInitialized) {
                cameraMovement?.run{
                    if(animated && duration!=null){
                        Log.d(TAG,"Camera Movement with animation and duration")
                        googleMap.animateCamera(cameraUpdate,duration as Int,null)
                    }
                    else if (animated){
                        Log.d(TAG,"Camera Movement with animation ")
                        googleMap.animateCamera(cameraUpdate)
                    }
                    else{
                        Log.d(TAG,"Camera Movement ")
                        googleMap.moveCamera(cameraUpdate)
                    }
                }
            }
        })

        locationViewModel.isCameraIdle.observe(viewLifecycleOwner,Observer{
            Log.d(TAG,"TEST: CameraIdle changed")
        })

        locationViewModel.selectedLatLng.observe(viewLifecycleOwner, Observer { latLng->
            if(latLng != null) {
                Log.d(TAG,"SelectedLatLng Changed")
                /*cannot use Lambda cause of Kotlin SAM not giving a "this" reference to the observer instance*/
                locationViewModel.isCameraIdle.observe(viewLifecycleOwner,object:Observer<Boolean> {
                    override fun onChanged(isCameraIdle: Boolean?) {
                        Log.d(TAG,"cameraIdle Changed")
                        if (isCameraIdle == true) {
                            val selectLocation = SelectionBottomFragment(latLng)
                            childFragmentManager.commit {
                                setReorderingAllowed(true)
                                add(selectLocation, "SelectLocation")
                            }
                            locationViewModel.isCameraIdle.removeObserver(this)
                        }
                    }
                })
            }
        })

        locationViewModel.currentLocation.observe(viewLifecycleOwner, object :
            Observer< Location> {
            override fun onChanged(location: Location?) {
                if(location!=null){
                    locationViewModel.setupCurrentLocation()
                    locationViewModel.currentLocation.removeObserver(this)
                }
            }
        })

        setFragmentResultListener("LocationFragmentReqKey") { _, bundle ->
            if(!bundle.getBoolean("Successful")){
                currentSelectedMarker.remove()
            }

        }

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"onResume")
        if(childFragmentManager.fragments.isEmpty()){
            setNormalMode()
        }
   }

    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d(TAG,"onMapReady called")
        if (googleMap != null) {
            this.googleMap = googleMap
            locationViewModel.requestLocation()
            googleMap.setOnCameraIdleListener {
                Log.d(TAG,"Camera Idle")
                locationViewModel.cameraIdle()
            }

            googleMap.setOnCameraMoveStartedListener {
                Log.d(TAG,"camera move started $it")
                locationViewModel.setCameraMoveAction(it)
                locationViewModel.cameraMoving()

            }
        }

        googleMap?.isMyLocationEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled=false

        /*Overriding default behaviour*/
        googleMap?.setOnMarkerClickListener { true }

        googleMap?.setOnMapLongClickListener {
                latLng -> Log.d(TAG,latLng.toString())
            currentSelectedMarker = googleMap.addMarker(MarkerOptions().position(latLng))
            if(latLng!=null) {
                setSelectionMode(latLng)
            }
            val callback  = object:
                FragmentManager.FragmentLifecycleCallbacks(){
                override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                    super.onFragmentDetached(fm, f)
                    setNormalMode()
                }
            }
            childFragmentManager.registerFragmentLifecycleCallbacks(callback,false)
        }
    }


    private fun setSelectionMode(latLng: LatLng){
        locationViewModel.setSelectionMode(latLng)
        binding.searchView
            .animate()?.translationYBy(-binding.searchView.height*2f)?.duration = 1000

    }

    private fun setNormalMode(){
        binding.searchView
            .animate()?.translationYBy(binding.searchView.height*2f)?.duration = 1000
    }

}


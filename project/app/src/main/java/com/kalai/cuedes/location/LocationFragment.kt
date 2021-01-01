package com.kalai.cuedes.location

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentLocationBinding
import com.kalai.cuedes.location.Status.*
import com.kalai.cuedes.location.selection.SelectionFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("MissingPermission")
class LocationFragment : Fragment() ,OnMapReadyCallback, OnMapLoadedCallback{

    companion object {
        private const val TAG = "LocationFragment"
        val locationRequestHighAccuracy = LocationRequest().apply {  priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval=5000}
        val locationRequestBalanced = LocationRequest().apply { fastestInterval = 60*1000
            interval=60*1000*60}
    }

    private lateinit var  mapLogoValueAnimator: ValueAnimator
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var map: SupportMapFragment
    private lateinit var binding:FragmentLocationBinding
    private lateinit var googleMap: GoogleMap
    private  var currentSelectedMarker: Marker? = null
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLocationBinding.inflate(layoutInflater, container, false)
        map = childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
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

        locationViewModel.cameraMovement.observe(viewLifecycleOwner, Observer { cameraMovement ->
            Log.d(TAG, "Camera Movement changed")
            if (this::googleMap.isInitialized) {
                cameraMovement?.run {
                    if (animated && duration != null) {
                        Log.d(TAG, "Camera Movement with animation and duration")
                        googleMap.animateCamera(cameraUpdate, duration as Int, null)
                    } else if (animated) {
                        Log.d(TAG, "Camera Movement with animation ")
                        googleMap.animateCamera(cameraUpdate)
                    } else {
                        Log.d(TAG, "Camera Movement ")
                        googleMap.moveCamera(cameraUpdate)
                    }
                }
            }
        })

        locationViewModel.currentLocation.observe(viewLifecycleOwner, object :
            Observer<Location> {
            override fun onChanged(location: Location?) {
                if (location != null) {
                    locationViewModel.setupCurrentLocation()
                    locationViewModel.currentLocation.removeObserver(this)
                }
            }
        })

        setFragmentResultListener("LocationFragmentReqKey") { _, bundle ->
            if (!bundle.getBoolean("Successful")) {
                currentSelectedMarker?.remove()
            }
            locationViewModel.updateStatus(NORMAL)
        }

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText.isNullOrBlank()){
                    binding.currentLocationButton.animate()?.alpha(1f)?.setDuration(1000)?.start()
                }
                else if(binding.currentLocationButton.alpha==1f){
                    binding.currentLocationButton.animate()?.alpha(0f)?.setDuration(1000)?.start()
                }
                return true}
        })

        locationViewModel.status.observe(viewLifecycleOwner,
            Observer<Status> { updatedStatus-> when(updatedStatus){
                SELECTION -> locationViewModel.selectedLatLng.value?.let {  setSelectionMode() }
                else -> { setNormalMode()  }
            } })

        locationViewModel.selectedMarker.observe(viewLifecycleOwner,Observer {
                marker ->
                if(marker == null){
                    Log.d(TAG,"Marker null")
                    currentSelectedMarker?.remove() }
            Log.d(TAG,"Marker changed $marker")
            currentSelectedMarker = marker

        })
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"onResume")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(locationViewModel.status.value == NORMAL){
                    Log.d(TAG,"Normal Status")
                    onBackPressedCallback.remove()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback
        )
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d(TAG,"onMapReady called")
        if (googleMap != null) {
            this.googleMap = googleMap
            locationViewModel.mapReady()
            locationViewModel.requestLocation()

            googleMap.setOnCameraIdleListener {
                Log.d(TAG,"Camera Idle")
                locationViewModel.cameraIdle()
                googleMap.setOnMapLoadedCallback(this);
            }


            googleMap.setOnCameraMoveStartedListener {
                Log.d(TAG,"camera move started $it")
                locationViewModel.setCameraMoveAction(it)
                locationViewModel.cameraMoving()
            }
        }

        googleMap?.isMyLocationEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false

        /*Overriding default behaviour*/
        googleMap?.setOnMarkerClickListener { true }

        googleMap?.setOnMapLongClickListener {
                latLng -> Log.d(TAG,latLng.toString())

            if(latLng!=null) {
                locationViewModel.setSelectedLocation(googleMap.addMarker(MarkerOptions().position(latLng)))
                locationViewModel.updateStatus(SELECTION)
            }
        }

        /* val paddingHeightMultiplier = binding.motionLayoutContainer.constraintSetIds*/
        val padding = context?.resources?.displayMetrics?.heightPixels?.times(0.0725f)
        /*Log.d(TAG,"Padding is $padding")*/
        if (padding != null) {
            mapLogoValueAnimator = ValueAnimator.ofFloat(0f, padding).apply {
                duration = binding.motionLayoutContainer.transitionTimeMs
            }
            mapLogoValueAnimator.addUpdateListener { valueAnimator -> valueAnimator?.animatedValue.let {value->
                /*  Log.d(TAG,"Padding changed +$it")*/
                googleMap?.setPadding(0,0,0, (value as Float).toInt()) } }
        }
    }


    private fun setSelectionMode(){

        binding.searchView.clearFocus()
        childFragmentManager.commit {
            add(R.id.fragment_selection, SelectionFragment()) }

        binding.motionLayoutContainer.transitionToEnd()
        binding.root.transitionToEnd()
        mapLogoValueAnimator.start()


        binding.motionLayoutContainer.addTransitionListener(object:MotionLayout.TransitionListener{
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}
            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                locationViewModel.setSelectionMode()
                binding.motionLayoutContainer.removeTransitionListener(this)
            }
        })


        /*TODO launch fragment lazily via viewstub*/
    }

    private fun setNormalMode(){
        binding.root.transitionToStart()
        binding.motionLayoutContainer.transitionToStart()
        mapLogoValueAnimator.reverse()
    }

    override fun onMapLoaded() {
        Log.d(TAG,"Map loaded")
        locationViewModel.mapLoaded()
    }

}



package com.kalai.cuedes.location

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentLocationBinding
import com.kalai.cuedes.location.Status.NORMAL
import com.kalai.cuedes.location.Status.SELECTION
import com.kalai.cuedes.location.selection.SelectionFragment
import kotlinx.coroutines.Job


@SuppressLint("MissingPermission")
class LocationFragment : Fragment() ,OnMapReadyCallback, OnMapLoadedCallback{

    companion object {
        private const val TAG = "LocationFragment"
        val locationRequestHighAccuracy = LocationRequest().apply {  priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval=5000}
        val locationRequestBalanced = LocationRequest().apply { fastestInterval = 60*1000
            interval=60*1000*60}
    }


    private lateinit var job : Job
    private lateinit var  mapLogoValueAnimator: ValueAnimator
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var map: SupportMapFragment
    private lateinit var binding:FragmentLocationBinding
    private lateinit var googleMap: GoogleMap
    private  var currentSelectedMarker: Marker? = null
    private  var currentSelectedRadius: Circle? = null
    private lateinit var onBackPressedCallback: OnBackPressedCallback


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        job = Job()
        binding = FragmentLocationBinding.inflate(layoutInflater, container, false)
        map = childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        map.getMapAsync(this)

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
            locationViewModel.updateStatus(NORMAL)
            if (!bundle.getBoolean("Successful")) {
                locationViewModel.setSelectedLocation(null) }

        }

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean { return true }
            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText.isNullOrBlank()){
                    binding.currentLocationButton.animate()?.alpha(1f)?.setDuration(1000)?.start()
                }
                else if(binding.currentLocationButton.alpha == 1f){
                    binding.currentLocationButton.animate()?.alpha(0f)?.setDuration(1000)?.start()
                }
                return true
            }
        })

        locationViewModel.status.observe(viewLifecycleOwner,
            Observer<Status> { updatedStatus-> when(updatedStatus){
                SELECTION -> locationViewModel.selectedLatLng.value?.let {  setSelectionMode() }
                else -> { setNormalMode()  }
            } })

        locationViewModel.selectedMarker.observe(viewLifecycleOwner,Observer {
                marker ->
            if(marker == null){
                Log.d(TAG,"Observed Selected Marker changed to null")
                removeMarkerRadius()
            }
            else{
                Log.d(TAG,"Observed Selected Marker changed $marker")
                setupMarkerRadius(marker)
            }
        })

        locationViewModel.selectedRadius.observe(viewLifecycleOwner, Observer {
            updatedRadius ->
            currentSelectedRadius?.let {circle->
            getAnimationRadius(circle, circle.radius.toFloat(),updatedRadius).start()
         /*TODO*/
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"onResume")
        onBackPressedCallback.isEnabled = true
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
            onBackPressedCallback)
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



    override fun onPause() {
        super.onPause()
        Log.d(TAG,"OnPause")
        onBackPressedCallback.isEnabled = false
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

    }

    private fun setNormalMode(){
        binding.root.transitionToStart()
        binding.motionLayoutContainer.transitionToStart()
        if(this::mapLogoValueAnimator.isInitialized) {
            mapLogoValueAnimator.reverse()
        }
    }

    override fun onMapLoaded() {
        Log.d(TAG,"Map loaded")
        locationViewModel.mapLoaded()
    }

    private fun removeMarkerRadius(){
        Log.d(TAG,"removeMarkerRadius called")
        val currentSelectedRadius = currentSelectedRadius
        val currentSelectedMarker = currentSelectedMarker
        currentSelectedRadius?.let { circle ->
            Log.d(TAG, "radiusAnimation ")
            val radiusAnimation = getAnimationRadius(circle, circle.radius.toFloat(),0f).apply {
                startDelay=  if (locationViewModel.status.value == NORMAL) binding.root.transitionTimeMs else 0
                start()
            }
            radiusAnimation.doOnEnd {
                Log.d(TAG,"Removing Marker and Radius")
                currentSelectedMarker?.remove()
                currentSelectedRadius.remove()
            }
        }
    }



    private fun setupMarkerRadius(marker:Marker){
        if (marker.position != null) {
            Log.d(TAG,"marker position not null")
            currentSelectedMarker = marker
            currentSelectedRadius = googleMap.addCircle(CircleOptions().center(marker.position))
            currentSelectedRadius?.strokeColor = Color.TRANSPARENT
            context?.getColor(R.color.map_radius)?.let { currentSelectedRadius?.fillColor = it }
            val zoom = googleMap.cameraPosition?.zoom ?: 1f
            Log.d(TAG, "Zoom $zoom")
            val radiusAnimator = getAnimationRadius(currentSelectedRadius as Circle,0f,zoom*500)
            radiusAnimator.apply {
                startDelay = if(locationViewModel.status.value == NORMAL) binding.root.transitionTimeMs else 0
                start()
            }
        }
    }

    private fun getAnimationRadius(circle:Circle,startValue:Float,endValue:Float):ValueAnimator{
        val radiusAnimation = ValueAnimator.ofFloat( startValue,endValue)
        radiusAnimation.addUpdateListener { updatedAnimation ->
            circle.radius = (updatedAnimation?.animatedValue as Float).toDouble() }
        return radiusAnimation.apply {
            duration = 250
        }
    }

}



package com.kalai.cuedes.location

import android.animation.ObjectAnimator
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
import androidx.constraintlayout.widget.ConstraintSet
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
import com.kalai.cuedes.CueDesApplication
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentLocationBinding
import com.kalai.cuedes.location.LocationViewModel.Companion.DEFAULT_RADIUS
import com.kalai.cuedes.location.Status.*
import com.kalai.cuedes.location.selection.SelectionFragment



@SuppressLint("MissingPermission")
class LocationFragment : Fragment() ,OnMapReadyCallback, OnMapLoadedCallback{

    companion object {
        private const val TAG = "LocationFragment"
        val locationRequestHighAccuracy = LocationRequest().apply {  priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000}
        val locationRequestBalanced = LocationRequest().apply { fastestInterval = 60*1000
            interval = 60*1000*60}
        const val REQ_KEY= "LocationFragmentReqKey"
    }



    private lateinit var  mapLogoValueAnimator: ValueAnimator
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var map: SupportMapFragment
    private lateinit var binding:FragmentLocationBinding
    private lateinit var googleMap: GoogleMap
    private var currentSelectedMarker: Marker? = null
    private var currentSelectedRadius: Circle? = null
    private lateinit var onBackPressedCallback: OnBackPressedCallback


    private val repository by lazy { (activity?.application as CueDesApplication).repository }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationBinding.inflate(layoutInflater, container, false)
        map = childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        map.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* Due to Default Location Button being displaced*/
        binding.currentLocationButton.setOnClickListener {
            locationViewModel.getCurrentLocationUpdate() }

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

        setFragmentResultListener(REQ_KEY) { _, bundle ->
            locationViewModel.updateStatus(NORMAL)
            if (!bundle.getBoolean("Successful")) {
                locationViewModel.setSelectedLocation(null) }
            else{
                if(binding.root.animation?.hasEnded() != true)
                    binding.root.addTransitionListener(object:MotionLayout.TransitionListener{
                        override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
                        override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}
                        override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}
                        override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                            val color =context?.getColor(R.color.radius_alarm_active)
                            val currentColor = currentSelectedRadius?.fillColor?:color
                            if(currentColor !=null && color!=null) {
                                val animator = ObjectAnimator.ofArgb(currentColor, color)
                                animator.duration = 500
                                animator.addUpdateListener { animation ->
                                    currentSelectedRadius?.fillColor =
                                        animation.animatedValue as Int
                                }
                                animator.start()
                            }
                            binding.root.removeTransitionListener(this)
                        }
                    })
            }
        }

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean { return true }
            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText.isNullOrBlank()){
                    binding.currentLocationButton.animate()?.alpha(1f)?.setDuration(1000)?.start()
                    /*TODO find a more elegant way to put view behind*/
                    binding.currentLocationButton.elevation = binding.searchView.elevation+1
                }
                else if(binding.currentLocationButton.alpha == 1f){
                    binding.currentLocationButton.animate()?.alpha(0f)?.setDuration(1000)?.start()
                    /*TODO find a more elegant way to put view in-front*/
                    binding.currentLocationButton.elevation = binding.searchView.elevation-1
                }
                return true
            }
        })

        locationViewModel.status.observe(viewLifecycleOwner,
            Observer { updatedStatus->
                Log.d(TAG,"UpdatedStatus  $updatedStatus")
                when(updatedStatus){
                    INITIAL_SELECTION -> locationViewModel.selectedLatLng.value?.let {  setSelectionMode() }
                    NORMAL-> { setNormalMode()  }
                    else -> {}
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
            Log.d(TAG," Radius changed${currentSelectedRadius?.radius} $updatedRadius")
            if(updatedRadius!=null) {
                currentSelectedRadius?.let { circle ->
                    if (circle.radius.toInt() != updatedRadius) {
                        Log.d(TAG,"Circle radius ${circle.radius} $updatedRadius")
                        val radiusAnimation = getAnimationRadius(circle, circle.radius.toInt(), updatedRadius)
                        radiusAnimation.start()
                        Log.d(TAG, "Radius Animation Started")
                        radiusAnimation.doOnEnd {
                            locationViewModel.isCameraIdle.observe(viewLifecycleOwner,object : Observer<Boolean> {
                                override fun onChanged(isCameraIdle: Boolean?) {
                                    if(isCameraIdle == true){
                                        locationViewModel.fitContent(circle)
                                        locationViewModel.isCameraIdle.removeObserver(this)}
                                }
                            })
                            if(locationViewModel.status.value == INITIAL_SELECTION){
                                locationViewModel.updateStatus(SELECTION)
                            }
                        }
                    }
                }
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
                if(locationViewModel.status.value == NORMAL || locationViewModel.status.value == null){
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
                val status = locationViewModel.status.value
                val optimalUpdatedStatus =  if( status == null || status == NORMAL) INITIAL_SELECTION else SELECTION
                Log.d(TAG,"OptimalUpdatedStatus $optimalUpdatedStatus")
                locationViewModel.updateStatus(optimalUpdatedStatus)
            }
        }

        /* TODO calc offset instead of manual input -- val paddingHeightMultiplier = binding.motionLayoutContainer.constraintSetIds*/
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
        onBackPressedCallback.isEnabled = false }


    private fun setSelectionMode(){
        binding.searchView.clearFocus()
        childFragmentManager.commit {
            add(R.id.fragment_selection, SelectionFragment(repository),SelectionFragment.TAG) }
        binding.motionLayoutContainer.transitionToEnd()
        binding.root.transitionToEnd()
        mapLogoValueAnimator.start() }


    private fun setNormalMode(){
        Log.d(TAG,"setNormalMode")
        binding.root.transitionToStart()
        binding.motionLayoutContainer.transitionToStart()
        if(this::mapLogoValueAnimator.isInitialized) {
            mapLogoValueAnimator.reverse()
        }
        binding.motionLayoutContainer.addTransitionListener(object:MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}
            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                childFragmentManager.commit {
                    childFragmentManager.findFragmentByTag(SelectionFragment.TAG)?.let { remove(it) }
                }
                binding.motionLayoutContainer.removeTransitionListener(this)
            }
        })
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
            val radiusAnimation = getAnimationRadius(circle, circle.radius.toInt(),0).apply {
                startDelay=  if (locationViewModel.status.value == NORMAL) binding.root.transitionTimeMs else 0
                start() }
            radiusAnimation.doOnEnd {
                Log.d(TAG,"Removing Marker and Radius")
                currentSelectedMarker?.remove()
                currentSelectedRadius.remove()
                locationViewModel.setRadius(null)
            }
        }
    }


    private fun setupMarkerRadius(marker:Marker) {
        if (marker.position != null) {
            Log.d(TAG, "marker position not null")
            currentSelectedMarker = marker
            currentSelectedRadius = googleMap.addCircle(CircleOptions().center(marker.position))
            currentSelectedRadius?.strokeColor = Color.TRANSPARENT
            context?.getColor(R.color.map_radius)?.let { currentSelectedRadius?.fillColor = it }
            val optimalRadius = locationViewModel.selectedRadius.value ?: DEFAULT_RADIUS
            Log.d(TAG, "optimalRadius $optimalRadius")
            val radiusAnimator = getAnimationRadius(currentSelectedRadius as Circle, 0, optimalRadius)
            radiusAnimator.apply {
                /* Making animation delayed after the motion layout transition as to give less work to the GPU at a single time and be less dependent of motionlayout callback*/
                startDelay = if (locationViewModel.status.value == NORMAL) binding.root.transitionTimeMs else 0
                start() }

            radiusAnimator.doOnEnd {
                currentSelectedRadius?.let { circle -> locationViewModel.fitContent(circle) }
                Log.d(TAG, "radiusAnimator End $currentSelectedRadius ${currentSelectedRadius?.radius}")
                locationViewModel.setRadius(optimalRadius)
            }
        }
    }


    private fun getAnimationRadius(circle:Circle,startValue:Int,endValue:Int):ValueAnimator{
        val radiusAnimation = ValueAnimator.ofInt( startValue,endValue)
        radiusAnimation.addUpdateListener { updatedAnimation ->
            circle.radius = (updatedAnimation?.animatedValue as Int).toDouble() }
        return radiusAnimation.apply {
            duration = 250
        }
    }

}



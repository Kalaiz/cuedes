package com.kalai.cuedes.location

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.kalai.cuedes.CueDesApplication
import com.kalai.cuedes.R
import com.kalai.cuedes.SharedViewModel
import com.kalai.cuedes.databinding.FragmentLocationBinding
import com.kalai.cuedes.isDevicePermissionGranted
import com.kalai.cuedes.location.LocationViewModel.Companion.DEFAULT_RADIUS
import com.kalai.cuedes.location.Status.*
import com.kalai.cuedes.location.selection.SelectionFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber



class LocationFragment : Fragment() ,OnMapReadyCallback, OnMapLoadedCallback{

    // TODO: BUG fix: find me not working during an unusual scenario:
    //  User uses one time permission access, then user gives permission later,
    //  but does not show current location despite pressing find me, unless the app is restarted.
    // TODO: refactor the process of requesting permission so to remove redundancies with DevicePermissionFragment; Possibly a different class.
    private val permissionCode = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            plusElement(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
    }

    private val deviceSnackBarCallback =  object: BaseTransientBottomBar.BaseCallback<Snackbar>(){
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            userNeverAskAgainIntent()
        }
    }

    companion object {

        val locationRequestHighAccuracy = LocationRequest().apply {  priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000}
        val locationRequestBalanced = LocationRequest().apply { fastestInterval = 60*1000
            interval = 60*1000*60}
        val locationRequestFastLowAccuracy = LocationRequest().apply { priority=LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            fastestInterval = 1000
            interval=2000
        }
        const val REQ_KEY= "LocationFragmentReqKey"
    }

    private lateinit var  mapLogoValueAnimator: ValueAnimator
    private val locationViewModel: LocationViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var map: SupportMapFragment
    private lateinit var binding:FragmentLocationBinding
    private lateinit var googleMap: GoogleMap
    private var currentSelectedMarker: Marker? = null
    private var currentSelectedRadius: Circle? = null

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var searchActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var statusViewActiveColorStateList: ColorStateList
    private lateinit var statusViewInActiveColorStateList: ColorStateList

    private lateinit var  requestPermission: ActivityResultLauncher<Array<(String)>>


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationBinding.inflate(layoutInflater, container, false)
        map = childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        map.getMapAsync(this)
        return binding.root
    }

    @SuppressLint("MissingPermission") //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermission =  registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                activityResult->
            if(activityResult.values.any{it==false}){
                activity?.findViewById<View>(android.R.id.content)?.let {
                    val snackBar = Snackbar.make(
                        it,
                        getString(R.string.onboard_permission_msg),
                        Snackbar.LENGTH_SHORT
                    )
                    if (!permissionCode.fold(true,
                            { acc, permissionCode ->
                                acc && shouldShowRequestPermissionRationale(permissionCode)
                            })) {
                        snackBar.addCallback(deviceSnackBarCallback)
                        snackBar.show()
                    } else if(context.isDevicePermissionGranted(permissionCode)){
                        googleMap.isMyLocationEnabled = true
                        googleMap.uiSettings?.isMyLocationButtonEnabled = false
                        sharedViewModel.requestLocation()
                    }

                }
            }
        }
        searchActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Timber.d(  result.toString())
            when(result.resultCode){
                Activity.RESULT_OK ->{
                    result.data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)
                        Timber.d(  "Place: ${place.name}, ${place.latLng}")
                        place.latLng?.let { latLng -> setLocation(latLng) }
                    }}
                else -> { binding.root.transitionToStart()}
            }

        }


        context?.let {
            statusViewActiveColorStateList = ColorStateList.valueOf(getColor(it,R.color.status_view_active))
            statusViewInActiveColorStateList = ColorStateList.valueOf(getColor(it,R.color.status_view_inactive))
        }

    }

    private fun userNeverAskAgainIntent(){
        // TODO: Need to consider when no permission is updated after request permission process when permission is not given
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri= Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* Due to Default Location Button being displaced*/

        with(locationViewModel) {
            binding.findMeButton.setOnClickListener {
              currentLocationUpdate()
            }

            cameraMovement.observe(viewLifecycleOwner, Observer { cameraMovement ->
               Timber.d( "Camera Movement changed")
                if (this@LocationFragment::googleMap.isInitialized) {
                    cameraMovement?.run {
                        if (animated && duration != null) {
                            Timber.d(  "Camera Movement with animation and duration")
                            googleMap.animateCamera(cameraUpdate, duration as Int, null)
                        } else if (animated) {
                            Timber.d( "Camera Movement with animation ")
                            googleMap.animateCamera(cameraUpdate)
                        } else {
                            Timber.d( "Camera Movement ")
                            googleMap.moveCamera(cameraUpdate)
                        }
                    }
                }
            })

           currentLocation.observe(viewLifecycleOwner, object :
                    Observer<Location> {
                override fun onChanged(location: Location?) {
                    if (location != null) {
                        setupCurrentLocation()
                        currentLocation.removeObserver(this)
                    }
                }
            })

            setFragmentResultListener(REQ_KEY) { _, bundle ->
                updateStatus(NORMAL)
                if (!bundle.getBoolean("Successful")) {
                   setSelectedLocation(null)
                } else { /*Successful*/

                    if (binding.root.animation?.hasEnded() != true)
                        binding.root.addTransitionListener(object : MotionLayout.TransitionListener {
                            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
                            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}
                            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}
                            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                                val isActivated =  bundle.getBoolean("Activated")
                                val color = context?.run{if(isActivated) getColor(R.color.radius_alarm_active) else getColor(R.color.radius_alarm_inactive)}
                                val currentColor = currentSelectedRadius?.fillColor ?: color
                                if (currentColor != null && color != null) {
                                    val animator = ObjectAnimator.ofArgb(currentColor, color)
                                    animator.duration = 500
                                    animator.addUpdateListener { animation ->
                                        currentSelectedRadius?.fillColor =
                                                animation.animatedValue as Int
                                    }
                                    animator.start()
                                    animator.doOnEnd { currentSelectedRadius?.let { addRecentCircle(it) } }

                                }
                                binding.root.removeTransitionListener(this)
                            }
                        })
                }
            }


            binding.searchButton.setOnClickListener { startSearchActivity() }

           status.observe(viewLifecycleOwner,
                    Observer { updatedStatus ->
                        Timber.d( "UpdatedStatus  $updatedStatus")
                        when (updatedStatus) {
                            INITIAL_SELECTION -> selectedLatLng.value?.let { setSelectionMode() }
                            NORMAL -> {
                                setNormalMode()
                            }
                            else -> {
                            }
                        }
                    })

            selectedMarker.observe(viewLifecycleOwner, Observer { marker ->
                if (marker == null) {
                    Timber.d("Observed Selected Marker changed to null")
                    removeMarkerRadius()
                } else {
                    Timber.d("Observed Selected Marker changed $marker")
                    setupMarkerRadius(marker)
                }
            })

            selectedRadius.observe(viewLifecycleOwner, Observer { updatedRadius ->
                Timber.d(" Radius changed${currentSelectedRadius?.radius} $updatedRadius")
                if (updatedRadius != null) {
                    currentSelectedRadius?.let { circle ->
                        if (circle.radius.toInt() != updatedRadius) {
                            Timber.d("Circle radius ${circle.radius} $updatedRadius")
                            val radiusAnimation =
                                    getAnimationRadius(circle, circle.radius.toInt(), updatedRadius)
                            radiusAnimation.start()
                            Timber.d("Radius Animation Started")
                            radiusAnimation.doOnEnd {
                                isCameraIdle.observe(viewLifecycleOwner,
                                        object : Observer<Boolean> {
                                            override fun onChanged(isCameraIdle: Boolean?) {
                                                if (isCameraIdle == true) {
                                                    locationViewModel.fitContent(circle)
                                                    locationViewModel.isCameraIdle.removeObserver(this)
                                                }
                                            }
                                        })
                                if (status.value == INITIAL_SELECTION) {
                                    updateStatus(SELECTION)
                                }
                            }
                        }
                    }
                }
            })


            lifecycleScope.launch {
                alarmFlow.collect { alarms ->
                    /*For drawing repo stored circles ( Excluding ones which were recently drawn )*/
                    updateAlarms(alarms)
                    binding.activeAlarmTextView.text = locationViewModel.alarmStatusText
                }
            }
            isAlarmActive.observe(viewLifecycleOwner, Observer { isActive ->
                context?.let {
                    binding.statusView.backgroundTintList = if (isActive == true) statusViewActiveColorStateList else statusViewInActiveColorStateList
                }
            })
        }
    }


    override fun onResume() {
        super.onResume()
        Timber.d( "onResume")
        onBackPressedCallback.isEnabled = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(locationViewModel.status.value == NORMAL || locationViewModel.status.value == null){
                    Timber.d( "Normal Status")
                    onBackPressedCallback.remove()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
                this,
                onBackPressedCallback)
    }


    @SuppressLint("MissingPermission") // Linter is not able to check that the extension function is doing the permission check or not.
    override fun onMapReady(googleMap: GoogleMap) {
        Timber.d( "onMapReady called")
        if (googleMap != null) {
            locationViewModel.needUpdateCircles.observe(viewLifecycleOwner, Observer {
                circleOptions-> circleOptions.forEach{(alarm,circleOption)-> locationViewModel.addAlarmCircle(Pair(alarm,googleMap.addCircle(circleOption) ))
                circleOption.center?.let { MarkerOptions().position(it) }
                    ?.let { googleMap.addMarker(it) }

            }

            })
            this.googleMap = googleMap
            locationViewModel.mapReady()
            sharedViewModel.requestLocation()
            sharedViewModel.currentLocation.observe(this, Observer {
                locationViewModel.updateCurrentLocation(it)
            })


            googleMap.setOnCameraIdleListener {
                Timber.d( "Camera Idle")
                locationViewModel.cameraIdle()
                googleMap.setOnMapLoadedCallback(this)
            }

            googleMap.setOnCameraMoveStartedListener {
                Timber.d( "camera move started $it")
                locationViewModel.setCameraMoveAction(it)
                locationViewModel.cameraMoving()
            }

        }

        if(context.isDevicePermissionGranted(permissionCode)) {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        }
        else{
            requestPermission.launch(permissionCode.toTypedArray())
        }

        /*Overriding default behaviour*/
        googleMap?.setOnMarkerClickListener { true }

        googleMap?.setOnMapLongClickListener {
            latLng -> Timber.d( latLng.toString())
            if(latLng!=null) {
                setLocation(latLng)
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
        Timber.d( "OnPause")
        onBackPressedCallback.isEnabled = false }


    private fun setSelectionMode(){
        /* binding.searchView.clearFocus()*/
        childFragmentManager.commit {
            add(R.id.fragment_selection, SelectionFragment(),SelectionFragment.TAG) }
        binding.motionLayoutContainer.transitionToEnd()
        if(binding.root.currentState == binding.root.startState)
            binding.root.transitionToEnd()
        mapLogoValueAnimator.start() }


    private fun setNormalMode(){
        Timber.d( "setNormalMode")
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
        Timber.d( "Map loaded")
        locationViewModel.mapLoaded()


    }

    private fun removeMarkerRadius(){
        Timber.d( "removeMarkerRadius called")
        val currentSelectedRadius = currentSelectedRadius
        val currentSelectedMarker = currentSelectedMarker
        currentSelectedRadius?.let { circle ->
            Timber.d(  "radiusAnimation ")
            val radiusAnimation = getAnimationRadius(circle, circle.radius.toInt(),0).apply {
                startDelay=  if (locationViewModel.status.value == NORMAL) binding.root.transitionTimeMs else 0
                start() }
            radiusAnimation.doOnEnd {
                Timber.d( "Removing Marker and Radius")
                currentSelectedMarker?.remove()
                currentSelectedRadius.remove()
                locationViewModel.setRadius(null)
            }
        }
    }


    private fun setupMarkerRadius(marker:Marker) {
        if (marker.position != null) {
            Timber.d( "marker position not null")
            currentSelectedMarker = marker
            currentSelectedRadius = googleMap.addCircle(CircleOptions().center(marker.position))
            currentSelectedRadius?.strokeColor = Color.TRANSPARENT
            context?.getColor(R.color.map_radius)?.let { currentSelectedRadius?.fillColor = it }
            val optimalRadius = locationViewModel.selectedRadius.value ?: DEFAULT_RADIUS
            Timber.d(  "optimalRadius $optimalRadius")
            val radiusAnimator = getAnimationRadius(currentSelectedRadius as Circle, 0, optimalRadius)
            radiusAnimator.apply {
                /* Making animation delayed after the motion layout transition as to give less work to the GPU at a single time and be less dependent of motionlayout callback*/
                startDelay = if (locationViewModel.status.value == NORMAL) binding.root.transitionTimeMs else 0
                start() }

            radiusAnimator.doOnEnd {
                currentSelectedRadius?.let { circle -> locationViewModel.fitContent(circle) }
                Timber.d(  "radiusAnimator End $currentSelectedRadius ${currentSelectedRadius?.radius}")
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


    private fun startSearchActivity(){
        val fields = listOf(Place.Field.NAME,Place.Field.LAT_LNG)

        val intent =
                context?.let {
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                            .build(it)
                }

        binding.root.addTransitionListener(object :MotionLayout.TransitionListener{
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}
            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                searchActivityLauncher.launch(intent)
                binding.root.removeTransitionListener(this)}
        })
        binding.root.transitionToEnd()
    }

    private fun setLocation(latLng: LatLng){
        locationViewModel.setSelectedLocation(googleMap.addMarker(MarkerOptions().position(latLng)))
        val status = locationViewModel.status.value
        val optimalUpdatedStatus =  if( status == null || status == NORMAL) INITIAL_SELECTION else SELECTION
        Timber.d( "OptimalUpdatedStatus $optimalUpdatedStatus")
        locationViewModel.updateStatus(optimalUpdatedStatus)
    }
}



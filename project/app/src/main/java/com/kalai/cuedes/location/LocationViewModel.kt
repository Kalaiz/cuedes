package com.kalai.cuedes.location

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.kalai.cuedes.CueDesApplication
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.getCameraUpdateBounds
import com.kalai.cuedes.location.Status.*


@SuppressLint("MissingPermission")
class LocationViewModel(application: Application) : AndroidViewModel(application) {


    companion object{
        private const val TAG = "LocationViewModel"
        const val DEFAULT_ZOOM = 12f
        const val DEFAULT_RADIUS = 500
    }



    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)
    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation:LiveData<Location> get() = _currentLocation

    private val _selectedLatLng = MutableLiveData<LatLng?>()
    val selectedLatLng:LiveData<LatLng?> get() = _selectedLatLng

    private val _cameraMovement = MutableLiveData<CameraMovement>()
    val cameraMovement:LiveData<CameraMovement> get() =  _cameraMovement

    private val _isCameraIdle = MutableLiveData<Boolean>()
    val isCameraIdle:LiveData<Boolean> get() = _isCameraIdle

    private val _mapCameraMoveAction = MutableLiveData<Int>()
    val mapCameraMoveAction:LiveData<Int> get() = _mapCameraMoveAction

    private val _mapLoaded = MutableLiveData<Boolean>(false)
    val isMapLoaded:LiveData<Boolean> get() = _mapLoaded

    private val _status = MutableLiveData<Status?>(null)
    val status:LiveData<Status?> get() = _status

    private val _selectedMarker = MutableLiveData<Marker?>()
    val selectedMarker:LiveData<Marker?> get() = _selectedMarker

    private val _selectedRadius = MutableLiveData<Int?>()
    val selectedRadius:LiveData<Int?> get() = _selectedRadius

    private val _isAlarmActive = MutableLiveData<Boolean?>()
    val isAlarmActive:LiveData<Boolean?> get() = _isAlarmActive

    private lateinit var alarms:List<Alarm>

    private val numOfActiveAlarm:Int  get(){
        return if (::alarms.isInitialized) alarms.filter { alarm -> alarm.isActivated  }.count() else 0}

    val alarmStatusText:String get(){
        val numberOfActiveAlarm = numOfActiveAlarm
        return "$numberOfActiveAlarm active alarm${if(numberOfActiveAlarm>1) "s" else ""}"
    }

    fun requestLocation(){
        Log.d(TAG,"requesting Location")
        val locationCallback =  object: LocationCallback(){
            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                Log.d(TAG,"Location available? ${locationAvailability?.isLocationAvailable}")
                fusedLocationClient.lastLocation.addOnSuccessListener {location ->
                    Log.d(TAG,"Success")
                    location?.let {
                        _currentLocation.value = it
                        Log.d(TAG,"Current Location is  $it")
                        Log.d(TAG,"Current LngLat is  ${it.longitude} ${it.latitude}")
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(LocationFragment.locationRequestHighAccuracy,locationCallback, Looper.myLooper())
    }

    fun currentLocationUpdate(){
        currentLocationCameraMovement(true)
    }


    fun cameraIdle() {
        Log.d(TAG,"cameraIdle() called")
        _isCameraIdle.value = true
    }


    private fun currentLocationCameraMovement(animated:Boolean,duration: Int? = null){
        val location = _currentLocation.value
        location?.run {
            val latLng = LatLng(latitude,longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM)
            _cameraMovement.value =
                CameraMovement(cameraUpdate, animated,duration)
        }
    }

    fun setupCurrentLocation(){
        currentLocationCameraMovement(false)
    }

    fun setCameraMoveAction(action: Int) {

    }

    fun cameraMoving() {
        Log.d(TAG,"CameraMoving")
        _isCameraIdle.value = false
        _mapLoaded.value = false
    }


    fun mapReady(){
        _mapLoaded.value=true
    }

    fun mapLoaded() {
        _mapLoaded.value=true
    }


    fun updateStatus(status:Status){
        if(status != _status.value)
            _status.value = status
    }

    fun setSelectedLocation(marker: Marker?) {
        Log.d(TAG,"setSelectedLocation")
        val status = _status.value
        if(status == SELECTION || status == INITIAL_SELECTION ||(status == NORMAL && marker == null)){
            Log.d(TAG,"SELECTION or INITIAL_SELECTION or (NORMAL AND marker null)")
            /* So that the removing animation occurs*/
            _selectedMarker.value = null
            /*Removing for the record*/
            _selectedLatLng.value = null
        }
        /*If in SELECTION mode, need to assist in animating the removal of marker ( as did above) and then animate the new one */
        if(marker!=null ){
            Log.d(TAG,"marker not null $marker")
            _selectedMarker.value = marker
            _selectedLatLng.value = marker.position
        }
    }

    fun setRadius(radius:Int?){
        _selectedRadius.value = radius
    }


   fun fitContent(circle: Circle){
        _cameraMovement.value = CameraMovement(getCameraUpdateBounds(circle,100),true,300)
    }

    fun updateAlarms(alarms: List<Alarm>) {
        this.alarms = alarms
        val numberOfActiveAlarm = numOfActiveAlarm
        _isAlarmActive.value = numberOfActiveAlarm > 0

    }




}
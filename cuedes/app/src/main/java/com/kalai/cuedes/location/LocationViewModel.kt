package com.kalai.cuedes.location

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Color
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.kalai.cuedes.CueDesApplication
import com.kalai.cuedes.R
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.getCameraUpdateBounds
import com.kalai.cuedes.location.Status.*
import timber.log.Timber
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.Set
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.count
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.groupBy
import kotlin.collections.hashMapOf
import kotlin.collections.listOf
import kotlin.collections.mutableSetOf
import kotlin.collections.none
import kotlin.collections.set
import kotlin.collections.subtract
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@SuppressLint("MissingPermission")
class LocationViewModel(application: Application) : AndroidViewModel(application) {


    companion object{
        const val DEFAULT_ZOOM = 12f
        const val DEFAULT_RADIUS = 500
    }

    private var recentlyAddedAlarm: Alarm? = null



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

    private val _needUpdateCircles = MutableLiveData<HashMap<Alarm,CircleOptions>>()
    val needUpdateCircles:LiveData<HashMap<Alarm,CircleOptions>> get() = _needUpdateCircles

    private val alarmCircleMap = hashMapOf<Alarm,Circle?>()

    private  var alarms = listOf<Alarm>()

    private val numOfActiveAlarm:Int  get()=  alarms.filter { alarm -> alarm.isActivated  }.count()

    val alarmStatusText:String get()="$numOfActiveAlarm active alarm${if(numOfActiveAlarm>1) "s" else ""}"


    private val repository by lazy { (getApplication<Application>() as CueDesApplication).repository }

    val alarmFlow = repository.alarms
    private  val fusedLocationClient  by lazy { (getApplication<Application>() as CueDesApplication).fusedLocationClient}

/* _currentLocation.value = it*/
    /*TODO need to move to CueDesApplication */




    fun currentLocationUpdate(){
        currentLocationCameraMovement(true)
    }


    fun cameraIdle() {
        Timber.d( "cameraIdle() called")
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
        Timber.d( "CameraMoving")
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


    fun addAlarmCircle(alarmCircle:Pair<Alarm,Circle>){
        alarmCircleMap[alarmCircle.first] = alarmCircle.second
    }

    fun setSelectedLocation(marker: Marker?) {
        Timber.d( "setSelectedLocation")
        val status = _status.value
        if(status == SELECTION || status == INITIAL_SELECTION ||(status == NORMAL && marker == null)){
            Timber.d( "SELECTION or INITIAL_SELECTION or (NORMAL AND marker null)")
            /* So that the removing animation occurs*/
            _selectedMarker.value = null
            /*Removing for the record*/
            _selectedLatLng.value = null
        }
        /*If in SELECTION mode, need to assist in animating the removal of marker ( as did above) and then animate the new one */
        if(marker!=null ){
            Timber.d( "marker not null $marker")
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

    fun addAlarm(alarm:Alarm){
        alarmCircleMap[alarm]=null
        recentlyAddedAlarm = alarm
    }

    fun updateAlarms(alarms: List<Alarm>) {
        Timber.d( "Updating alarms")
        addCircles(alarms.subtract(this.alarms))
        removeCircles(this.alarms.subtract(alarms))
        this.alarms = alarms
        Timber.d( "number of active alarms $numOfActiveAlarm $alarms")
        _isAlarmActive.value = numOfActiveAlarm > 0
    }

    private fun removeCircles(alarms: Set<Alarm>) {
        Timber.d("Removing Circles $alarms")
        alarms.forEach {
            alarmCircleMap[it]?.remove()
            alarmCircleMap.remove(it) }
    }


    fun updateCurrentLocation(location: Location){
        _currentLocation.value = location
    }

    private fun addCircles(alarms: Set<Alarm>) {
        val needUpdateCircles = hashMapOf<Alarm,CircleOptions>()
        val refinedAlarmCircles = updateIsActivatedCircles(alarms)
        refinedAlarmCircles.forEach { alarm ->
            with(alarm) {
                /*TODO thinking of using DiffUtil*/

                needUpdateCircles[alarm]=CircleOptions()
                        .radius(radius.toDouble())
                        .center(LatLng(latitude,longitude))
                        .strokeColor(Color.TRANSPARENT)
                        .fillColor(circleColour(isActivated))
            }

            /*This will be drawn by UI later*/
            alarmCircleMap[alarm] = null
            _needUpdateCircles.value = needUpdateCircles
        }
    }


    private fun circleColour(isActivated:Boolean):Int=
            if(isActivated) getApplication<Application>().applicationContext.getColor(R.color.radius_alarm_active)
            else getApplication<Application>().applicationContext.getColor(R.color.radius_alarm_inactive)


    private fun updateIsActivatedCircles(alarms: Set<Alarm>):List<Alarm> {
        val refinedAlarms = mutableSetOf<Alarm>()
        alarms.groupBy {it.name}
                .filter {entry -> entry.value.size>1 }
                .forEach{(name,alarms)->
                    val alarm = alarmCircleMap.keys.filter { alarm -> alarm.name == name }[0]
                    refinedAlarms.add(alarm)
                    alarmCircleMap[alarm]?.fillColor = circleColour(alarms[0].isActivated)}//[0] Due to alarms.subtract(this.alarms) preserving order
        return alarms.filter { alarm-> refinedAlarms.none { refinedAlarm -> alarm.name == refinedAlarm.name } }

    }

    fun addRecentCircle(circle:Circle) {
        recentlyAddedAlarm?.let { addAlarmCircle(Pair(it,circle))  }

    }


}
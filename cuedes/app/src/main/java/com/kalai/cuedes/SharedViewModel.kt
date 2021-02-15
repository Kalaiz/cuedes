package com.kalai.cuedes

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.model.LatLng
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.location.LocationFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SharedViewModel(application:Application) : AndroidViewModel(application) {


    private val cueDesApplication by lazy { getApplication<Application>() as CueDesApplication }
    private val repository by lazy {cueDesApplication.repository}
    private val geofencingClient by lazy { cueDesApplication.geofencingClient }
    private val fusedLocationClient by lazy { cueDesApplication.fusedLocationClient }

    private val _error= MutableLiveData<String>()
    val error:LiveData<String> = _error

    private val numOfActiveAlarms get()
    = viewModelScope.async { fetchAlarms().filter { it.isActivated}.size}


    private suspend fun fetchAlarms()= suspendCoroutine<List<Alarm>>{
        viewModelScope.launch(Dispatchers.IO)
        { repository.alarms.collect { alarms -> it.resume(alarms)
            this.cancel()}
        }
    }

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation:LiveData<Location> get() = _currentLocation

    fun updateIsActivated(alarmName: String,isActivated: Boolean) {
        with(cueDesApplication){
            var processedIsActivated:Boolean
            viewModelScope.launch {
                if (isActivated && numOfActiveAlarms.await() >= 5) {
                    _error.value =
                            "Activation failed. There are already 5 active alarms. Please turn one of them off"
                    processedIsActivated = !isActivated
                }
                else {
                    processedIsActivated = isActivated
                    if (isActivated)
                        findAlarm(alarmName)?.let { createGeoFence(it) }
                    else
                        removeGeoFence(alarmName)
                }

                viewModelScope.launch(Dispatchers.IO){
                    repository.updateIsActivated(alarmName,processedIsActivated)
                }
            }
        }
    }

    fun deleteAlarm(alarmName: String) {
        viewModelScope.launch(Dispatchers.IO){
            with(cueDesApplication){
                findAlarm(alarmName)?.let{ repository.delete(it) }
            }
        }
    }


    /*TODO: Moving to SharedViewModel instead?*/
    private val geofencePendingIntent: PendingIntent
        get() {
            val intent = Intent(getApplication(), GeofenceBroadcastReceiver::class.java)
            return  PendingIntent.getBroadcast(getApplication(), System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_ONE_SHOT)
        }

    /*Returns alarm if successful or else null*/
    @SuppressLint("MissingPermission")
    private suspend fun insertIntoRepository(alarm: Alarm): Alarm {
        /*        val handler = CoroutineExceptionHandler { _, exception ->
            Timber.d("Exception ${exception.message}")
            _error.value = exception.message
        }*/
        val location = fetchLocation()
        Timber.d("inserting into repo")
        /*TODO: throwing exceptions would be better and catching them would be better */
        return withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            Timber.d("location ${location}")
            val latLng = location.let { LatLng(it.latitude, it.longitude) }
            val alarmLatLng = LatLng(alarm.latitude,alarm.longitude)
            val isWithinBound = latLng.checkIsInBounds(alarm.radius,alarmLatLng)
            if(numOfActiveAlarms.await()>=5){
                alarm.isActivated = false
                repository.insert(alarm)
                viewModelScope.launch {
                    _error.value = "Alarm de-activated. There are already 5 alarms activated, please turn one of them off." }
            }
            else if (!isWithinBound){
                Timber.d("Within bounds")
                repository.insert(alarm)}
            else if(isWithinBound)
            {
                Timber.d("location within bound")
                alarm.isActivated = false
                repository.insert(alarm)
                viewModelScope.launch {
                    _error.value = "Alarm de-activated. You are trying to set an alarm for a location which you are currently in." }

            }
            alarm
        }
    }

    /*TODO: Check whether is in bound or not before triggering alarm*/



    /*Returns alarm if successful or else null*/
    /*TODO: Ask for permission if permission denied during runtime ( maybe via notification)*/
    @SuppressLint("MissingPermission")
    private suspend fun createGeoFence(alarm: Alarm) : Alarm? = suspendCoroutine { cont->
        alarm.run{
            val geofence =  Geofence.Builder()
                    .setRequestId(name)
                    .setCircularRegion(latitude,longitude,radius.toFloat())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

            geofencingClient.addGeofences(geofencingRequest,geofencePendingIntent)?.run {
                addOnSuccessListener {
                    cont.resume(alarm)
                    Timber.d("Alarm set successfully")
                }
                addOnFailureListener {
                    cont.resume(null)
                    Timber.d("Alarm not set successfully ${it}")
                }
            }
        }
    }

    private suspend fun findAlarm(alarmName: String): Alarm? = suspendCoroutine { cont->
        CoroutineScope(Dispatchers.IO).launch{
            repository.alarms.collect { alarms->
                cont.resume(alarms.find { alarm -> alarm.name == alarmName })
                this.cancel()
            }
        }
    }

    private fun removeGeoFence(alarmName:String){
        geofencingClient.removeGeofences(mutableListOf(alarmName))
    }

    suspend fun processAlarm(alarm:Alarm){
        val repoAlarm = viewModelScope.async { insertIntoRepository(alarm) }
        if(repoAlarm.await().isActivated)
            createGeoFence(repoAlarm.await())
    }

    fun requestLocation()  {
        viewModelScope.launch {
            _currentLocation.value =   fetchLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLocation():Location = suspendCoroutine { cont ->
        Timber.d( "requesting Location")
        val locationCallback =  object: LocationCallback(){
            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                Timber.d( "Location available? ${locationAvailability?.isLocationAvailable}")
                fusedLocationClient.lastLocation.addOnSuccessListener {location ->
                    Timber.d( "Success")
                    location?.let {
                        cont.resume(it)
                        Timber.d( "Current Location is  $it")
                        Timber.d( "Current LngLat is  ${it.longitude} ${it.latitude}")
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(LocationFragment.locationRequestHighAccuracy,locationCallback, Looper.myLooper())
    }


}
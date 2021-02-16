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
import com.kalai.cuedes.notification.Notification
import com.kalai.cuedes.notification.NotificationConfig
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
    private val applicationContext get() =  getApplication<Application>().applicationContext
    private val _notificationConfig= MutableLiveData<NotificationConfig>()
    val notificationConfig:LiveData<NotificationConfig> = _notificationConfig


    private val numOfActiveAlarms get()
    = viewModelScope.async { fetchAlarms().filter { it.isActivated}.size}

    private val warningFiveAlarmMessage = applicationContext.getString(R.string.warning_too_many_alarm)

    private val warningNearVicinityMessage = applicationContext.getString(R.string.warning_alarm_at_destination)

    private val errorMessage = applicationContext.getString(R.string.error_repo_insert)

    private val errorConfig = NotificationConfig.Builder
            .create(Notification.ERROR,errorMessage)
            .addIcon(R.drawable.ic_error)
            .build()

    private val warningFiveAlarmConfig = NotificationConfig.Builder
            .create(Notification.WARNING,warningFiveAlarmMessage)
            .addIcon(R.drawable.ic_notification_off)
            .build()

    private val warningNearVicinityConfig = NotificationConfig.Builder
            .create(Notification.WARNING,warningNearVicinityMessage)
            .addIcon(R.drawable.ic_notification_off)
            .build()

    private val geofencePendingIntent: PendingIntent
        get() {
            val intent = Intent(getApplication(), GeofenceBroadcastReceiver::class.java)
            return  PendingIntent.getBroadcast(getApplication(), System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_ONE_SHOT)
        }

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation:LiveData<Location> get() = _currentLocation

    private suspend fun fetchAlarms()= suspendCoroutine<List<Alarm>>{
        viewModelScope.launch(Dispatchers.IO)
        { repository.alarms.collect { alarms -> it.resume(alarms)
            this.cancel()}
        }
    }


    suspend fun updateIsActivated(alarmName: String, isActivated: Boolean):Boolean = suspendCoroutine{ cont->
        with(cueDesApplication){
            viewModelScope.launch {
                val alarm = findAlarm(alarmName)
                if (alarm != null) {
                    if (alarm.isActivated != isActivated) {
                        when {
                            isActivated and (numOfActiveAlarms.await() >= 5) -> {
                                _notificationConfig.value = warningFiveAlarmConfig
                                cont.resume(false)
                            }
                            isActivated and isCurrentLocationWithinAlarmBound(alarm) -> {
                                _notificationConfig.value = warningNearVicinityConfig
                                cont.resume(false)
                            }
                            else -> {
                                if(isActivated)
                                    createGeoFence(alarm)
                                else
                                    removeGeoFence(alarmName)
                                viewModelScope.launch(Dispatchers.IO) { repository.updateIsActivated(alarmName, isActivated)
                                }.invokeOnCompletion { handler ->
                                    val hasError = handler != null
                                    if (hasError)
                                        viewModelScope.launch(Dispatchers.Main) {
                                            _notificationConfig.value = errorConfig
                                        }
                                    cont.resume(!hasError)
                                }
                            }
                        }
                    } else
                        cont.resume(true)
                }
                else
                    cont.resume(false)
            }

        }
    }


    private suspend fun isCurrentLocationWithinAlarmBound(alarm: Alarm):Boolean = suspendCoroutine {  cont->
        viewModelScope.launch {
            val location = fetchLocation()
            val latLng = location.let { LatLng(it.latitude, it.longitude) }
            val alarmLatLng = LatLng(alarm.latitude,alarm.longitude)
            cont.resume(latLng.checkIsInBounds(alarm.radius,alarmLatLng))
        }

    }


    fun deleteAlarm(alarmName: String) {
        viewModelScope.launch(Dispatchers.IO){
            with(cueDesApplication){
                findAlarm(alarmName)?.let{ repository.delete(it) }
            }
        }
    }


    /*Returns alarm if successful or else null*/
    @SuppressLint("MissingPermission")
    private suspend fun insertIntoRepository(alarm: Alarm): Alarm =
            withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
                Timber.d("inserting into repo")
                val isWithinBound = isCurrentLocationWithinAlarmBound(alarm)
                if(numOfActiveAlarms.await()>=5){
                    alarm.isActivated = false
                    repository.insert(alarm)
                    viewModelScope.launch {
                        _notificationConfig.value = warningFiveAlarmConfig }
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
                        _notificationConfig.value =  warningNearVicinityConfig }

                }
                alarm
            }



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
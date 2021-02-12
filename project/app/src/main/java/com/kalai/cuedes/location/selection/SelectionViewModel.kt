package com.kalai.cuedes.location.selection

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.kalai.cuedes.CueDesApplication
import com.kalai.cuedes.GeofenceBroadcastReceiver
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.location.DistanceUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("MissingPermission")
class SelectionViewModel(application: Application)  : AndroidViewModel(application) {


    companion object{
        const val TAG = "SelectionViewModel"
    }
    private val _selectedLocation = MutableLiveData<LatLng>()
    private val selectedLocation: LiveData<LatLng> = _selectedLocation


    private val repository by lazy { (getApplication<Application>() as CueDesApplication).repository }
    private val geofencingClient by lazy { (getApplication<Application>() as CueDesApplication).geofencingClient }
    private var _selectedRadius = MutableLiveData<Int>()
    val selectedRadius: LiveData<Int> = _selectedRadius


    private val _selectedDistanceUnit = MutableLiveData<DistanceUnit>()
    val selectedDistanceUnit: LiveData<DistanceUnit> = _selectedDistanceUnit

    private val _isAlarmSet = MutableLiveData<Boolean?>()
    val isAlarmSet: LiveData<Boolean?> = _isAlarmSet

    private val geofencePendingIntent: PendingIntent get() {
        val intent = Intent(getApplication(), GeofenceBroadcastReceiver::class.java)
        return  PendingIntent.getBroadcast(getApplication(), System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_ONE_SHOT)
    }


    fun postSelection(){
        viewModelScope.launch{
            val name = async {
                withContext(Dispatchers.IO){
                    insertIntoRepository() }
            }
            _isAlarmSet.value =setAlarm(name.await())

        }
    }


    private fun insertIntoRepository():String{
        val name = "alarm"+repository.getCount()
        val latLng = selectedLocation.value
        val radius = selectedRadius.value
        if(latLng!=null && radius!=null) {
            val latitude = latLng.latitude
            val longitude = latLng.longitude
            repository.insert(Alarm(name,latitude,longitude,radius))}
        return  name
    }

    private suspend fun setAlarm(name:String) :Boolean = suspendCoroutine { cont->
        _selectedLocation.value?.run{
            val radius:Float = selectedRadius.value?.toFloat() ?:0f
            val geofence =  Geofence.Builder()
                .setRequestId(name)
                .setCircularRegion(latitude,longitude,radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient.addGeofences(geofencingRequest,geofencePendingIntent)?.run {
                addOnSuccessListener {
                    cont.resume(true)
                    Log.d(TAG,"Alarm set successfully")
                }
                addOnFailureListener {
                    cont.resume(false)
                    Log.d(TAG,"Alarm not set successfully")
                }
            }
        }


    }

    fun setLatLng(latLng: LatLng) {
        _selectedLocation.value = latLng
    }

    fun setRadius(radius: Int) {
        _selectedRadius.value = radius
    }




}
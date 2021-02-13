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
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("MissingPermission")
class SelectionViewModel(application: Application)  : AndroidViewModel(application) {


    companion object{
        const val TAG = "SelectionViewModel"
    }
    private val _selectedLocation = MutableLiveData<LatLng>()
    private val selectedLocation: LiveData<LatLng> = _selectedLocation


    private var _selectedRadius = MutableLiveData<Int>()
    val selectedRadius: LiveData<Int> = _selectedRadius


    private val _selectedDistanceUnit = MutableLiveData<DistanceUnit>()
    val selectedDistanceUnit: LiveData<DistanceUnit> = _selectedDistanceUnit

    private val _alarmSet = MutableLiveData<Alarm?>()
    val alarmSet: LiveData<Alarm?> = _alarmSet

    private val cueDesApplication by lazy { getApplication<Application>() as CueDesApplication }



    fun postSelection(){
        viewModelScope.launch{
            val alarm:Deferred<Alarm?> = async {
                withContext(Dispatchers.IO){
                    val latLng = selectedLocation.value
                    val radius = _selectedRadius.value
                    if(latLng!=null && radius!=null )
                        cueDesApplication.insertIntoRepository(latLng, radius)
                    else
                        null
                }
            }
            _alarmSet.value = cueDesApplication.setAlarm(alarm.await())
        }
    }



    fun setLatLng(latLng: LatLng) {
        _selectedLocation.value = latLng
    }

    fun setRadius(radius: Int) {
        _selectedRadius.value = radius
    }




}
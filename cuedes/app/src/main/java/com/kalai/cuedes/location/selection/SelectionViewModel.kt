package com.kalai.cuedes.location.selection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.kalai.cuedes.CueDesApplication
import com.kalai.cuedes.FailCreateAlarmException
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.location.DistanceUnit
import kotlinx.coroutines.*

class SelectionViewModel(application: Application)  : AndroidViewModel(application) {


    private val _selectedLocation = MutableLiveData<LatLng>()


    private var _selectedRadius = MutableLiveData<Int>()
    val selectedRadius: LiveData<Int> = _selectedRadius


    private val _selectedDistanceUnit = MutableLiveData<DistanceUnit>()
    val selectedDistanceUnit: LiveData<DistanceUnit> = _selectedDistanceUnit

    private val _alarm = MutableLiveData<Alarm?>()
    val alarm: LiveData<Alarm?> = _alarm

    private val cueDesApplication by lazy { getApplication<Application>() as CueDesApplication }
    private val repository by lazy { cueDesApplication.repository }

    private var needVibration = false

    private var needSound = false




   fun createAlarm(){
        viewModelScope.launch {
            val alarmName =  viewModelScope.async(Dispatchers.IO) { "alarm" + repository.getCount()}
            val latitude = _selectedLocation.value?.latitude
            val longitude = _selectedLocation.value?.longitude
            val radius = _selectedRadius.value
            if(latitude!=null && longitude!=null && radius!= null)
                _alarm.value = Alarm(alarmName.await(), latitude, longitude, radius,hasVibration = needVibration,hasSound = needSound)
            else
               _alarm.value = null
        }
    }


    fun setLatLng(latLng: LatLng) {
        _selectedLocation.value = latLng
    }

    fun setRadius(radius: Int) {
        _selectedRadius.value = radius
    }


    fun updateNeedVibration(needVibration:Boolean){this.needVibration = needVibration}

    fun updateNeedSound(needSound:Boolean) { this.needSound = needSound }


}
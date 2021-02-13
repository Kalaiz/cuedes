package com.kalai.cuedes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharedViewModel(application:Application) : AndroidViewModel(application) {


    private val cueDesApplication by lazy { getApplication<Application>() as CueDesApplication }


    fun updateIsActivated(alarmName: String,isActivated: Boolean) {
        with(cueDesApplication){
            viewModelScope.launch(Dispatchers.IO){ repository.updateIsActivated(alarmName,isActivated) }
            if(isActivated)
                viewModelScope.launch {createGeoFence(findAlarm(alarmName))}
            else
                removeGeoFence(alarmName)

        }
    }

    fun deleteAlarm(alarmName: String) {
        viewModelScope.launch(Dispatchers.IO){
            with(cueDesApplication){
                findAlarm(alarmName)?.let{ repository.delete(it) }
            }
        }
    }




}
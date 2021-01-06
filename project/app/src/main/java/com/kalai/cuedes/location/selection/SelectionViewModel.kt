package com.kalai.cuedes.location.selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.kalai.cuedes.location.DistanceUnit

class SelectionViewModel :ViewModel(){

    private var _selectedLocation =  MutableLiveData<LatLng?>()
    val selectedLocation: LiveData<LatLng?>
        get() { return _selectedLocation}

    private var _selectedRadius = MutableLiveData<Int>()
    val selectedRadius: LiveData<Int>
        get() { return _selectedRadius}

    private var _selectedDistanceUnit = MutableLiveData<DistanceUnit>()
    val selectedDistanceUnit: LiveData<DistanceUnit>
        get() { return _selectedDistanceUnit}


    fun setLatLng(latLng: LatLng){
        _selectedLocation.value = latLng
    }

    fun setRadius(radius:Int){
        _selectedRadius.value=radius
    }

    fun clear(){
        _selectedLocation.value = null
    }


}
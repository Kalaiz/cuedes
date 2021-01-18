package com.kalai.cuedes.location.selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.kalai.cuedes.data.AlarmRepository
import com.kalai.cuedes.location.DistanceUnit

class SelectionViewModel(var repository: AlarmRepository)  : ViewModel() {

    private var _selectedLocation = MutableLiveData<LatLng?>()
    val selectedLocation: LiveData<LatLng?>
        get() {
            return _selectedLocation
        }

    private var _selectedRadius = MutableLiveData<Int>()
    val selectedRadius: LiveData<Int>
        get() {
            return _selectedRadius
        }

    private var _selectedDistanceUnit = MutableLiveData<DistanceUnit>()
    val selectedDistanceUnit: LiveData<DistanceUnit>
        get() {
            return _selectedDistanceUnit
        }


    fun setLatLng(latLng: LatLng) {
        _selectedLocation.value = latLng
    }

    fun setRadius(radius: Int) {
        _selectedRadius.value = radius
    }

    fun clear() {
        _selectedLocation.value = null
    }


    class SelectionViewModelFactory(private val repository: AlarmRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SelectionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SelectionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }


    }
}
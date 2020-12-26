package com.kalai.cuedes.location

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import java.time.Duration


@SuppressLint("MissingPermission")
class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)
    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation:LiveData<Location> get() = _currentLocation

    private val _selectedLatLng = MutableLiveData<LatLng>()
    val selectedLatLng:LiveData<LatLng> get() = _selectedLatLng


    private val _cameraMovement = MutableLiveData<CameraMovement>()
    val cameraMovement:LiveData<CameraMovement> get() =  _cameraMovement

    private val _isCameraIdle = MutableLiveData<Boolean>()
    val isCameraIdle:LiveData<Boolean> get() = _isCameraIdle

    private val _mapCameraMoveAction = MutableLiveData<Int>()
    val mapCameraMoveAction:LiveData<Int> get() = _mapCameraMoveAction



    companion object{ private const val TAG = "LocationViewModel" }


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

    fun getCurrentLocationUpdate(){
        currentLocationCameraMovement(true)
    }

    fun setSelectionMode(latLng: LatLng){
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 11.0f)
        _cameraMovement.value =
            CameraMovement(cameraUpdate, true,500)
        _selectedLatLng.value = latLng
    }

    fun cameraIdle() {
        Log.d(TAG,"cameraIdle() called")
        _isCameraIdle.value = true
    }


    private fun currentLocationCameraMovement(animated:Boolean,duration: Int? = null){
        val location = _currentLocation.value
        location?.run {
            val latLng = LatLng(latitude,longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 11.0f)
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
    }


}
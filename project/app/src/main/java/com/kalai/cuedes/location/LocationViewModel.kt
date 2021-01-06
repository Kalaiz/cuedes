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
import com.google.android.gms.maps.model.Marker
import com.kalai.cuedes.location.Status.*
import java.time.Duration


@SuppressLint("MissingPermission")
class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)
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

    private val _status = MutableLiveData<Status>(NORMAL)
    val status:LiveData<Status> get() = _status

    private val _selectedMarker = MutableLiveData<Marker?>()
    val selectedMarker:LiveData<Marker?> get() = _selectedMarker

    private val _selectedRadius = MutableLiveData<Float>()
    val selectedRadius:LiveData<Float> get() = _selectedRadius

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

    fun setSelectionMode(){
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(_selectedLatLng.value, 11.0f)
        _cameraMovement.value =
            CameraMovement(cameraUpdate, true,500)
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

    fun setSelectedLocation(marker: Marker?) {
        Log.d(TAG,"setSelectdLocation")
        if(_status.value == SELECTION || _status.value == NORMAL && marker == null){
            Log.d(TAG,"SELECTION MODE")
            /* So that the removing animation occurs*/
            _selectedMarker.value = null
            /*Removing for the record*/
            _selectedLatLng.value = null
        }
        /*If in SELECTION mode, need to assist in animating the removal of marker ( as did above) and then animate the new one */
        if(marker!=null ){
            Log.d(TAG,"marker not null $marker")
        _selectedMarker.value = marker
        _selectedLatLng.value = marker.position
    }
    }

    fun setRadius(radius:Int){

    }


}
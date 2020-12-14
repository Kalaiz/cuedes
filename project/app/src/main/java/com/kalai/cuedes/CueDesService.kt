package com.kalai.cuedes

import android.annotation.SuppressLint
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng


class CueDesService: LifecycleService() {

   private lateinit var  currentLocation:LatLng

   companion object{

       private const val TAG = "CueDesService"

   }
   /*Temporary*/
   private val alarms by lazy { MutableLiveData<ArrayList<String>>() }


    private val fusedLocationClient: FusedLocationProviderClient by lazy{
       LocationServices.getFusedLocationProviderClient(this)
   }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /*   Retrieving and Converting Last Known Position */
        fusedLocationClient.lastLocation.result.apply{
            currentLocation = LatLng(latitude,longitude)
        }
        fusedLocationClient.lastLocation.addOnCompleteListener {

        }

        alarms.observe(this,
            Observer<ArrayList<String>> {
                list -> if(list.isEmpty()){
                    Log.d(TAG,"There isn't any alarms")
                  /*this.stopSelf() */
                }
            })
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }
}




package com.kalai.cuedes

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.model.LatLng


class CueDesService: Service() {

   private lateinit var  currentLocation:LatLng

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
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onBind(p0: Intent?): IBinder? {
         TODO("Not yet implemented")

     }
 }




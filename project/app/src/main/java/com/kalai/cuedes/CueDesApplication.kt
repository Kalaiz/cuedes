package com.kalai.cuedes

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.data.AlarmDatabase
import com.kalai.cuedes.data.AlarmRepository
import com.kalai.cuedes.location.selection.SelectionViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.DebugTree
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CueDesApplication: Application() {

    companion object{
        const val CHANNEL_ID = "562104"
    }


    private val database by lazy {  AlarmDatabase.getDatabase(this)  }
    val repository by lazy { AlarmRepository(database.alarmDao()) }
    val geofencingClient: GeofencingClient by lazy {  LocationServices.getGeofencingClient(this) }
    private val cueDesServiceIntent  by lazy {  Intent(this,CueDesService::class.java) }

    private val fusedLocationClient: FusedLocationProviderClient  by lazy { LocationServices.getFusedLocationProviderClient(this) }


    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        createNotificationChannel()
        GlobalScope.launch {
            repository.alarms.collect { alarms ->
                if(alarms.any { alarm -> alarm.isActivated  }){
                    startService(cueDesServiceIntent)
                }
                else
                    stopService(cueDesServiceIntent)
            }
        }
    }


    private val geofencePendingIntent: PendingIntent
        get() {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        return  PendingIntent.getBroadcast(this, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_ONE_SHOT)
    }

    /*Returns alarm if successful or else null*/
    fun insertIntoRepository(latLng:LatLng, radius:Int): Alarm {
        val name = "alarm"+repository.getCount()
        var alarm: Alarm? = null
            val latitude = latLng.latitude
            val longitude = latLng.longitude
            alarm = Alarm(name,latitude,longitude,radius)
            repository.insert(alarm)
        return  alarm
    }

    /*Returns alarm if successful or else null*/
    @SuppressLint("MissingPermission")
    /*TODO: Ask for permission if permission denied during runtime*/
    suspend fun setAlarm(alarm: Alarm?) : Alarm? = suspendCoroutine { cont->
        if(alarm == null){
            cont.resume(null)
        }
        else
            alarm.run{
                val geofence =  Geofence.Builder()
                        .setRequestId(name)
                        .setCircularRegion(latitude,longitude,radius.toFloat())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build()
                val geofencingRequest = GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofence(geofence)
                        .build()

                geofencingClient.addGeofences(geofencingRequest,geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        cont.resume(alarm)
                      Timber.d("Alarm set successfully")
                    }
                    addOnFailureListener {
                        cont.resume(null)
                       Timber.d("Alarm not set successfully ${it}")
                    }
                }
            }


    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("Notification channel created")
            val name = getString(R.string.app_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText

            }

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



    fun removeGeoFence(vararg alarm:Alarm ){
        geofencingClient.removeGeofences(alarm.map {it.name}.toMutableList())
    }

    fun removeGeoFence(alarmName:String){
        geofencingClient.removeGeofences(mutableListOf(alarmName))
    }
}


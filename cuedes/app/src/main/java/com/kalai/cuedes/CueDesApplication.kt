package com.kalai.cuedes

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Looper
import android.provider.MediaStore
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.data.AlarmDatabase
import com.kalai.cuedes.data.AlarmRepository
import com.kalai.cuedes.location.LocationFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import timber.log.Timber.DebugTree
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CueDesApplication: Application() {

    companion object {
        const val CHANNEL_ID = "562104"
        val VIBRATION_PATTERN = longArrayOf(250,500,500,750,500,1000,500,1250,500,1500,500,1750,500,2000)
        val RINGTONE_URI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }


    private val database by lazy { AlarmDatabase.getDatabase(this) }
    val repository by lazy { AlarmRepository(database.alarmDao()) }
    val geofencingClient: GeofencingClient by lazy { LocationServices.getGeofencingClient(this) }
    private val cueDesServiceIntent by lazy { Intent(this, CueDesService::class.java) }
    val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }


    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        createNotificationChannel()
        GlobalScope.launch {
            repository.alarms.collect { alarms ->
                if (alarms.any { alarm -> alarm.isActivated })
                    startService(cueDesServiceIntent)
                else
                    stopService(cueDesServiceIntent)

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
                enableLights(true)
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN
                setSound(RINGTONE_URI,AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setLegacyStreamType(AudioManager.STREAM_MUSIC).build())
            }

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun findAlarm(alarmName: String): Alarm? = suspendCoroutine { cont->
        CoroutineScope(Dispatchers.IO).launch{
            repository.alarms.collect { alarms->
                cont.resume(alarms.find { alarm -> alarm.name == alarmName })
                this.cancel()
            }
        }
    }



}


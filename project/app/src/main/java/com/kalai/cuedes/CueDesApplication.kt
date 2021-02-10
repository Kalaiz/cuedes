package com.kalai.cuedes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.kalai.cuedes.data.AlarmDatabase
import com.kalai.cuedes.data.AlarmRepository

class CueDesApplication: Application() {

    companion object{
        const val CHANNEL_ID = "0"
    }
    private val database by lazy {  AlarmDatabase.getDatabase(this)  }
    val repository by lazy { AlarmRepository(database.alarmDao()) }
    val geofencingClient by lazy {  LocationServices.getGeofencingClient(this) }

    init {
        createNotificationChannel()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

}


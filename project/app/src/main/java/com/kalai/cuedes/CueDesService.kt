package com.kalai.cuedes

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import timber.log.Timber
import kotlin.random.Random

class CueDesService : Service(), LocationListener{
    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    private lateinit var locationManager:LocationManager

    companion object{
        const val  CHANNEL_ID  = "ServiceNotificationChannel "
    }
    inner class LocalBinder : Binder() {
        internal val service: CueDesService
            get() = this@CueDesService
    }
    private val localBinder = LocalBinder()



    override fun onBind(intent: Intent): IBinder {
        return localBinder
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        locationManager =  getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5f, this);
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CueDes Service")
            .setContentText("Running")
            .setSmallIcon(R.drawable.ic_alarm)
            .build()

        startForeground(Int.MAX_VALUE,notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onLocationChanged(location: Location) {
        Timber.d("Location changed $location")
    }
}
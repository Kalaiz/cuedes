package com.kalai.cuedes

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.GeofencingEvent
import com.kalai.cuedes.CueDesApplication.Companion.CHANNEL_ID
import com.kalai.cuedes.entry.EntryActivity

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object{
        const val  TAG = "GeofenceReceiver"
    }

    private var notificationID:Int =0


    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Log.d(TAG,"Recived something ${geofencingEvent.triggeringLocation}" )

        val appIntent = Intent(context, MainActivity::class.java).apply {
            notificationID = System.currentTimeMillis().toInt()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            Log.d(TAG,"Putting notification ID")
            putExtra("notificationID",notificationID)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("You are about to reach your destination.")
                .setContentText("Press off to clear notification.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_back, "off",
                        pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, builder.build())
        }
        val triggeringGeofences = geofencingEvent.triggeringGeofences.map { it.requestId }
        (context.applicationContext as CueDesApplication).geofencingClient.removeGeofences(triggeringGeofences)
    }
}
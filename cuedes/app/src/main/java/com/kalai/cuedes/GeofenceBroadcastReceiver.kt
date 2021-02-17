package com.kalai.cuedes

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.GeofencingEvent
import com.kalai.cuedes.CueDesApplication.Companion.CHANNEL_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private var notificationID: Int = 0

    private lateinit var cueDesApplication: CueDesApplication
    private val serviceScope = CoroutineScope(Dispatchers.Main)


    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Timber.d("Received something ${geofencingEvent.triggeringLocation}")


        cueDesApplication = (context.applicationContext as CueDesApplication)


        var needAlarmSound: Boolean = false
        var needVibration: Boolean = false


        val alarmNames = geofencingEvent?.triggeringGeofences?.map { it?.requestId }

        alarmNames?.let {
            serviceScope.launch {
                loop@ for (alarmName in geofencingEvent.triggeringGeofences.map { it.requestId }) {
                    cueDesApplication.findAlarm(alarmName)?.run {
                        needAlarmSound = needAlarmSound.or(hasSound)
                        needVibration = needVibration.or(hasVibration)
                    }
                    if (needAlarmSound and needVibration)
                        break@loop
                }
            }.invokeOnCompletion {
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    notificationID = System.currentTimeMillis().toInt()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    Timber.d("Putting notification ID")
                    putExtra("notificationID", notificationID)
                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0)

                val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_alarm)
                        .setContentTitle("You are about to reach your destination${if (alarmNames.size > 1) "s" else ""}.")
                        .setContentText("Press off or slide to clear notification.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .addAction(R.drawable.ic_back, "off",
                                pendingIntent)


                if (needAlarmSound)
                    notificationBuilder.setSound(CueDesApplication.RINGTONE_URI)
                else
                    notificationBuilder?.setNotificationSilent()

                if (needVibration)
                    notificationBuilder?.setVibrate(CueDesApplication.VIBRATION_PATTERN)

                Timber.d("NeedVibration $needVibration NeedAlarmSound $needAlarmSound")

                with(NotificationManagerCompat.from(context)) {
                    notify(notificationID, notificationBuilder.build())
                }


                val triggeringGeofences = geofencingEvent.triggeringGeofences.map {
                    GlobalScope.launch {
                        cueDesApplication.repository.updateIsActivated(it.requestId, false)
                    }
                    it.requestId
                }

                Timber.d("Triggering Geofences $triggeringGeofences")
                cueDesApplication.geofencingClient.removeGeofences(triggeringGeofences)


            }
        }





    }
}
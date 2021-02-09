package com.kalai.cuedes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object{
        const val  TAG = "GeofenceReceiver"
    }
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Log.d(TAG,"Recived something ${geofencingEvent.triggeringLocation}" )
    }
}
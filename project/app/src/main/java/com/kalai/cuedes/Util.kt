package com.kalai.cuedes

import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

fun View.hide() {
    if (visibility == View.VISIBLE || isEnabled) {
        visibility = View.INVISIBLE
        isEnabled = false
    }
}

fun View.show(){
    if(visibility==View.INVISIBLE || !isEnabled){
        visibility= View.VISIBLE
        isEnabled = true
    }
}

fun Context?.isDevicePermissionGranted(permissionCodes: Array<String>):Boolean = if(this!=null)(permissionCodes.fold(true,
    { acc, it ->
        acc && ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    })) else false

fun GoogleApiAvailability.isGooglePlayServicesCompatible(context: Context?) = if (context!=null) isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS else false
package com.kalai.cuedes

import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat

public fun View.hide() {
    if (visibility == View.VISIBLE || isEnabled) {
        visibility = View.INVISIBLE
        isEnabled = false
    }
}

public fun View.show(){
    if(visibility==View.INVISIBLE || !isEnabled){
        visibility= View.VISIBLE
        isEnabled = true
    }
}

public fun Context.isDevicePermissionGranted(permissionCodes: Array<String>):Boolean = (permissionCodes.fold(true,
    { acc, it ->
        acc && ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }))
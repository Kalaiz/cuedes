package com.kalai.cuedes.location

import com.google.android.gms.maps.CameraUpdate

data class CameraMovement(var cameraUpdate:CameraUpdate, var animated:Boolean, var duration: Int? = null)
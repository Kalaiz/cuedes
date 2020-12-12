package com.kalai.cuedes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity
data class Alarm(@PrimaryKey var locationName:String,
                 @ColumnInfo(name = "longitude") var longitude:Double,
                 @ColumnInfo(name="latitude") var latitude:Double,
                 @ColumnInfo(name = "radius")var radius:Float)

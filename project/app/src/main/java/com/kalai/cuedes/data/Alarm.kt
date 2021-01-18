package com.kalai.cuedes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity
data class Alarm(@PrimaryKey var name:String,
                 @ColumnInfo(name="Latitude") var latitude:Double,
                 @ColumnInfo(name = "Longitude") var longitude:Double,
                 @ColumnInfo(name = "Radius")var radius:Int,
                 @ColumnInfo(name ="IsActivated") var isActivated:Boolean = true)


package com.kalai.cuedes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface AlarmDao {

    @Query("SELECT * FROM Alarm")
    fun getAll(): Flow<List<Alarm>>


    @Insert
    fun insertAll(vararg alarms:Alarm)

    @Query("UPDATE Alarm SET IsActivated = :isActivated  WHERE name = :alarmName")
    fun updateIsActivated(alarmName:String,isActivated:Boolean)

    @Delete
    fun delete(alarm:Alarm)

    @Query("SELECT COUNT(name) FROM Alarm")
    fun count() : Long

    @Query("DELETE FROM Alarm")
    fun deleteAll()

}
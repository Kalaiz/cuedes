package com.kalai.cuedes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface AlarmDao {

    @Query("SELECT * FROM Alarm")
    fun getAll(): Flow<List<Alarm>>


    @Insert
    fun insertAll(vararg alarms:Alarm)


    @Delete
    fun delete(alarm:Alarm)

}
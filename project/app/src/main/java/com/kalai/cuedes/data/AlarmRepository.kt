package com.kalai.cuedes.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val alarmDao: AlarmDao) {

    val alarms:Flow<List<Alarm>> = alarmDao.getAll()


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(alarm:Alarm) {
        alarmDao.insertAll(alarm)}

}
package com.kalai.cuedes.data

import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val alarmDao: AlarmDao) {

    val alarms:Flow<List<Alarm>> = alarmDao.getAll()

    fun insert(alarm:Alarm)
            = alarmDao.insertAll(alarm)

    fun updateIsActivated(alarmName:String,isActivated:Boolean)
            = alarmDao.updateIsActivated(alarmName,isActivated)

    fun delete(alarm:Alarm)
            = alarmDao.delete(alarm)


    fun getCount()
            =  alarmDao.count()

    fun deleteAll()
            = alarmDao.deleteAll()

}
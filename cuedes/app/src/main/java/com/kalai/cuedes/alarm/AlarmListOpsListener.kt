package com.kalai.cuedes.alarm

interface AlarmListOpsListener {

  suspend fun updateIsActivated(alarmName: String,isActivated:Boolean):Boolean
   fun deleteAlarm(alarmName: String)

}
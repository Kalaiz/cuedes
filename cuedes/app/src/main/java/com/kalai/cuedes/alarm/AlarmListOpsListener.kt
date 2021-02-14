package com.kalai.cuedes.alarm

interface AlarmListOpsListener {

   fun updateIsActivated(alarmName: String,isActivated:Boolean)
   fun deleteAlarm(alarmName: String)

}
package com.kalai.cuedes.alarm

import com.kalai.cuedes.data.Alarm

interface AlarmListOpsListener {
   fun removeAlarm(alarm: Alarm)
   fun updateIsActivated(isActivated:Boolean)

}
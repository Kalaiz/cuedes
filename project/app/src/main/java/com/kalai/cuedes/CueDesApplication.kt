package com.kalai.cuedes

import android.app.Application
import com.kalai.cuedes.data.AlarmDatabase
import com.kalai.cuedes.data.AlarmRepository

class CueDesApplication: Application() {
    private val database by lazy {  AlarmDatabase.getDatabase(this)  }
    val repository by lazy { AlarmRepository(database.alarmDao()) }

}


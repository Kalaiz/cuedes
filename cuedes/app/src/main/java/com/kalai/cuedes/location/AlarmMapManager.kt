package com.kalai.cuedes.location

import com.kalai.cuedes.data.Alarm
/**
 * Manage alarm UI;
 * fetch alarms
 * fetch the circle UIs
 * upload circle configs so that Fragment can draw it
 * observe changes to the alarms and emit changes accordingly*/
class AlarmMapManager private constructor() {

    /*TODO: Transferring alarm - map related data/logic here*/

    companion object{
        val instance = AlarmMapManager()
        fun create(alarms: MutableList<Alarm>):AlarmMapManager
                = instance.apply { initialise(alarms) }
    }


    private fun initialise(alarms: MutableList<Alarm>){
        this.alarms.addAll(alarms)
    }

    private val alarms:MutableList<Alarm> = mutableListOf()



    /*Need to call when app process is killed*/
    fun clear(){

    }

}
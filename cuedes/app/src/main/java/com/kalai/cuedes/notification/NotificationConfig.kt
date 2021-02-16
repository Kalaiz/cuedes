package com.kalai.cuedes.notification

class NotificationConfig
private constructor(val type: Notification,
                    val message: String,
                    var resourceId: Int?,
                    var duration: Int?){

    object Builder{
        private lateinit var type: Notification
        private lateinit var message:String
        private var resourceId: Int? = null
        private var duration:Int? = null

        fun create(type: Notification, message:String): Builder {
            Builder.type = type
            Builder.message = message
            return  this
        }

        fun addIcon(resourceId:Int): Builder {
            Builder.resourceId = resourceId
            return  this
        }

        fun setDuration(duration:Int): Builder {
            Builder.duration = duration
            return  this
        }

        fun build() = NotificationConfig(type, message, resourceId, duration)

    }



}
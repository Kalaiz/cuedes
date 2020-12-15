package com.kalai.cuedes

import android.view.View

        public fun View.hide(){
            visibility= View.INVISIBLE
            isEnabled=false
        }

        public fun View.show(){
            visibility= View.VISIBLE
            isEnabled=true
        }


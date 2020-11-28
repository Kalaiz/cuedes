package com.kalai.cuedes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val  currentFragmentLiveData = MutableLiveData<Int>(0)


}
package com.kalai.cuedes.entry.onboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class OnBoardViewModel :ViewModel(){
    private val  _isPageNavigationViewable = MutableLiveData<EnumMap<PageContent,Boolean>>(EnumMap(PageContent::class.java))

    fun getIsPageNavigationViewable(): LiveData<EnumMap<PageContent,Boolean>> = _isPageNavigationViewable

    fun updateIsPageNavigationViewable(fragmentKey:PageContent,value:Boolean){
        val map = _isPageNavigationViewable.value
            map?.run {
                put(fragmentKey,value)
                _isPageNavigationViewable.value = this }

    }
}
package com.kalai.cuedes.entry.onboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.kalai.cuedes.entry.onboard.PageContent.*


class OnBoardPagerAdapter(fragmentActivity: FragmentActivity, private val numOfTabs: Int) : FragmentStateAdapter(fragmentActivity) {

    companion object{
        val PAGE = mapOf(
            0 to INTRO, 1 to PLAY_SERVICE,
            2 to DEVICE_PERMISSION,3 to GOOGLE_LOCATION_SERVICE_PERMISSION,
            4 to END
        )
    }

    override fun getItemCount(): Int = numOfTabs

    override fun createFragment(position: Int): Fragment {
        return when (PAGE[position]){
            INTRO -> IntroFragment()
            PLAY_SERVICE -> PlayServiceFragment()
            DEVICE_PERMISSION -> DevicePermissionFragment()
            GOOGLE_LOCATION_SERVICE_PERMISSION -> LocationServicePermissionFragment()
            else-> EndFragment()

        }
    }
}
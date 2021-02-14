package com.kalai.cuedes

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kalai.cuedes.alarm.AlarmListFragment
import com.kalai.cuedes.location.LocationFragment
import com.kalai.cuedes.settings.SettingsFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity, private val numOfTabs: Int) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = numOfTabs

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> LocationFragment()
            1 -> AlarmListFragment()
            else -> SettingsFragment()
    }
}


}
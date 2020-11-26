package com.kalai.cuedes.alarm

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kalai.cuedes.dashboard.DashboardFragment
import com.kalai.cuedes.settings.SettingsFragment

class PagerAdapter(fragmentActivity: FragmentActivity, private val numOfTabs: Int) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = numOfTabs

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> DashboardFragment()
            1 -> AlarmListFragment()
            else -> SettingsFragment()
    }
}


}
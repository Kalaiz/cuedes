package com.kalai.cuedes

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(fragmentActivity: FragmentActivity, private val numOfTabs: Int) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = numOfTabs

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> DashboardFragment()
            1 -> ListFragment()
            else -> SettingsFragment()
    }
}


}
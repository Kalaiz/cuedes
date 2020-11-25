package com.kalai.cuedes

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.kalai.cuedes.databinding.ActivityMainBinding


class MainActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager : ViewPager2
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var tabLayout: TabLayout
    private val viewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewPager = binding.mainPager
        tabLayout = binding.mainTabLayout
        pagerAdapter = PagerAdapter(this,tabLayout.tabCount)
        viewPager.adapter = pagerAdapter

        tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
             viewPager.currentItem = tab?.position ?: 0
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })


    }
}
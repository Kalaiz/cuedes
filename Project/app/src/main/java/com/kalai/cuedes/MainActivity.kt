package com.kalai.cuedes

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kalai.cuedes.alarm.PagerAdapter
import com.kalai.cuedes.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewPager = binding.viewPager
        bottomNavigationView = binding.bottomNavigationView
        pagerAdapter = PagerAdapter(this, bottomNavigationView.menu.size())
        viewPager.adapter = pagerAdapter
        viewPager.isUserInputEnabled = false

        bottomNavigationView.setOnNavigationItemSelectedListener { selectedItem ->
            viewPager.currentItem = when (selectedItem.itemId) {
                R.id.action_locate -> 0
                R.id.action_alarms -> 1
                else -> 2
            }
            return@setOnNavigationItemSelectedListener true
        }

    }
}



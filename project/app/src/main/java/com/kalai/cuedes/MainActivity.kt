package com.kalai.cuedes

import android.animation.ObjectAnimator
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.libraries.places.api.Places
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kalai.cuedes.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var mainPagerAdapter: MainPagerAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private val sharedViewModel: SharedViewModel by viewModels()

    companion object{
        private const val TAG =  "MainActivity" }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

            with(NotificationManagerCompat.from(this)) {
                cancelAll()

        }
        viewPager = binding.viewPager
        bottomNavigationView = binding.bottomNavigationView
        mainPagerAdapter = MainPagerAdapter(this, bottomNavigationView.menu.size())
        viewPager.adapter = mainPagerAdapter


        /*Initialising Places*/
        applicationContext?.getString(R.string.google_maps_key)?.let { Places.initialize(applicationContext, it) }

        /* Pre - fetching Fragments in advance */
        viewPager.offscreenPageLimit = 1

        /*Disabling Swipes*/
        viewPager.isUserInputEnabled = false

        bottomNavigationView.setOnNavigationItemSelectedListener { selectedItem ->
            Log.d(TAG,"BottomNavBar Item $selectedItem ")
            if(selectedItem.itemId != bottomNavigationView.selectedItemId) {
                val selectedId = when (selectedItem.itemId) {
                    R.id.action_locate -> 0
                    R.id.action_alarms -> 1
                    else -> 2
                }

                val fadeIn = ObjectAnimator.ofFloat(viewPager, "alpha", 0.4f).apply {
                    duration = 150
                }
                val fadeOut = ObjectAnimator.ofFloat(viewPager, "alpha", 1f).apply {
                    duration = 250
                }

                fadeIn.start()
                fadeIn?.doOnEnd {
                    viewPager.setCurrentItem(selectedId, false)
                    fadeOut?.start()
                }
            }
            return@setOnNavigationItemSelectedListener true
        }




    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG,"onBackPressed")
        if(!onBackPressedDispatcher.hasEnabledCallbacks()){
            finishAndRemoveTask()
        }
    }


}


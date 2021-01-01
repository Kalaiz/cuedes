package com.kalai.cuedes

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
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
        val view = binding.root
        setContentView(view)

        viewPager = binding.viewPager
        bottomNavigationView = binding.bottomNavigationView
        mainPagerAdapter = MainPagerAdapter(this, bottomNavigationView.menu.size())
        viewPager.adapter = mainPagerAdapter

        /* Pre - fetching Fragments in advance */
        viewPager.offscreenPageLimit = 1

        /*Disabling Swipes*/
        viewPager.isUserInputEnabled = false

        bottomNavigationView.setOnNavigationItemSelectedListener { selectedItem ->
            Log.d(TAG,"BottomNavBar Item $selectedItem ")
            val selectedId = when (selectedItem.itemId) {
                R.id.action_locate -> 0
                R.id.action_alarms -> 1
                else -> 2 }


            val fadeIn = ObjectAnimator.ofFloat(viewPager, "alpha", 0.4f).apply {
                duration = 150 }
            val fadeOut = ObjectAnimator.ofFloat(viewPager, "alpha", 1f).apply {
                duration = 250 }

            fadeIn.start()
            fadeIn?.doOnEnd {
                viewPager.setCurrentItem(selectedId,false)
                fadeOut?.start() }

            return@setOnNavigationItemSelectedListener true
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG,"onBackPressed")
        if(!onBackPressedDispatcher.hasEnabledCallbacks()){
            finish()
        }
    }

    private fun startCueDesService(){
        val cueDesServiceIntent= Intent(this, CueDesService::class.java)
        ContextCompat.startForegroundService(this, cueDesServiceIntent)
    }
}


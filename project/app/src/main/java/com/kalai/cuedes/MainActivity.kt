package com.kalai.cuedes

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kalai.cuedes.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private val sharedViewModel: SharedViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewPager = binding.viewPager
        bottomNavigationView = binding.bottomNavigationView
        pagerAdapter = PagerAdapter(this, bottomNavigationView.menu.size())
        viewPager.adapter = pagerAdapter

        /*Pre - fetching Fragments in advance */
        viewPager.offscreenPageLimit = 1

        /*Disabling Swipes*/
        viewPager.isUserInputEnabled = false


        bottomNavigationView.setOnNavigationItemSelectedListener { selectedItem ->
            viewPager.currentItem = when (selectedItem.itemId) {
                R.id.action_locate -> 0
                R.id.action_alarms -> 1
                else -> 2
            }
            return@setOnNavigationItemSelectedListener true
        }



        /*TODO: Need to handle lifecycle properly*/
        /*startCueDesService()*/


    }

    private fun startCueDesService(){
        val cueDesServiceIntent= Intent(this, CueDesService::class.java)
        ContextCompat.startForegroundService(this, cueDesServiceIntent)
    }
}



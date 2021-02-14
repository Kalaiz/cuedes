package com.kalai.cuedes


import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.androidadvance.topsnackbar.TSnackbar
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.kalai.cuedes.databinding.ActivityMainBinding
import timber.log.Timber


/*TODO need to migrate to timber*/
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
        applicationContext?.getString(R.string.google_maps_key)?.let { Places.initialize(
            applicationContext,
            it
        ) }

        /* Pre - fetching Fragments in advance */
        viewPager.offscreenPageLimit = 1

        /*Disabling Swipes*/
        viewPager.isUserInputEnabled = false

        bottomNavigationView.setOnNavigationItemSelectedListener { selectedItem ->
            Log.d(TAG, "BottomNavBar Item $selectedItem ")
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

        sharedViewModel.error.observe(this, Observer { errorMessage ->
            Timber.d(errorMessage)
            if (errorMessage != null)
                showErrorSnackBar(errorMessage)

        })


    }

    private fun showErrorSnackBar(errorMessage: String) {

        /*TODO: Changing it to native one*/
        val snackBar = TSnackbar.make(
            binding.coordinatorLayout,
            errorMessage,
            TSnackbar.LENGTH_LONG
        )
        snackBar.setActionTextColor(Color.WHITE)
        val snackBarView: View = snackBar.view
        snackBarView.setBackgroundColor(getColor(R.color.error))
        snackBarView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        val textView = snackBarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
        textView.setTextColor(getColor(R.color.colorLight))
        textView.gravity = TextView.TEXT_ALIGNMENT_CENTER
        snackBar.show()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG, "onBackPressed")
        if(!onBackPressedDispatcher.hasEnabledCallbacks()){
            finishAndRemoveTask()
        }
    }


}


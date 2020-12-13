package com.kalai.cuedes

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.kalai.cuedes.databinding.ActivityMainBinding


private val PERMISSION_CODES = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
private const val REQ_CODE = 1

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

        /*Getting  Relevant Permissions*/
        if ((PERMISSION_CODES.fold(false,
                {acc,it-> acc || ActivityCompat.checkSelfPermission(this,it)!= PackageManager.PERMISSION_GRANTED})))
        {
            getPermissions()
            return
        }

        /*TODO: Need to handle lifecycle properly*/
        /*startCueDesService()*/


    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            REQ_CODE -> if(grantResults.isEmpty() || (grantResults.any { it == PackageManager.PERMISSION_DENIED }))
            {
                Snackbar.make(binding.root,getString(R.string.main_permission_msg),Snackbar.LENGTH_LONG).show()
                getPermissions()
            }

        }
    }

    private fun getPermissions(){
        ActivityCompat.requestPermissions(this,PERMISSION_CODES,REQ_CODE)
    }


    private fun startCueDesService(){
        val cueDesServiceIntent= Intent(this,CueDesService::class.java)
        ContextCompat.startForegroundService(this,cueDesServiceIntent)
    }
}



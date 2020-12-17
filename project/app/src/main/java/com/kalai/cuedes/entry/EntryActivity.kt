package com.kalai.cuedes.entry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.kalai.cuedes.MainActivity
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.ActivityEntryBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class EntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntryBinding

    companion object{
        private const val TAG = "EntryActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataStore = applicationContext?.createDataStore("settings")
        val isOnBoardingKey = preferencesKey<Boolean>("isOnBoard")
        val isOnBoardFlow = dataStore?.data?.map { preferences -> preferences[isOnBoardingKey] ?: true }
        lifecycleScope.launch {
            if(isOnBoard(isOnBoardFlow)){
            supportFragmentManager.commit {
                val onBoardFragment = OnBoardFragment()
                add(R.id.entry_fragment_container_view,onBoardFragment)
            }
        }
            else{
                val intent = Intent(this@EntryActivity,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*OnBoardFragment's startResolutionForResult (of Play Service API) not calling the fragment's onActivityResult,
        despite the fragment onActivityResult calling it's superclass's onActivityResult */
        supportFragmentManager.fragments.forEach { fragment-> fragment.onActivityResult(requestCode,resultCode,data) }
        Log.d(TAG,"onActivityResult")
    }

    private suspend fun isOnBoard(isOnBoardFlow: Flow<Boolean>?): Boolean = isOnBoardFlow?.first()?:true
}


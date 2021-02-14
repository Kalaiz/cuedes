package com.kalai.cuedes.entry

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.kalai.cuedes.MainActivity
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.ActivityEntryBinding
import com.kalai.cuedes.entry.onboard.OnBoardFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class EntryActivity : AppCompatActivity() {

    companion object{ private const val TAG = "EntryActivity" }

    private lateinit var binding: ActivityEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataStore = applicationContext?.createDataStore("settings")
        val isOnBoardingKey = booleanPreferencesKey("isOnBoard")
        val isOnBoardFlow = dataStore?.data?.map { preferences -> preferences[isOnBoardingKey] ?: true }
        lifecycleScope.launch {
            if(isOnBoard(isOnBoardFlow)){
            supportFragmentManager.commit {
                val onBoardFragment =
                    OnBoardFragment()
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

    private suspend fun isOnBoard(isOnBoardFlow: Flow<Boolean>?): Boolean = isOnBoardFlow?.first()?:true
}


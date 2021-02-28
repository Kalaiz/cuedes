package com.kalai.cuedes.settings


import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kalai.cuedes.R
import com.kalai.cuedes.SharedViewModel


class SettingsFragment : PreferenceFragmentCompat() {

    private val sharedViewModel: SharedViewModel by activityViewModels()


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val clearAlarmPreference: Preference = findPreference("clear_alarm")

        clearAlarmPreference.setOnPreferenceClickListener {
             sharedViewModel.clearAllAlarms()
            true
        }

    }


}
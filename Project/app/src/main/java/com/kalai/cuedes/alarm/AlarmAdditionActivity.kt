package com.kalai.cuedes.alarm

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.ActivityAlarmAdditionBinding

class AlarmAdditionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmAdditionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmAdditionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*Initialising Spinner Adapter*/
        val spinnerAdapter =  ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,DistanceUnit.values())
        binding.spinner.adapter= spinnerAdapter


        binding.buttonGetDestination.setOnClickListener {

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                // Replace whatever is in the fragment_container view with this fragment
                add(R.id.frameLayoutContainer,DestinationSelectionFragment())
            }
        }

    }
}
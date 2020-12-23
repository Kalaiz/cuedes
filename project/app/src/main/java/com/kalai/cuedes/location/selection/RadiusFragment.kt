package com.kalai.cuedes.location.selection

import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.SpinnerAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionRadiusBinding
import com.kalai.cuedes.location.DistanceUnit

class RadiusFragment:Fragment() {

    private lateinit var  binding: FragmentSelectionRadiusBinding

    companion object{
        private const val TAG = "RadiusFragment"
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionRadiusBinding.inflate(layoutInflater, container, false)
        binding.nextButton.setOnClickListener{
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                hide(parentFragmentManager.fragments.last())
                add(R.id.selection_fragment_container_view,NotificationMethodFragment(),"NotificationMethodFragment")
                addToBackStack(null)
            }
        }


        binding.radiusSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {


            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d(TAG,"Progress changed ${seekBar?.progress}")
            }

        })

        return binding.root
    }



}
package com.kalai.cuedes.location.selection

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.slider.Slider
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionRadiusBinding

class RadiusFragment:Fragment() {

    private lateinit var  binding: FragmentSelectionRadiusBinding
    private val selectionViewModel: SelectionViewModel by viewModels({ requireParentFragment() })

    companion object{
        private const val TAG = "RadiusFragment"
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionRadiusBinding.inflate(layoutInflater, container, false)


        selectionViewModel.selectedRadius.observe(viewLifecycleOwner, Observer {radius->
            binding.radiusTextView.text = radius.toInt().toString()
            binding.radiusSlider.value = radius.toFloat()})

        binding.addImageButton.setOnClickListener {
            selectionViewModel.selectedRadius.value?.let { radius ->
                if(radius+1 <= binding.radiusSlider.valueTo)
                    selectionViewModel.setRadius(radius+1) }
        }

        binding.minusImageButton.setOnClickListener {
            selectionViewModel.selectedRadius.value?.let { radius ->
                if(radius-1 >= binding.radiusSlider.valueFrom)
                    selectionViewModel.setRadius(radius-1) }
        }
        binding.nextButton.setOnClickListener{
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                hide(parentFragmentManager.fragments.last())
                add(R.id.selection_fragment_container_view,NotificationMethodFragment(),"NotificationMethodFragment")
                addToBackStack(null)
            }
        }


        binding.radiusSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                Log.d(TAG,"Progress changed ${slider.value}")
                selectionViewModel.setRadius(slider.value.toInt() )
            }
        })
        return binding.root
    }



}
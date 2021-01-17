package com.kalai.cuedes.location.selection

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.slider.Slider
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionBinding
import com.kalai.cuedes.location.LocationFragment
import com.kalai.cuedes.location.LocationViewModel


class SelectionFragment : DialogFragment() {


    companion object {
        const val TAG = "SelectionFragment"
    }


    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var binding: FragmentSelectionBinding
    private val locationViewModel: LocationViewModel by viewModels({requireParentFragment()})
    private val selectionViewModel: SelectionViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionBinding.inflate(inflater, container, false)


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

        binding.radiusSlider.addOnChangeListener { _, value, _ ->
            binding.radiusTextView.text = value.toInt().toString()
        }

        binding.radiusSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }
            override fun onStopTrackingTouch(slider: Slider) {
                Log.d(TAG,"Progress changed ${slider.value}")
                selectionViewModel.setRadius(slider.value.toInt() )
            }
        })

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onViewCreated")



        locationViewModel.selectedRadius.observe(viewLifecycleOwner,object :Observer< Int?> {
            override fun onChanged(radius: Int?) {
                Log.d(TAG,"Updating selectedRadius to $radius")
                if (radius != null && selectionViewModel.selectedRadius.value != radius) {
                    selectionViewModel.setRadius(radius)
                    locationViewModel.selectedRadius.removeObserver(this)
                }
            }
        }
        )

        locationViewModel.selectedLatLng.observe(viewLifecycleOwner, Observer {
                updatedLatLng-> updatedLatLng?.let{selectionViewModel.setLatLng(it)}
        })

        selectionViewModel.selectedRadius.observe(viewLifecycleOwner,Observer{
                updatedRadius ->
            Log.d(TAG,"Updating selectedRadius in SelectionViewModel to $updatedRadius")
            if(updatedRadius != locationViewModel.selectedRadius.value)
            updatedRadius?.let { locationViewModel.setRadius(updatedRadius) }
        })

        binding.cancelButton.setOnClickListener {
            endSelection(false)
        }

        binding.startButton.setOnClickListener {
            endSelection(true)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"onDestroy")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                Log.d(TAG,"OnBackPressed")
                endSelection(false)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback
        )
    }


    private fun endSelection(isSuccessful:Boolean){
        Log.d(TAG,"Going to be dismissed")
        val result = bundleOf("Successful" to isSuccessful)
        /* TODO need to make req key const*/
        parentFragment?.parentFragmentManager?.setFragmentResult("LocationFragmentReqKey",result)
        onBackPressedCallback.remove()
        this@SelectionFragment.onDestroy()
    }

}






package com.kalai.cuedes.location.selection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.kalai.cuedes.SharedViewModel
import com.kalai.cuedes.databinding.FragmentSelectionBinding
import com.kalai.cuedes.location.LocationViewModel
import kotlinx.coroutines.launch
import timber.log.Timber


class SelectionFragment : DialogFragment() {

    companion object{
        const val TAG = "SelectionFragment"
    }

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var binding: FragmentSelectionBinding
    private val locationViewModel: LocationViewModel by viewModels({requireParentFragment()})
    private val selectionViewModel: SelectionViewModel by viewModels()
    private val sharedViewModel:SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectionBinding.inflate(inflater, container, false)



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
                Timber.d("Progress changed ${slider.value}")
                selectionViewModel.setRadius(slider.value.toInt() )
            }
        })

        return binding.root
    }


    /*TODO moving logic (ifs to viewmodel)*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")

        selectionViewModel.selectedRadius.observe(viewLifecycleOwner, Observer {radius->
            binding.radiusTextView.text = radius.toInt().toString()
            binding.radiusSlider.value = radius.toFloat()})

        locationViewModel.selectedRadius.observe(viewLifecycleOwner,object :Observer< Int?> {
            override fun onChanged(radius: Int?) {
                Timber.d("Updating selectedRadius to $radius")
                if (radius != null && selectionViewModel.selectedRadius.value != radius) {
                    selectionViewModel.setRadius(radius)
                    locationViewModel.selectedRadius.removeObserver(this)
                }
            } })

        locationViewModel.selectedLatLng.observe(viewLifecycleOwner, Observer {
                updatedLatLng-> updatedLatLng?.let{selectionViewModel.setLatLng(it)}
        })

        selectionViewModel.selectedRadius.observe(viewLifecycleOwner,Observer{
                updatedRadius ->
            Timber.d("Updating selectedRadius in SelectionViewModel to $updatedRadius")
            if(updatedRadius != locationViewModel.selectedRadius.value)
                updatedRadius?.let { locationViewModel.setRadius(updatedRadius) }
        })

        binding.cancelButton.setOnClickListener {
            endSelection(false) }

        binding.startButton.setOnClickListener {
            selectionViewModel.createAlarm() }

        selectionViewModel.alarm.observe(this, Observer {
                alarm->
            if(alarm!=null) {
                locationViewModel.addAlarm(alarm)
                lifecycleScope.launch {
                    sharedViewModel.processAlarm(alarm) }.invokeOnCompletion {
                    endSelection(it == null,alarm.isActivated)
                }
            }
            else
                endSelection(false)
        })

        updateSelection()

        binding.vibrationToggleButton.setOnCheckedChangeListener { _, isChecked ->
            selectionViewModel.updateNeedVibration(isChecked)
        }
        binding.speakerToggleButton.setOnCheckedChangeListener { _, isChecked ->
            selectionViewModel.updateNeedSound(isChecked)
        }
    }

    private fun updateSelection() {
        selectionViewModel.updateNeedSound(binding.speakerToggleButton.isChecked)
        selectionViewModel.updateNeedVibration(binding.vibrationToggleButton.isChecked)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy") }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                Timber.d("OnBackPressed")
                endSelection(false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback
        )
    }



    private fun endSelection(isSuccessful:Boolean, isActivated:Boolean = false){
        Timber.d("Going to be dismissed")
        val result = bundleOf("Successful" to isSuccessful,"Activated" to isActivated)

        /* TODO need to make req key const*/
        parentFragment?.parentFragmentManager?.setFragmentResult("LocationFragmentReqKey",result)
        onBackPressedCallback.remove()
        this@SelectionFragment.onDestroy()
    }

}






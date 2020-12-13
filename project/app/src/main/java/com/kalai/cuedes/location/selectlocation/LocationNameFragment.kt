package com.kalai.cuedes.location.selectlocation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kalai.cuedes.databinding.FragmentSelectionLocationNameBinding

class LocationNameFragment :Fragment(){

    private lateinit var  binding:FragmentSelectionLocationNameBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionLocationNameBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}
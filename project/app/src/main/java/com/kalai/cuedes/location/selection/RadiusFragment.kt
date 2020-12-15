package com.kalai.cuedes.location.selection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionRadiusBinding

class RadiusFragment:Fragment() {

    private lateinit var  binding: FragmentSelectionRadiusBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionRadiusBinding.inflate(layoutInflater, container, false)
        binding.nextButton.setOnClickListener{
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                hide(parentFragmentManager.fragments.last())
                add(R.id.selectionFragmentContainerView,NotificationMethodFragment(),"NotificationMethodFragment")
                addToBackStack(null)
            }
        }
        return binding.root
    }



}
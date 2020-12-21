package com.kalai.cuedes.location.selection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit
import com.kalai.cuedes.databinding.FragmentSelectionNotificationBinding


class NotificationMethodFragment :Fragment(){

    private lateinit var  binding: FragmentSelectionNotificationBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionNotificationBinding.inflate(layoutInflater, container, false)
        binding.nextButton.setOnClickListener{
            parentFragmentManager.popBackStack(null, POP_BACK_STACK_INCLUSIVE)
        }
        return binding.root
    }
}
package com.kalai.cuedes.location.selection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionLocationNameBinding


class LocationNameFragment :Fragment(){


    companion object{
        private const val TAG = "LocationNameFragment"
    }
    private lateinit var binding:FragmentSelectionLocationNameBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionLocationNameBinding.inflate(layoutInflater, container, false)
        binding.nextButton.setOnClickListener{
            parentFragmentManager.commit {
                Log.d(TAG,parentFragmentManager.fragments.toString())
                setReorderingAllowed(true)
                hide(parentFragmentManager.fragments.last())
                add(R.id.selectionFragmentContainerView,RadiusFragment(),"RadiusFragment")
                addToBackStack(null)
            }
        }





        return binding.root
    }

}
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
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionBinding


class SelectionFragment : DialogFragment() {


    companion object {
        private const val TAG = "SelectionFragment"
    }


    private lateinit var binding: FragmentSelectionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionBinding.inflate(inflater, container, false)
        childFragmentManager.commit {
            setReorderingAllowed(true)
            add(
                R.id.selection_fragment_container_view,
                LocationNameFragment(),
                "LocationFragment"
            )
            addToBackStack(null)

        }
        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.addOnBackStackChangedListener {
            if(childFragmentManager.backStackEntryCount == 0){
                val result = bundleOf("Successful" to true)
                /*TODO need to make req key const*/

                parentFragment?.parentFragmentManager?.setFragmentResult("LocationFragmentReqKey",result)
                this@SelectionFragment.onDestroy()
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                Log.d(TAG,"OnBackPressed")

                if (childFragmentManager.fragments.size == 1) {
                    Log.d(TAG,"Going to be dismissed")
                    val result = bundleOf("Successful" to false)
                    /*TODO need to make req key const*/

                    parentFragment?.parentFragmentManager?.setFragmentResult("LocationFragmentReqKey",result)
                    this@SelectionFragment.onDestroy()
                }
                else {
                    childFragmentManager.popBackStack()
                    childFragmentManager.commit {
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        show(childFragmentManager.fragments.last())
                    }
                }

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }


}






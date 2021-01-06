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
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionBinding
import com.kalai.cuedes.location.LocationViewModel


class SelectionFragment : DialogFragment() {


    companion object {
        private const val TAG = "SelectionFragment"
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
                /*Last child fragment will clear the backstack */
                val result = bundleOf("Successful" to true)
                /*TODO need to make req key const*/

                parentFragment?.parentFragmentManager?.setFragmentResult("LocationFragmentReqKey",result)
                onBackPressedCallback.remove()

            }

        }
        locationViewModel.selectedLatLng.observe(viewLifecycleOwner, Observer {
                updatedLatLng-> updatedLatLng?.let{selectionViewModel.updateSelectedLatLng(it)}
        })

        selectionViewModel.selectedRadius.observe(viewLifecycleOwner,Observer{
            updatedRadius -> updatedRadius?.let { locationViewModel.setRadius(updatedRadius) }
        })

    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                Log.d(TAG,"OnBackPressed")

                if (childFragmentManager.fragments.size == 1) {
                    Log.d(TAG,"Going to be dismissed")
                    val result = bundleOf("Successful" to false)
                    /*TODO need to make req key const*/

                    parentFragment?.parentFragmentManager?.setFragmentResult("LocationFragmentReqKey",result)
                    onBackPressedCallback.remove()
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
            onBackPressedCallback
        )
    }


}






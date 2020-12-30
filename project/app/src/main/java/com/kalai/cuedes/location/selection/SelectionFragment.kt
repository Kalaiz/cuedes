package com.kalai.cuedes.location.selection

import android.app.Dialog
import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionBinding

class SelectionFragment : BottomSheetDialogFragment() {


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


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialogFragment = super.onCreateDialog(savedInstanceState)
        bottomSheetDialogFragment.setOnShowListener {
            val bottomSheet = bottomSheetDialogFragment.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let { BottomSheetBehavior.from(bottomSheet).apply { isHideable = false
                state =  BottomSheetBehavior.STATE_EXPANDED

            } }

            bottomSheetDialogFragment.setCanceledOnTouchOutside(false)



        }
        Log.d(TAG,dialog?.window?.enterTransition?.duration.toString())
        Log.d(TAG,  ((this as DialogFragment).enterTransition as Transition?)?.name.toString())
        childFragmentManager.addOnBackStackChangedListener {
            if(childFragmentManager.backStackEntryCount == 0){
                dialog?.dismiss()
            }
        }



        bottomSheetDialogFragment.setOnKeyListener{ dialogInterface, i, keyEvent ->
            if(keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                Log.d(TAG, "onBackPressed()" + childFragmentManager.backStackEntryCount + i)
                if (childFragmentManager.fragments.size == 1) {
                    Log.d(TAG,"Going to be dismissed")
                    val result = bundleOf("Successful" to false)
                    /*TODO need to make req key const*/
                    parentFragment?.parentFragmentManager?.setFragmentResult("LocationFragmentReqKey",result)
                    dialogInterface.dismiss()
                } else {
                    childFragmentManager.popBackStack()
                    childFragmentManager.commit {
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        show(childFragmentManager.fragments.last())
                    }
                }
            }

            true
        }
        return bottomSheetDialogFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"OnViewCreated")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
}






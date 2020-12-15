package com.kalai.cuedes.location.selection

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionBinding


class SelectionFragment : BottomSheetDialogFragment(){

    companion object{
        private const val TAG = "BottomSheetFragment"
    }



    private lateinit var binding:FragmentSelectionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionBinding.inflate(inflater,container,false)


        childFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.selectionFragmentContainerView,LocationNameFragment(),"LocationFragment")
            addToBackStack(null)
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialogFragment = super.onCreateDialog(savedInstanceState)
        bottomSheetDialogFragment.setOnShowListener {
            val bottomSheet =
                bottomSheetDialogFragment.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { BottomSheetBehavior.from(bottomSheet).apply { isHideable = false }
            }
        }
        bottomSheetDialogFragment.setCanceledOnTouchOutside(false)

        bottomSheetDialogFragment.setOnKeyListener{ dialogInterface, i, keyEvent ->
            if(keyEvent != null && keyEvent.keyCode == KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                Log.d(TAG, "onBackPressed()" + childFragmentManager.backStackEntryCount + i)
                if (childFragmentManager.fragments.size == 1) {
                    dialogInterface.dismiss()
                } else {
                    childFragmentManager.popBackStack()
                    childFragmentManager.commit {
                        show(childFragmentManager.fragments.last())
                    }
                }
            }
            true
        }
        return bottomSheetDialogFragment
    }

    override fun getTheme(): Int = R.style.ThemeOverlay_BottomSheetDialog



}
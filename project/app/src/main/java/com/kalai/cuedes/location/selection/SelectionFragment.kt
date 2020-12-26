package com.kalai.cuedes.location.selection

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.KeyEvent.KEYCODE_BACK
import android.widget.FrameLayout
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.google.android.gms.maps.model.LatLng


import com.google.android.material.bottomsheet.BottomSheetBehavior

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionBinding


class SelectionFragment(private var latLng: LatLng) : BottomSheetDialogFragment(){

    companion object{
        private const val TAG = "BottomSheetFragment"
    }


    private lateinit var binding:FragmentSelectionBinding
    private val selectionViewModel: SelectionViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionBinding.inflate(inflater,container,false)
        childFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.selection_fragment_container_view,LocationNameFragment(latLng),"LocationFragment")
            addToBackStack(null)
        }
        return binding.root
    }




    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialogFragment = super.onCreateDialog(savedInstanceState)
        bottomSheetDialogFragment.setOnShowListener {
            val bottomSheet = bottomSheetDialogFragment.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let { BottomSheetBehavior.from(bottomSheet).apply { isHideable = false } }
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<FrameLayout?>(bottomSheet as FrameLayout)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetDialogFragment.setCanceledOnTouchOutside(false)
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height =  context?.resources?.displayMetrics?.heightPixels?.div(2.5f)?.toInt() ?: WindowManager.LayoutParams.MATCH_PARENT
            bottomSheet.layoutParams = layoutParams
        }
        childFragmentManager.addOnBackStackChangedListener {
            if(childFragmentManager.backStackEntryCount == 0){
                dialog?.dismiss()
            }
        }

        bottomSheetDialogFragment.setOnKeyListener{ dialogInterface, i, keyEvent ->
            if(keyEvent != null && keyEvent.keyCode == KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                Log.d(TAG, "onBackPressed()" + childFragmentManager.backStackEntryCount + i)
                if (childFragmentManager.fragments.size == 1) {
                    dialogInterface.dismiss()
                    selectionViewModel.clear()
                } else {
                    childFragmentManager.popBackStack()
                    childFragmentManager.commit {
                        setTransition(TRANSIT_FRAGMENT_FADE)
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
package com.kalai.cuedes.location.selection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.gms.maps.model.LatLng
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.FragmentSelectionBinding

class SelectionFragment : Fragment() {


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



}



/*


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setWindowAnimations(R.style.DialogAnimation)

        dialog?.setCanceledOnTouchOutside(false)

        childFragmentManager.addOnBackStackChangedListener {
            if(childFragmentManager.backStackEntryCount == 0){
                dialog?.dismiss()
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dialog?.setOnKeyListener{ dialogInterface, i, keyEvent ->
            if(keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                Log.d(TAG, "onBackPressed()" + childFragmentManager.backStackEntryCount + i)
                if (childFragmentManager.fragments.size == 1) {
                    Log.d(TAG,"Going to be dismissed")
                    val result = bundleOf("Successful" to false)
                    */
/*TODO need to make req key const*//*

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
    }
}*/

package com.kalai.cuedes.entry.onboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.common.GoogleApiAvailability
import com.kalai.cuedes.databinding.FragmentOnboardPlayServiceBinding
import com.kalai.cuedes.entry.onboard.PageContent.*
import com.kalai.cuedes.hide
import com.kalai.cuedes.isGooglePlayServicesCompatible
import com.kalai.cuedes.show

class PlayServiceFragment : Fragment() {

    private lateinit var googleApiAvailability: GoogleApiAvailability
    private lateinit var  binding: FragmentOnboardPlayServiceBinding
    private val viewModel: OnBoardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        googleApiAvailability = GoogleApiAvailability.getInstance()
        binding = FragmentOnboardPlayServiceBinding.inflate(inflater,container,false)
        binding.playServiceRequirementButton.setOnClickListener { googleApiAvailability.makeGooglePlayServicesAvailable(activity) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionUpdate()
    }

    override fun onResume() {
        super.onResume()
        permissionUpdate()
    }

    private fun permissionUpdate(){
        if (googleApiAvailability.isGooglePlayServicesCompatible(context)) {
            binding.playServiceRequirementButton.hide()
            binding.okImageView.show()
            if(viewModel.isPageNavigationViewable.value?.get(PLAY_SERVICE) != true)
                viewModel.updateIsPageNavigationViewable(PLAY_SERVICE,true)
        }
        else{
            binding.playServiceRequirementButton.show()
            if(viewModel.isPageNavigationViewable.value?.get(PLAY_SERVICE) != false)
                viewModel.updateIsPageNavigationViewable(PLAY_SERVICE, false)
        }
    }

}
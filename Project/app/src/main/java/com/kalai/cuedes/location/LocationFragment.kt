package com.kalai.cuedes.location

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kalai.cuedes.R
import com.kalai.cuedes.SharedViewModel
import com.kalai.cuedes.databinding.FragmentLocationBinding


private const val TAG = "LocationFragment"

class LocationFragment : Fragment() ,OnMapReadyCallback{

    companion object {
        fun newInstance() = LocationFragment()
    }


    private val viewModel: LocationViewModel by viewModels()
    private lateinit var map: SupportMapFragment
    private lateinit var binding:FragmentLocationBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLocationBinding.inflate(layoutInflater, container, false)
        map = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        map.getMapAsync(this)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d(TAG,"Map Ready")

        googleMap?.isMyLocationEnabled = true




        googleMap?.setOnMapLongClickListener {
                lonLat -> Log.d(TAG,lonLat.toString());
            googleMap.addMarker(MarkerOptions().position(lonLat))}

    }



}
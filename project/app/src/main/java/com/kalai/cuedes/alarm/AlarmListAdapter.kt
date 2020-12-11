package com.kalai.cuedes.alarm

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kalai.cuedes.R

class AlarmListAdapter (private val data:Array<String>,private val context:Context):
    RecyclerView.Adapter<AlarmListAdapter.ViewHolder>(){
    val TAG = "MainAdapter"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm_list,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(position)

    }

    override fun getItemCount(): Int {
        Log.d(TAG,"Data size"+ data.size)
        return data.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),OnMapReadyCallback{
        private val textView: TextView = view.findViewById(R.id.textViewLocation)
        private val mapView: MapView = view.findViewById(R.id.mapView)
        private lateinit var map: GoogleMap
        private lateinit var latLng: LatLng

        init{
            with(mapView) {
                onCreate(null)
                getMapAsync(this@ViewHolder)
            }
        }

        fun bindView(position: Int){
            data[position].let {
                textView.text= it
                /*TODO need to change this later*/
                latLng= LatLng(1.3521, 103.8198)
                setMapLocation()
            }
        }

        fun clearView() {
            with(map) {
                /* Clear the map and free up resources by changing the map type to none*/
                clear()
                mapType = GoogleMap.MAP_TYPE_NONE
            }
        }

        private fun setMapLocation() {
            if (!::map.isInitialized) return
            with(map) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
                addMarker(MarkerOptions().position(latLng))
                mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        }

        override fun onMapReady(googleMap: GoogleMap?) {
            MapsInitializer.initialize(context)
            /*If map is not initialised properly*/
            map = googleMap ?: return
            setMapLocation()
        }
    }

}
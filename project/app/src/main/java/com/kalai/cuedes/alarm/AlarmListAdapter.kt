package com.kalai.cuedes.alarm

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import  androidx.recyclerview.widget.ListAdapter
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kalai.cuedes.R
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.getCameraUpdateBounds

class AlarmListAdapter (private val context:Context):
    ListAdapter<Alarm,AlarmListAdapter.ViewHolder>(AlarmListDiffCallback()){

    companion object{
        const val TAG = "MainAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm_list,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),OnMapReadyCallback{
        private val alarmNameTextView: TextView = view.findViewById(R.id.alarm_identifier_text_view)
        private val mapView: MapView = view.findViewById(R.id.map_view)
        private val radiusTextView:TextView = view.findViewById(R.id.radius_text_view)
        private val alarmSwitch:SwitchCompat = view.findViewById(R.id.alarm_switch)
        private lateinit var map: GoogleMap
        private lateinit var latLng: LatLng
        private var radius = 0.0
        private var isActivated = false

        init{
            with(mapView) {
                onCreate(null)
                getMapAsync(this@ViewHolder)
                isClickable=false
            }
        }

        fun bindView(position: Int){
            getItem(position).run{
                alarmNameTextView.text= name
                this@ViewHolder.latLng = LatLng(latitude, longitude)
                radiusTextView.text = radius.toString()
                this@ViewHolder.radius = radius.toDouble()
                this@ViewHolder.isActivated = isActivated
                if(alarmSwitch.isChecked){
                    if(!isActivated){
                        alarmSwitch.toggle()
                    }
                }
               else{
                    if(isActivated)
                        alarmSwitch.toggle()
                }
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
            if (::map.isInitialized) {
                with(map) {
                    addMarker(MarkerOptions().position(latLng))
                    mapType = GoogleMap.MAP_TYPE_NORMAL
                }
                val color =
                    if (isActivated) context.getColor(R.color.radius_alarm_active) else context.getColor(
                        R.color.radius_alarm_inactive
                    )
                val circleOptions = CircleOptions()
                    .radius(this@ViewHolder.radius)
                    .center(latLng)
                    .fillColor(color)
                    .strokeColor(Color.TRANSPARENT)

                val circle = map.addCircle(circleOptions)
                val cameraUpdate = getCameraUpdateBounds(circle, 100)
                map.moveCamera(cameraUpdate)
            }
            else{}
        }



        override fun onMapReady(googleMap: GoogleMap?) {
            MapsInitializer.initialize(context)
            /*If map is not initialised properly*/
            map = googleMap ?: return
            setMapLocation()
        }
    }


    class AlarmListDiffCallback: DiffUtil.ItemCallback<Alarm>() {
        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean = oldItem.name == newItem.name
    }



}
package com.kalai.cuedes.alarm

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kalai.cuedes.CueDesApplication
import com.kalai.cuedes.R
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.getCameraUpdateBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class AlarmListAdapter ():
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

    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view),OnMapReadyCallback{
        private val alarmNameTextView: TextView = view.findViewById(R.id.alarm_identifier_text_view)
        private val mapView: MapView = view.findViewById(R.id.map_view)
        private val radiusTextView:TextView = view.findViewById(R.id.radius_text_view)
        private val alarmSwitch:SwitchCompat = view.findViewById(R.id.alarm_switch)
        private lateinit var map: GoogleMap
        private lateinit var latLng: LatLng
        private var radius = 0.0
        private var isActivated = false
        private var isMapLoaded = false
        private  val color get() =
                if (isActivated)
                    view.context.getColor(R.color.radius_alarm_active)
                else
                    view.context.getColor(R.color.radius_alarm_inactive)

        private val circleOptions get() = CircleOptions()
                .radius(this@ViewHolder.radius)
                .center(latLng)
                .fillColor(color)
                .strokeColor(Color.TRANSPARENT)

        lateinit var circle: Circle
        init{
            with(mapView) {
                onCreate(null)
                getMapAsync(this@ViewHolder)
                isClickable=false
            }
        }

        fun bindView(position: Int){
            Timber.d("BindingView at $position")
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
                setMapLocation(view.context)
            }
            alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
                /*TODO will move to viewmodel later*/
                isActivated = isChecked
                val alarmName = alarmNameTextView.text.toString()
                (view.context.applicationContext as CueDesApplication).removeGeoFence(alarmName)
                updateIsActivated(isChecked)
            }
        }

        private fun updateIsActivated(isActivated:Boolean) {
            CoroutineScope(Dispatchers.IO).launch{
            ( view.context.applicationContext as CueDesApplication).repository.updateIsActivated(alarmNameTextView.text.toString(),isActivated)
            }
            CoroutineScope(Dispatchers.Main).launch { circle.fillColor = color}

        }

        fun clearView() {
            with(map) {
                /* Clear the map and free up resources by changing the map type to none*/
                Timber.d("Clearing ${alarmNameTextView.text}")
                clear()
                mapType = GoogleMap.MAP_TYPE_NONE
            }
        }

        private fun setMapLocation(context: Context) {
            if (::map.isInitialized) {
                with(map) {
                    addMarker(MarkerOptions().position(latLng))
                    mapType = GoogleMap.MAP_TYPE_NORMAL
                }


               circle = map.addCircle(circleOptions)
                val cameraUpdate = getCameraUpdateBounds(circle, 100)
              CoroutineScope(Dispatchers.Main).launch {
                    map.moveCamera(cameraUpdate)
                }

                map.setOnMapLoadedCallback { Timber.d("Map loaded ${alarmNameTextView.text}") }


            }
        }



        override fun onMapReady(googleMap: GoogleMap?) {
            MapsInitializer.initialize(view.context)
            /*If map is not initialised properly*/
            mapView.getMapAsync {
                loadedGoogleMap ->
                Timber.d("Map loadeded")
                map = loadedGoogleMap
                setMapLocation(view.context)
            }
        }
    }



    class AlarmListDiffCallback: DiffUtil.ItemCallback<Alarm>() {
        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean = oldItem.name == newItem.name
    }



}
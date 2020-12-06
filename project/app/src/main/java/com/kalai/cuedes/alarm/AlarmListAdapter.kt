package com.kalai.cuedes.alarm

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kalai.cuedes.R

class AlarmListAdapter (private val data:Array<String>):
    RecyclerView.Adapter<AlarmListAdapter.ViewHolder>(){
    val TAG = "MainAdapter"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm_list,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = data[position]

    }

    override fun getItemCount(): Int {
        Log.d(TAG,"Data size"+ data.size)
        return data.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val textView: TextView = view.findViewById(R.id.textViewLocation)
    }

}
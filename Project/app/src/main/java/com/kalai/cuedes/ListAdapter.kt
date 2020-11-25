package com.kalai.cuedes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapter (private val data:Array<String>):
    RecyclerView.Adapter<ListAdapter.ViewHolder>(){
    val TAG = "MainAdapter"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.alarm_list_item,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = data[position]

    }

    override fun getItemCount(): Int {
        Log.d(TAG,"Data size"+ data.size)
        return data.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val textView: TextView = view.findViewById(R.id.alarmTextView)
    }

}
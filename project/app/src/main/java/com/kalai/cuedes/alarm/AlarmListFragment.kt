package com.kalai.cuedes.alarm


import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kalai.cuedes.databinding.FragmentAlarmListBinding


class AlarmListFragment : Fragment() {

    companion object {
        fun newInstance() = AlarmListFragment()
    }

    private val fakeData = arrayOf("Hello","Aloha","Test","Test2")
    private val viewModelAlarm: AlarmListViewModel by viewModels()
    private lateinit var binding: FragmentAlarmListBinding
    private lateinit var alarmRecyclerView: RecyclerView


    private val recycleListener = RecyclerView.RecyclerListener { holder ->
        val mapHolder = holder as AlarmListAdapter.ViewHolder
        mapHolder.clearView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmListBinding.inflate(inflater)
        alarmRecyclerView = binding.recyclerView.apply {
            adapter = context?.let { AlarmListAdapter(fakeData, it) }
            addRecyclerListener(recycleListener)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}
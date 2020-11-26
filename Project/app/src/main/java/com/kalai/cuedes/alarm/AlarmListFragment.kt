package com.kalai.cuedes.alarm


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kalai.cuedes.databinding.FragmentAlarmListBinding



class AlarmListFragment : Fragment() {

    companion object {
        fun newInstance() = AlarmListFragment()
    }

    private val fakeData = arrayOf("Hello","Aloha")
    private val viewModelAlarm: AlarmListViewModel by viewModels()
    private lateinit var binding: FragmentAlarmListBinding
    private lateinit var alarmRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmListBinding.inflate(inflater,container,false)
        alarmRecyclerView = binding.recyclerView
        alarmRecyclerView.adapter = AlarmListAdapter(fakeData)

        binding.floatingActionButton.setOnClickListener {
            val alarmAdditionIntent = Intent(this.context,AlarmAdditionActivity::class.java)
            startActivity(alarmAdditionIntent)
        }

        val layoutManager:LinearLayoutManager = alarmRecyclerView.layoutManager as LinearLayoutManager
        alarmRecyclerView.addItemDecoration(DividerItemDecoration(alarmRecyclerView.context,layoutManager.orientation))

        return binding.root
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
    }

}
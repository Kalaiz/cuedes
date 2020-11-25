package com.kalai.cuedes.alarm


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.kalai.cuedes.databinding.FragmentAlarmListBinding



class AlarmListFragment : Fragment() {

    companion object {
        fun newInstance() = AlarmListFragment()
    }

    private val fakeData = arrayOf("Hello","Aloha")
    private val viewModelAlarm: AlarmListViewModel by viewModels()



    private lateinit var binding: FragmentAlarmListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmListBinding.inflate(inflater,container,false)
        binding.listRecyclerView.adapter = AlarmListAdapter(fakeData)
        binding.floatingActionButton.setOnClickListener { /*TODO: need to link an activity or Fragment*/}
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
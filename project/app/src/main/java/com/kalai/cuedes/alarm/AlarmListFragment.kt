package com.kalai.cuedes.alarm


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kalai.cuedes.CueDesApplication
import com.kalai.cuedes.SharedViewModel
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.databinding.FragmentAlarmListBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class AlarmListFragment : Fragment(),AlarmListOpsListener {
    /*TODO Handle onBackPressed; Maybe something like onbackpress send to  home fragment*/



    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentAlarmListBinding
    private lateinit var alarmRecyclerView: RecyclerView
    private lateinit var adapter: ListAdapter<Alarm,AlarmListAdapter.ViewHolder>
    private val repository by lazy { (activity?.application as CueDesApplication).repository }


    private val recycleListener = RecyclerView.RecyclerListener { holder ->
        val mapHolder = holder as AlarmListAdapter.ViewHolder
        mapHolder.clearView()
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmListBinding.inflate(inflater)
        adapter = AlarmListAdapter(this)
        alarmRecyclerView = binding.recyclerView.apply {
            adapter = this@AlarmListFragment.adapter
            addRecyclerListener(recycleListener)
        }


        lifecycleScope.launch {
            repository.alarms.collect { data ->
                adapter.submitList(data)
                adapter.notifyDataSetChanged()
            }
        }

        return binding.root
    }




/*    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                    Log.d(TAG,"onBackPressed")

                    onBackPressedCallback.remove()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback
        )
    }*/



    override fun deleteAlarm(alarmName:String) {
        sharedViewModel.deleteAlarm(alarmName)
    }

    override fun updateIsActivated(alarmName:String,isActivated: Boolean) {
        sharedViewModel.updateIsActivated(alarmName,isActivated)

    }


}
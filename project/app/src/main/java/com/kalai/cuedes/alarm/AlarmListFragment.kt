package com.kalai.cuedes.alarm


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kalai.cuedes.CueDesApplication
import com.kalai.cuedes.data.Alarm
import com.kalai.cuedes.databinding.FragmentAlarmListBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class AlarmListFragment : Fragment() {
/*TODO Handle onBackPressed; Maybe something like onbackpress send to  home fragment*/
    companion object {
        private const val TAG = "AlarmListFragment"
    }


    private val viewModelAlarm: AlarmListViewModel by viewModels()
    private lateinit var binding: FragmentAlarmListBinding
    private lateinit var alarmRecyclerView: RecyclerView
    private lateinit var adapter: ListAdapter<Alarm,AlarmListAdapter.ViewHolder>
    private val repository by lazy { (activity?.application as CueDesApplication).repository }
  /*  private lateinit var onBackPressedCallback: OnBackPressedCallback*/

    private val recycleListener = RecyclerView.RecyclerListener { holder ->
        val mapHolder = holder as AlarmListAdapter.ViewHolder
        mapHolder.clearView()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmListBinding.inflate(inflater)

        context?.let { adapter = AlarmListAdapter(it) }
        alarmRecyclerView = binding.recyclerView.apply {
            adapter = this@AlarmListFragment.adapter
            addRecyclerListener(recycleListener)
        }

        lifecycleScope.launch {
            repository.alarms.collect { data ->
                adapter.submitList(data)
         }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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

    override fun onStart() {
        super.onStart()
    }



}
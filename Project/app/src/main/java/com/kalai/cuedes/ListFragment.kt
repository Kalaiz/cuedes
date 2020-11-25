package com.kalai.cuedes


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.kalai.cuedes.databinding.ListFragmentBinding


class ListFragment : Fragment() {

    companion object {
        fun newInstance() = ListFragment()
    }

    private val fakeData = arrayOf("Hello","Aloha")
    private val viewModel: ListViewModel by viewModels()



    private lateinit var binding: ListFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ListFragmentBinding.inflate(inflater,container,false)
        binding.listRecyclerView.adapter = ListAdapter(fakeData)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
    }

}
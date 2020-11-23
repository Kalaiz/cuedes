package com.kalai.cueloc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kalai.cueloc.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private val fakeData = arrayOf("hello","Aloha")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        /*Initialising RV*/
        val recyclerView = binding.mainRecyclerView;
        recyclerView.adapter = MainAdapter(fakeData)

    }
}
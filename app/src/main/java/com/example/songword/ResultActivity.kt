package com.example.songword

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ResultActivity : AppCompatActivity() {
    lateinit var word : TextView
    lateinit var def : TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        word = findViewById(R.id.holdText)
        def = findViewById(R.id.holdText2)

        val definition = intent.getSerializableExtra("result") as definition

        Log.d("liciTag", "definition: " + definition)

        word.setText(definition.word)
        def.setText(definition.def)

        // use recycler view to display the list of songs found
        recyclerView = findViewById(R.id.recyclerView)
        // Set the RecyclerView direction to vertical (the default)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val song = getSongs()
        val adapter = ResultAdapter(song)
        recyclerView.adapter = adapter
    }

    fun getSongs(): List<song>{
        val songsList = intent.getSerializableExtra("result2") as List<song>
        return songsList
    }
}



package com.example.songword

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import org.w3c.dom.Text

class ResultAdapter(val songs: List<song>) : RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.row_details, parent, false)
        return ViewHolder(view)
    }

    // The adapter has a row that's ready to be rendered and needs the content filled in
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPlace = songs[position]

        holder.artist.text = currentPlace.artist
        holder.song.text = currentPlace.song


    }

    // Return the total number of rows you expect your list to have
    override fun getItemCount(): Int {
        Log.d("licitag", "size of items in list: ${songs.size}")
        return songs.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artist : TextView = itemView.findViewById(R.id.artist)
        val song : TextView = itemView.findViewById(R.id.song)

    }

}


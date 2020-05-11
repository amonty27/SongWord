package com.example.songword

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import android.text.TextWatcher
import android.view.View
import org.jetbrains.anko.doAsync


class PlayerActivity : AppCompatActivity() {

    lateinit var search : TextView
    lateinit var searchButton : Button
    lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        search = findViewById(R.id.search)
        searchButton = findViewById(R.id.searchButton)
        progressBar = findViewById(R.id.progressBar)

        //does not show progress bar until search is happening and do not allow search without something typed in search box
        progressBar.isVisible = false
        searchButton.isEnabled = false

        search.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchButton.isEnabled = true
            }

        })

        val intent = Intent(this@PlayerActivity, ResultActivity::class.java)

        // when searched is clicked, start api search
        searchButton.setOnClickListener { v : View ->
            val searchText = search.text.toString()
            val apikey = getString(R.string.webster_key)
            val clientId = getString(R.string.client_id)
            val clientSecret = getString(R.string.client_secret)

            val play = PlayerManager()

            progressBar.isVisible = true

            doAsync{
                val api1 = play.retrieveSearch(searchText, apikey)
                val api2: ArrayList<song> = play.retrieveSongSearch(clientId, clientSecret,searchText)
                /* if the result sent back for the word is "no", "no", a result was not found for the word in
                the dictionary*/
                if(api1.def.equals("no")&&api1.word.equals("no")){
                    /* if the result sent back for the word is "no", "Sorry, no songs contain the word",
                    no results where found for songs containing that word*/
                    if(api2[0].artist.equals("no") && api2[0].song.equals("Sorry, no songs contain the word " + searchText)){
                        runOnUiThread{
                            /*send the user through to see the error message */
                            intent.putExtra("result", definition("","Sorry, " + searchText + " was not found in dictionary"))
                            intent.putExtra("result2", api2)
                            progressBar.isVisible = false
                            startActivity(intent)
                        }
                    }
                    runOnUiThread{
                        intent.putExtra("result", definition("","Sorry, " + searchText + " was not found in dictionary"))
                        intent.putExtra("result2", api2)
                        progressBar.isVisible = false
                        startActivity(intent)
                    }
                }
                else {
                    runOnUiThread {
                        intent.putExtra("result", api1)
                        intent.putExtra("result2", api2)
                        progressBar.isVisible = false
                        startActivity(intent)
                    }
                }
            }
        }



    }
}

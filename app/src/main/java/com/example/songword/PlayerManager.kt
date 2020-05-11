package com.example.songword

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import android.util.Log
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.wait
import okhttp3.internal.waitMillis
import java.lang.Exception
import java.net.URLEncoder

class PlayerManager {

    private val okHttpClient: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()

        // Set up our OkHttpClient instance to log all network traffic to Logcat
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)

        builder.connectTimeout(15, TimeUnit.SECONDS)
        builder.readTimeout(15, TimeUnit.SECONDS)
        builder.writeTimeout(15, TimeUnit.SECONDS)

        okHttpClient = builder.build()
    }

    // from android tweets
    fun retrieveOAuthToken(apiKey: String, apiSecret: String): String {
        // Twitter requires us to encoded our API Key and API Secret in a special way for the request.
        val encodedSecrets = encodeSecrets(apiKey, apiSecret)

        // OAuth is defined to be a POST call, which has a specific body / payload to let the server
        // know we are doing "application-only" OAuth (e.g. we will only access public information)
        val requestBody = "grant_type=client_credentials"
            .toRequestBody(
                contentType = "application/x-www-form-urlencoded".toMediaType()
            )

        // Build the request
        // The encoded secrets become a header on the request
        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .header("Authorization", "Basic $encodedSecrets")
            .post(requestBody)
            .build()

        // "Execute" the request (.execute will block the current thread until the server replies with a response)
        val response = okHttpClient.newCall(request).execute()

        // Create an empty, mutable list to hold up the Tweets we will parse from the JSON
        val responseString: String? = response.body?.string()

        // If the response was successful (e.g. status code was a 200) AND the server sent us back
        // some JSON (which will contain the OAuth token), then we can go ahead and parse the JSON body.
        return if (!responseString.isNullOrEmpty() && response.isSuccessful) {
            val json: JSONObject = JSONObject(responseString)
            Log.d("licitag","access token:" + json.getString("access_token"))
            json.getString("access_token")

        } else {
            ""
        }
    }

    // from android tweets
    private fun encodeSecrets(apiKey: String, apiSecret: String): String {
        // Encoding for a URL -- converts things like spaces into %20
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val encodedSecret = URLEncoder.encode(apiSecret, "UTF-8")

        // Concatenate the two together, with a colon inbetween
        val combinedEncoded = "$encodedKey:$encodedSecret"

        // Base-64 encode the combined string
        // https://en.wikipedia.org/wiki/Base64
        val base64Combined = Base64.encodeToString(
            combinedEncoded.toByteArray(), Base64.NO_WRAP)

        return base64Combined
    }

    // retrieve songs with the word in the title
    fun retrieveSongSearch(clientID : String, clientSecret: String, searchTerm: String): ArrayList<song>{

        val accessToken = retrieveOAuthToken(clientID,clientSecret)

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/search?q=$searchTerm&type=track")
            .header("Authorization", "Bearer $accessToken")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseString: String? = response.body?.string()
        val songNames = arrayListOf<String>()
        val artistList = arrayListOf<String>()
        val list : ArrayList<song> = arrayListOf()

        if (!responseString.isNullOrEmpty() && response.isSuccessful) {
            Log.d("liciTag", "this worked i guess yippy222")
            val json = JSONObject(responseString)
            val songs = json.getJSONObject("tracks")
            Log.d("liciTag","songs + $songs")
            val items = songs.getJSONArray("items")
            Log.d("liciTag", "items:" + items)
            // if the items JSON array is empty, no results were found, so return list with error message in it
            if(items.length() == 0){
                list.add(song("","Sorry, no songs contain the word " + searchTerm))
                return list
            }

            // for all songs returned (max 20 by default), get the artist name and song name
            for(i in 0 until items.length()) {
                val item = items.getJSONObject(i)

                val songName = item.getString("name")
                Log.d("liciTag", "song name: " + songName)

                val artists = item.getJSONArray("artists")
                val artisit = artists.getJSONObject(0)
                val artisit2 = artisit.getString("name")
                Log.d("liciTag", "artist2 " + artisit2)

                list.add(song(song = songName, artist = artisit2))
            }
        }

        return list
    }


    // find the first result from the dictionary
    fun retrieveSearch (term : String, apiKey : String) : definition{

        val request = Request.Builder()
            .url("https://www.dictionaryapi.com/api/v3/references/collegiate/json/$term?key=$apiKey")
            .build()

        var shortdef2 : String = ""

        val response = okHttpClient.newCall(request).execute()
        val responseString: String? = response.body?.string()

        if (!responseString.isNullOrEmpty() && response.isSuccessful) {
            Log.d("liciTag", "this worked i guess yippy222")

            val json = JSONArray(responseString)
            Log.d("liciTag", "jsonArray: " + json)

            // if there is nothing in the array, no results or suggestions for other words were found
            if(json.length() == 0) {
                Log.d("liciTag", "we are checking length of json array")
                return definition("no", "no")
            }
            /*
            try to get the results from the array. This is to avoid accessing things that were not returned when
            the word searched was not found but suggestions were
            */
           try {
               val meto = json.getJSONObject(0)
               Log.d("liciTag", "meto: " + meto)

               val meta = meto.getJSONObject("meta")
               Log.d("liciTag", "meta: " + meta)

               val term2 = meta.getString("id")
               Log.d("liciTag", "term2: " + term2)

               val shortdef = meto.getJSONArray("shortdef")
               Log.d("liciTag", "shortdef: " + shortdef)

               shortdef2 = shortdef.getString(0)
               Log.d("liciTag", "shortdef2: " + shortdef2)

               Log.d("liciTag", "shortdef2 baby: " + shortdef2)
               return definition(term2, shortdef2)
           }
           catch(exception:Exception){
               exception.printStackTrace()
               return definition("no", "no")
           }
        }

        else{
            Log.d("liciTag", "we are in the other else statement")
            return definition("no", "no")
        }

    }
}
package com.example.songword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {

    lateinit var username : TextView
    lateinit var password : TextView
    lateinit var loginButton : Button
    lateinit var signupButton : Button
    lateinit var rememberSwitch : Switch

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = getSharedPreferences("songword_data", Context.MODE_PRIVATE)
        var usernameFill : Boolean = false
        var passwordFill : Boolean = false
        username = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signUpButton)
        rememberSwitch = findViewById(R.id.remember)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        rememberSwitch.isChecked = preferences.getBoolean("switch", false)
        // If the user is already logged in, send them directly to the Maps Activity
        if (firebaseAuth.currentUser != null && preferences.getBoolean("switch",false)) {
            val user = firebaseAuth.currentUser
            Toast.makeText(this, getString(R.string.loggedin)+ " ${user!!.email}",
                Toast.LENGTH_SHORT).show()

            val intent = Intent(this, PlayerActivity::class.java)
            startActivity(intent)
        }

        // keep the login button un-enabled until the user has typed in their username and password
        loginButton.isEnabled = false

        rememberSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                preferences
                    .edit()
                    .putBoolean("switch", isChecked)
                    .apply()
            }
            else{
                preferences
                    .edit()
                    .putBoolean("switch", false)
                    .apply()
            }
        }

        username.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val usernameVal = username.text.toString()
                usernameFill = usernameVal.trim().isNotEmpty()

                if(usernameFill && passwordFill){loginButton.isEnabled = true}
                else {loginButton.isEnabled = false}
            }
        })

        password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val passwordVal = password.text.toString()
                passwordFill = passwordVal.trim().isNotEmpty()

                if(usernameFill && passwordFill){loginButton.isEnabled = true}
                else {loginButton.isEnabled = false}
            }
        })

        loginButton.setOnClickListener { view: View ->
            Log.d("MainActivity", "onClick() called")

            // Save user credentials to file
            val inputtedUsername: String = username.text.toString()
            val inputtedPassword: String = password.text.toString()

            firebaseAuth
                .signInWithEmailAndPassword(inputtedUsername, inputtedPassword)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    firebaseAnalytics.logEvent("login_clicked", null)

                    if (task.isSuccessful) {
                        firebaseAnalytics.logEvent("login_success", null)

                        val currentUser: FirebaseUser = firebaseAuth.currentUser!!
                        val email = currentUser.email

                        Toast.makeText(
                            this,
                            getString(R.string.signedin)+" $email!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // An Intent is used to start a new Activity and also send data to it (via `putExtra(...)`)
                        val intent: Intent = Intent(this, PlayerActivity::class.java)
                        startActivity(intent)
                    } else {
                        val exception = task.exception!!
                        val errorType = if (exception is FirebaseAuthInvalidCredentialsException) {
                            "invalid_credentials"
                        } else {
                            "unknown_error"
                        }

                        // Track an analytic with the specific failure reason
                        val bundle = Bundle()
                        bundle.putString("error_type", errorType)
                        firebaseAnalytics.logEvent("login_failed", bundle)

                        Toast.makeText(
                            this,
                            getString(R.string.invalid) + "$exception!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        //send user to signup button if they need to create a new account
        signupButton.setOnClickListener { v: View ->
            val intent = Intent(this@MainActivity, SignupActivity::class.java)
            startActivity(intent)
        }
    }

}
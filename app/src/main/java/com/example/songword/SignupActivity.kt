package com.example.songword

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignupActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    lateinit var email : TextView
    lateinit var password1 : TextView
    lateinit var password2: TextView
    lateinit var signupButton : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        var emailFill : Boolean = false
        var passwordFill1 : Boolean = false
        var passwordFill2: Boolean = false

        email = findViewById(R.id.email)
        password1 = findViewById(R.id.password1)
        password2 = findViewById(R.id.password2)
        signupButton = findViewById(R.id.signupButton2)

        signupButton.isEnabled = false

        email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val emailVal = email.text.toString()
                emailFill = emailVal.trim().isNotEmpty()

                if(emailFill && passwordFill1 && passwordFill2){signupButton.isEnabled = true}
                else {signupButton.isEnabled = false}
            }
        })

        password1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val password1Val = password1.text.toString()
                passwordFill1 = password1Val.trim().isNotEmpty()

                if(emailFill && passwordFill1 && passwordFill2){signupButton.isEnabled = true}
                else {signupButton.isEnabled = false}
            }
        })

        password2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val password2Val = password2.text.toString()
                passwordFill2 = password2Val.trim().isNotEmpty()

                if(emailFill && passwordFill1 && passwordFill2){signupButton.isEnabled = true}
                else {signupButton.isEnabled = false}
            }
        })

        signupButton.setOnClickListener{

            //check if the passwords match
            if(password1.text.toString() != password2.text.toString()){
                Toast.makeText(
                    this,
                    getString(R.string.invalidPass),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val inputtedEmail = email.text.toString()
            val inputtedPassword = password1.text.toString()

            Log.d("licitag", "got here so that works")


            firebaseAuth
                .createUserWithEmailAndPassword(inputtedEmail, inputtedPassword)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        firebaseAnalytics.logEvent("signup_success", null)

                        val currentUser: FirebaseUser = firebaseAuth.currentUser!!
                        val email = currentUser.email

                        Toast.makeText(
                            this,
                            getString(R.string.success) +" $email!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        firebaseAnalytics.logEvent("signup_failed", null)

                        val exception = task.exception!!
                        Toast.makeText(
                            this,
                            getString(R.string.failed)+" $exception!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
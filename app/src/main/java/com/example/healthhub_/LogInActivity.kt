// LogInActivity.kt
//
//  Log In Activity handles the user login process, inputs validation and Firebase authentication
//
// Gustavo Amaya
// May 2024
//
// Version 1

package com.example.healthhub_

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.healthhub_.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogInActivity : AppCompatActivity() {
    // Declares instance for firebase authentication and view binding
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLogInBinding

    // initialize the activity, sets up view binding, and initializes Firebase Auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set view binding
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Click listener for the login button, retrieves input data, and initiates the login
        binding.btnLogIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if(checkAllField()) {

                // Attempts to sign in using Firebase Auth, displays a success message, navigates to
                // "HomeActivity" if successful, and logs an error if unsuccessful
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful){
                        //if successful already sign in
                        Toast.makeText(this, "Successfully Log In", Toast.LENGTH_SHORT).show()
                        //go to another activity
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        //use to destroy activity
                        finish()
                    } else {
                        //Not Sign in
                        Log.e("error: ", it.exception.toString())
                    }

                }
            }
        }
    }

    // Validates the email and password fields, checking for required fields, valid email format,
    // minimum password length. Displays error messages if validation fails
    private fun checkAllField(): Boolean {
        val email = binding.etEmail.text.toString()
        if(binding.etEmail.text.toString() == ""){
            binding.textInputLayoutEmail.error = "This is a required field"
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.textInputLayoutEmail.error = "Check email format"
            return false
        }
        if(binding.etPassword.text.toString() == ""){
            binding.textInputLayoutPassword.error = "This is required field"
            binding.textInputLayoutPassword.errorIconDrawable = null
            return false
        }
        if(binding.etPassword.length() <= 6){
            binding.textInputLayoutPassword.error = "Password should at least 8 characters long"
            binding.textInputLayoutPassword.errorIconDrawable = null
            return false
        }
        return true
    }
}
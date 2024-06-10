// SignUpActivity.kt
//
// Sign Up Activity handles user registation, validating input fields and creating a new user acc-
// ount using the Firebase Database

package com.example.healthhub_

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.healthhub_.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


 class SignUpActivity : AppCompatActivity() {
     // Declares instance for firebase authentication and view binding
    private lateinit var auth : FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

     // initialize the activity, sets up view binding, and initializes Firebase Auth
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Click listener for sign up button, retrieves input data, validates it, and attempts to
        // create a new user account using Firebase Auth
        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if(checkAllFields()){
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(this,
                                  "Account created successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignUpActivity,
                                            UserGoalsActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        //account not created
                        Log.e("error: ", it.exception.toString())
                    }
                }
            }
        }
    }

     // checkAllFields handles the validating the email and password fields and handles errors
     private fun checkAllFields(): Boolean {
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
         if(binding.etConfirmPassword.text.toString() == ""){
             binding.textInputLayoutConfirmPassword.error = "This is required field"
             binding.textInputLayoutConfirmPassword.errorIconDrawable = null
             return false
         }
         if(binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()){
             binding.textInputLayoutPassword.error = "Password do not match"
             return false
         }
         return true;
     }


}
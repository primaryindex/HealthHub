// MainActivity.kt
//
// Main Activity handles the user's authentication state and navigates the appropriate activity
//
// Gustavo Amaya
// May 2024
//
// Version 1

package com.example.healthhub_

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File

class MainActivity : AppCompatActivity() {
    // Declares instance for firebase authentication and view binding
    private lateinit var auth: FirebaseAuth

    // initialize the activity and initializes Firebase Auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sets the code code cache directory for optimized dex files ot read-only
        // had to do this to run the app for some reason
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        // Delay 3 seconds to check if a user is authenticated, handles next activity if user is
        // authenticated or goes sign up activity
        Handler(Looper.getMainLooper()).postDelayed({
            val user = auth.currentUser
            if(user != null){
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }else {
                val intent = Intent(this@MainActivity, SignUpActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 3000) // 3000 millis = 3 second
    }
}
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
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //I do not know how this fixed the error I was having with security Dex or something
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        //use to put delay
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
package com.example.healthhub_

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserGoalsActivity : AppCompatActivity() {
    //find out what these mean
    val database = Firebase.database.reference

    //Buttons To Respective User Goals
    lateinit var buttonSex: Button
    lateinit var buttonActivityLevel: Button
    lateinit var buttonWeightGoal: Button
    lateinit var buttonContinue: Button

    //variable to store user selections
    var userSex: String? = null
    var activityLevel: String? = null
    var weightGoal: String? = null
    var userAge: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_goals)

        //Get the buttons from the UserGoalsActivity
        buttonSex = findViewById(R.id.btn_sex)
        buttonActivityLevel = findViewById(R.id.btn_activity_level)
        buttonWeightGoal = findViewById(R.id.btn_weight_plan)
        buttonContinue = findViewById(R.id.btn_continue)


        //Activity Level Selection
        buttonActivityLevel.setOnClickListener {
            val popupMenu = PopupMenu(this@UserGoalsActivity, buttonActivityLevel)
            popupMenu.menuInflater.inflate(R.menu.activitylevel, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                activityLevel = menuItem.title.toString()
                true
            }
            popupMenu.show()
        }

        // Sex selection
        buttonSex.setOnClickListener {
            val popupMenu = PopupMenu(this@UserGoalsActivity, buttonSex)
            popupMenu.menuInflater.inflate(R.menu.sex, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                userSex = menuItem.title.toString()
                true
            }
            popupMenu.show()
        }

        // Weight Goal selection
        buttonWeightGoal.setOnClickListener {
            val popupMenu = PopupMenu(this@UserGoalsActivity, buttonWeightGoal)
            popupMenu.menuInflater.inflate(R.menu.weightgoal, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                weightGoal = menuItem.title.toString() // Assign the actual value based on user selection
                true
            }
            popupMenu.show()
        }

        checkAllSelectionsMade() //checks all selections are made

        buttonContinue.setOnClickListener {
            checkAllSelectionsMade()
            val intent = Intent(this@UserGoalsActivity, WeightLossActivity::class.java)
            startActivity(intent)

            finish()
        }
    }


    private fun checkAllSelectionsMade() {
        if (activityLevel != null && userSex != null && weightGoal != null) {
            // Save user data to Firebase and show Toast for confirmation
            saveUserProfileToDatabase()
            Toast.makeText(this@UserGoalsActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        } else {
            // Not all selections have been made
            Toast.makeText(this@UserGoalsActivity, "Please complete all selections", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserProfileToDatabase() {
        //Store User
        val userData = hashMapOf(
            "sex" to userSex,
            "age" to userAge,
            "activityLevel" to activityLevel,
            "weightGoal" to weightGoal
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            Firebase.database.reference.child("user-activity-profile").child(userId).setValue(userData)
                .addOnSuccessListener {
                    // Handle successful data save, e.g., navigating to a new activity or updating the UI
                    Toast.makeText(this@UserGoalsActivity, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Handle failed data save
                    Toast.makeText(this@UserGoalsActivity, "Failed to save data!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this@UserGoalsActivity, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }



}
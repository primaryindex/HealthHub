// NewWeightGoalActivity.kt
//
// New Weight Goal Activity handles user interactions related to setting and updating to the Fire-
// base a new weight goal.
//
// Gustavo Amaya
// May 2024
//
// Version 1

package com.example.healthhub_

import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class NewWeightGoalActivity : AppCompatActivity() {
    // Declares instance for firebase authentication
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // data class that handles user profile information
    data class UserProfile(
        val currentWeight: Double?,
        val goalWeight: Double?
    )
    private var userProfile: UserProfile? = null

    // Initializes activity, sets content views, fetches user data, and updates the activity layout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_weight_goal)
        fetchUserData {
            updateUI()
        }

        // Sets up the input filed and button for updating the user's wieght goal
        val updateWeightButton = findViewById<Button>(R.id.btnUpdateWeight)
        val weightInput = findViewById<EditText>(R.id.etUpdateWeight)

        updateWeightButton.setOnClickListener {
            val newWeightStr = weightInput.text.toString()
            if (newWeightStr.isNotEmpty()) {
                val newWeight = newWeightStr.toDouble()
                updateWeight(newWeight)
            } else {
                Toast.makeText(this,
                           "Please enter a valid weight", Toast.LENGTH_SHORT).show()
        }
    }
}

    // fetchUserData handles fetching the user's physical profile data from Firebase
    private fun fetchUserData(callback: () -> Unit) {
        val userPhysicalProfileRef = FirebaseDatabase.getInstance().getReference(
                                                               "user-physical-profile/$userId")

        userPhysicalProfileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(physicalProfileSnapshot: DataSnapshot) {
                val currentWeight =
                    physicalProfileSnapshot.child(
                                             "currentWeight").value.toString().toDoubleOrNull()
                val goalWeight =
                    physicalProfileSnapshot.child(
                                                "goalWeight").value.toString().toDoubleOrNull()

                // Passing Data to the UserProfile data class
                userProfile = UserProfile(
                    currentWeight, goalWeight
                )
                callback()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug",
                    "Error accessing user-physical-profile: ${error.message}")
            }
        })
    }

    // updateUI method handles the user's current and goal weights to the activity layout
    private fun updateUI() {
        userProfile?.let {
            val goalWeight = """
                You reached your goal weight of ${it.goalWeight?.let { "%.1f".format(it) }}
            """.trimIndent()
            val currentWeight = """
                Current Weight ${it.currentWeight?.let {"%.1f".format(it)}}
            """.trimIndent()

            findViewById<TextView>(R.id.tvEnterNewWeight).text = goalWeight
            findViewById<TextView>(R.id.tvCurrentWeight).text = currentWeight
        }
    }

    // updateWeight handles the user's goal weight in the database and logs the update
    private fun updateWeight(newWeight: Double) {
        val userPhysicalProfileRef = FirebaseDatabase.getInstance().getReference(
                                                               "user-physical-profile/$userId")
        userPhysicalProfileRef.child(
                          "goalWeight").setValue(newWeight).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this,
                                       "Weight updated successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this,
                           "Failed to update weight", Toast.LENGTH_SHORT).show()
            }
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val yearWeekFormat = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault())
        val yearWeek = yearWeekFormat.format(Date())
        val weightLogRef = FirebaseDatabase.getInstance().getReference(
                                              "user-weight-log/$userId/$yearWeek/$currentDate")
        val logEntry = mapOf("weight" to newWeight)
        weightLogRef.setValue(logEntry).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this,
                                   "Weight log updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                                       "Failed to log weight update", Toast.LENGTH_SHORT).show()
            }
        }
    }


}


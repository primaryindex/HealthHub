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

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    data class UserProfile(
        val currentWeight: Double?,
        val goalWeight: Double?
    )

    private var userProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_weight_goal)
        fetchUserData {
            updateUI()
        }

        val weightInput = findViewById<EditText>(R.id.etUpdateWeight)
        val updateWeightButton = findViewById<Button>(R.id.btnUpdateWeight)

        updateWeightButton.setOnClickListener {
            val newWeightStr = weightInput.text.toString()
            if (newWeightStr.isNotEmpty()) {
                val newWeight = newWeightStr.toDouble()
                updateWeight(newWeight)
            } else {
                Toast.makeText(this, "Please enter a valid weight", Toast.LENGTH_SHORT).show()
        }
    }
}

    private fun fetchUserData(callback: () -> Unit) {
        val userPhysicalProfileRef = FirebaseDatabase.getInstance().getReference("user-physical-profile/$userId")

        userPhysicalProfileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(physicalProfileSnapshot: DataSnapshot) {
                val currentWeight =
                    physicalProfileSnapshot.child("currentWeight").value.toString().toDoubleOrNull()
                val goalWeight =
                    physicalProfileSnapshot.child("goalWeight").value.toString().toDoubleOrNull()

                // Passing Data to the UserProfile data class
                userProfile = UserProfile(
                    currentWeight, goalWeight
                )
                callback()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error accessing user-physical-profile: ${error.message}")
            }
        })
    }

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

    private fun updateWeight(newWeight: Double) {
        val userPhysicalProfileRef = FirebaseDatabase.getInstance().getReference("user-physical-profile/$userId")
        userPhysicalProfileRef.child("goalWeight").setValue(newWeight).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Weight updated successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to update weight", Toast.LENGTH_SHORT).show()
            }
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val yearWeekFormat = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault())
        val yearWeek = yearWeekFormat.format(Date())
        val weightLogRef = FirebaseDatabase.getInstance().getReference("user-weight-log/$userId/$yearWeek/$currentDate")
        val logEntry = mapOf("weight" to newWeight)
        weightLogRef.setValue(logEntry).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Weight log updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to log weight update", Toast.LENGTH_SHORT).show()
            }
        }
    }


}


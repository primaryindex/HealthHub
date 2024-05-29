package com.example.healthhub_

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class WeightLossActivity : AppCompatActivity() {
    private val database = Firebase.database.reference

    //Weekly Goal Button
    lateinit var weeklyWeightGoal: Button
    var buttonWeeklyWeightGoal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_loss)

        //Find the button weekly goal
        weeklyWeightGoal = findViewById(R.id.btnWeeklyGoals)

        //This will take in the information from user for their weekly goal weight
        weeklyWeightGoal.setOnClickListener {
            val popupMenu = PopupMenu(this@WeightLossActivity, weeklyWeightGoal)

            //Inflating pop up menu from pop_menu.xml file
            popupMenu.menuInflater.inflate(R.menu.weeklygoal, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {  menuItem ->
                // Toast message on menu item clicked
                Toast.makeText(
                    this@WeightLossActivity,
                    "You clicked " + menuItem.title,
                    Toast.LENGTH_SHORT
                ).show()
                buttonWeeklyWeightGoal = menuItem.title.toString()
                true
            }
            // Show the popup menu
            popupMenu.show()
        }

        //Take info from User form the EditTexts
        val submitUserInfo = findViewById<TextView>(R.id.btn_submit)
        submitUserInfo.setOnClickListener {
            val full_name = findViewById<EditText>(R.id.etFullName).text.toString()
            val user_age = findViewById<EditText>(R.id.et_Age).text.toString().toInt()
            val height_ft = findViewById<EditText>(R.id.etHeightFt).text.toString().toInt()
            val height_in = findViewById<EditText>(R.id.etHeightIn).text.toString().toInt()
            val currentWeight = findViewById<EditText>(R.id.etcurrentWeight).text.toString().toDouble()
            val goal_weight = findViewById<EditText>(R.id.etWeightGoal).text.toString().toDouble()

            if (validateInput(height_ft, user_age, height_in, currentWeight, goal_weight)) {
                sendUserDataToFirebase(full_name, user_age, height_ft, height_in, currentWeight, goal_weight, buttonWeeklyWeightGoal)
            }
        }
    }

    private fun validateInput(heightFt: Int, userAge: Int, heightIn: Int, currentWeight: Double, goalWeight: Double): Boolean {
        if (heightFt < 3 || heightFt > 8) {
            Toast.makeText(this, "Please enter a valid height in feet.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (userAge < 18 || userAge > 99) {
            Toast.makeText(this, "Please enter a valid age.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (heightIn < 0 || heightIn > 11) {
            Toast.makeText(this, "Please enter a valid height in inches.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (currentWeight < 50.0 || currentWeight > 300.0 || goalWeight < 50.0 || goalWeight > 300.0) {
            Toast.makeText(this, "Please enter a valid weight.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun sendUserDataToFirebase(fullName: String, userAge: Int, heightFt: Int, heightIn: Int, currentWeight: Double, goalWeight: Double, weeklyGoal: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userData = mapOf(
            "fullName" to fullName,
            "age" to userAge,
            "heightFt" to heightFt,
            "heightIn" to heightIn,
            "currentWeight" to currentWeight,
            "goalWeight" to goalWeight,
            "weeklyGoal" to weeklyGoal
        )

        //time format for database for user weight and caloric logs
        val currentDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = currentDateFormat.format(Date())
        val yearWeekFormat = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault())
        val yearWeek = yearWeekFormat.format(Date())

        val weightLogData = mapOf("weight" to currentWeight)

        // Saving the weight log under a date-specific child under the user-specific log node
        database.child("user-weight-log/$userId/$yearWeek/$currentDate").setValue(weightLogData)
            .addOnSuccessListener {
                Toast.makeText(this, "Weight log updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to log weight update", Toast.LENGTH_SHORT).show()
            }

        // Updating user's physical profile
        database.child("user-physical-profile/$userId").setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@WeightLossActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save data!", Toast.LENGTH_SHORT).show()
            }
        // Updating user's caloric profile
        val calories: Int = 0
        val caloricLogData = mapOf("calories" to calories)

        database.child("user-caloric-log/$userId/$yearWeek/$currentDate").setValue(caloricLogData)
            .addOnSuccessListener {
                Toast.makeText(this, "Weight log updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to log weight update", Toast.LENGTH_SHORT).show()
            }

        //Updating user's miles done per day
        val miles: Double = 0.0
        val mileLogData = mapOf("miles" to miles)

        database.child("user-mile-log/$userId/$yearWeek/$currentDate").setValue(mileLogData)
            .addOnSuccessListener {
                Toast.makeText(this,"Miles log updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to log weight update", Toast.LENGTH_SHORT).show()
            }

    }

}
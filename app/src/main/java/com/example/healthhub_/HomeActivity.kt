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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class HomeActivity : AppCompatActivity() {
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private var calories: Double = 0.0
    private var calorDeficit: Double = 0.0
    private var miles: Double = 0.0
    private lateinit var auth : FirebaseAuth
    data class UserProfile(
        val fullName: String?,
        val heightFeet: Int?,
        val heightInches: Int?,
        val currentWeight: Double?,
        val goalWeight: Double?,
        val age: Int?,
        val sex: String?,
        val activityLevel: String?,
        val weeklyGoal: String?)

    private var userProfile: UserProfile? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = Firebase.auth

        initializeDailyLogsIfNeeded {
            fetchUserData {
                updateUI()
                calculateBMR()
            }
        }

        val weightInput = findViewById<EditText>(R.id.etUpdateWeight)
        val caloricInput = findViewById<EditText>(R.id.etUpdateCalories)
        val updateWeightButton = findViewById<Button>(R.id.btnUpdateWeight)
        val updateCaloriesButton = findViewById<Button>(R.id.btnUpdateCalories)
        //todo miles
        val mileInput = findViewById<EditText>(R.id.etUpdateMiles)
        val updateMileButton = findViewById<Button>(R.id.btnUpdateMiles)

        //go to summary report
        val summaryReportButton = findViewById<Button>(R.id.btnSummary)
        val signOutButton = findViewById<Button>(R.id.btnSignOut)


        updateWeightButton.setOnClickListener {
            val newWeightStr = weightInput.text.toString()
            if (newWeightStr.isNotEmpty()) {
                val newWeight = newWeightStr.toDouble()
                updateWeight(newWeight) {
                    fetchUserData {
                        updateUI()
                        calculateBMR()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid weight", Toast.LENGTH_SHORT).show()
            }
        }

        updateCaloriesButton.setOnClickListener {
            val newCaloricStr = caloricInput.text.toString()
            if (newCaloricStr.isNotEmpty()) {
                val newCalories = newCaloricStr.toInt()
                updateCalories(newCalories) {
                    fetchUserData {
                        updateUI()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid caloric number", Toast.LENGTH_SHORT).show()
            }
        }

        updateMileButton.setOnClickListener {
            val newMileStr = mileInput.text.toString()
            if(newMileStr.isNotEmpty()) {
                val newMiles = newMileStr.toDouble()
                updateMiles(newMiles) {
                    fetchUserData {
                        updateUI()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid caloric number", Toast.LENGTH_SHORT).show()
            }
        }

        summaryReportButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, SummaryReportActivity::class.java)
            startActivity(intent)
            finish()
        }

        signOutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent( this@HomeActivity, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun fetchUserData(callback: () -> Unit) {
        // Format for user-caloric-log
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yearWeek = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault()).format(Date())

        // References to Nodes in Database
        val userPhysicalProfileRef = FirebaseDatabase.getInstance().getReference("user-physical-profile/$userId")
        val userActivityProfileRef = FirebaseDatabase.getInstance().getReference("user-activity-profile/$userId")
        val caloricLogRef = FirebaseDatabase.getInstance().getReference("user-caloric-log/$userId/$yearWeek/$currentDate")
        val mileLogRef = FirebaseDatabase.getInstance().getReference("user-mile-log/$userId/$yearWeek/$currentDate")

        userPhysicalProfileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(physicalProfileSnapshot: DataSnapshot) {
                val fullName = physicalProfileSnapshot.child("fullName").value as String?
                val heightFeet = physicalProfileSnapshot.child("heightFt").value.toString().toIntOrNull()
                val heightInches = physicalProfileSnapshot.child("heightIn").value.toString().toIntOrNull()
                val currentWeight = physicalProfileSnapshot.child("currentWeight").value.toString().toDoubleOrNull()
                val goalWeight = physicalProfileSnapshot.child("goalWeight").value.toString().toDoubleOrNull()
                val age = physicalProfileSnapshot.child("age").value.toString().toIntOrNull()
                val weeklyGoal = physicalProfileSnapshot.child("weeklyGoal").value as String?

                userActivityProfileRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(activityProfileSnapshot: DataSnapshot) {
                        val sex = activityProfileSnapshot.child("sex").value as String?
                        val activityLevel = activityProfileSnapshot.child("activityLevel").value as String?

                        caloricLogRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(caloricSnapshot: DataSnapshot) {
                                val caloriesFromDb = caloricSnapshot.child("calories").value.toString().toDoubleOrNull() ?: 0.0

                                mileLogRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(mileSnapshot: DataSnapshot) {
                                        val milesFromDb = mileSnapshot.child("miles").value.toString().toDoubleOrNull() ?: 0.0

                                        // Update class variables
                                        calories = caloriesFromDb
                                        miles = milesFromDb

                                        // Passing Data to the UserProfile data class
                                        userProfile = UserProfile(
                                            fullName, heightFeet, heightInches, currentWeight, goalWeight, age, sex, activityLevel, weeklyGoal
                                        )

                                        // Trigger callback after all data is fetched
                                        callback()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("FirebaseDebug", "Error accessing user-mile-log: ${error.message}")
                                    }
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseDebug", "Error accessing user-caloric-log: ${error.message}")
                            }
                        })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseDebug", "Error accessing user-activity-profile: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error accessing user-physical-profile: ${error.message}")
            }
        })
    }

    private fun updateUI() {
        userProfile?.let {
            val userWeight = """
            Current Weight: ${it.currentWeight?.let { "%.1f".format(it) }} lbs -> Goal Weight: ${it.goalWeight?.let {"%.1f".format(it) }} lbs
        """.trimIndent()

            val userHeight = """
            Height: ${it.heightFeet} ft ${it.heightInches} in
        """.trimIndent()

            val userMiles = """
            ${miles?.let {"%.1f".format(miles)}} Miles Run Today    
            """.trimIndent()

            val userCalories = """
                Calories Ate: ${calories.toInt()} -> Calorie Goal: ${calorDeficit.toInt()}
            """.trimIndent()

            findViewById<TextView>(R.id.tvFullName).text = it.fullName
            findViewById<TextView>(R.id.tvUserHeight).text = userHeight
            findViewById<TextView>(R.id.tvWeight).text = userWeight
            findViewById<TextView>(R.id.tvMileIntake).text = userMiles
            findViewById<TextView>(R.id.tvCaloricIntake).text = userCalories
        }
    }

    private fun updateWeight(newWeight: Double, callback: () -> Unit) {
        val userPhysicalProfileRef = FirebaseDatabase.getInstance().getReference("user-physical-profile/$userId")
        userPhysicalProfileRef.child("currentWeight").setValue(newWeight).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Weight updated successfully", Toast.LENGTH_SHORT).show()
                checkGoalWeight(newWeight)
                callback()
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

    private fun checkGoalWeight(newWeight: Double) {
        userProfile?.let {
            if (newWeight <= it.goalWeight!!) {
                val intent = Intent(this, NewWeightGoalActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun calculateBMR() {
        userProfile?.let {
            val currentYearWeek = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault()).format(Date())
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val userWeightLogRef = FirebaseDatabase.getInstance().getReference("user-weight-log/$userId")
            userWeightLogRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(weightSnapshot: DataSnapshot) {
                    val weightData = weightSnapshot.child(currentYearWeek).child(currentDate).child("weight")
                    val weight = weightData.value.toString().toDoubleOrNull()

                    if (weight != null && it.heightFeet != null && it.heightInches != null && it.sex != null && it.age != null) {
                        val heightCm = (it.heightFeet * 30.48) + (it.heightInches * 2.54)
                        val weightKg = weight * 0.453
                        val bmr = if (it.sex == "Male") {
                            (10 * weightKg) + (6.25 * heightCm) - (5 * it.age) + 5
                        } else {
                            (10 * weightKg) + (6.25 * heightCm) - (5 * it.age) - 161
                        }
                        calorieIntakeActivityLevel(bmr)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseDebug", "Error accessing user-weight-log: ${error.message}")
                }
            })
        }
    }

    private fun calorieIntakeActivityLevel(bmr: Double) {
        userProfile?.let {
            when (it.activityLevel) {
                "Not Very Active" -> calorDeficit = bmr * 1.2
                "Lightly Active" -> calorDeficit = bmr * 1.375
                "Active" -> calorDeficit = bmr * 1.55
                "Very Active" -> calorDeficit = bmr * 1.725
            }
            calorieWeeklyGoal(calorDeficit)
        }
    }

    private fun calorieWeeklyGoal(caloric: Double) {
        userProfile?.let {
            when (it.weeklyGoal) {
                "Lose 0.5 pound/week" -> calorDeficit = (caloric - 250)
                "Lose 1 pound/week" -> calorDeficit = (caloric - 500)
                "Lose 1.5 pounds/week" -> calorDeficit = (caloric - 750)
                "Lose 2 pounds/week" -> calorDeficit = (caloric - 1000)
            }
            // Update UI
            val userCalories = """
            Calories Ate: ${calories.toInt()} -> Calorie Goal: ${calorDeficit.toInt()}
        """.trimIndent()
            findViewById<TextView>(R.id.tvCaloricIntake).text = userCalories
        }
    }

    private fun updateCalories(newCalories: Int, callback: () -> Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val yearWeekFormat = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault())
        val yearWeek = yearWeekFormat.format(Date())
        val caloricLogRef = FirebaseDatabase.getInstance().getReference("user-caloric-log/$userId/$yearWeek/$currentDate")

        // Fetch the current calories from the database
        caloricLogRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentCalories = snapshot.child("calories").value.toString().toIntOrNull() ?: 0
                val updatedCalories = currentCalories + newCalories

                // Update the calories in the database
                val logEntry = mapOf("calories" to updatedCalories)
                caloricLogRef.setValue(logEntry).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@HomeActivity, "Calories log updated successfully", Toast.LENGTH_SHORT).show()
                        // Update the local calories variable
                        calories = updatedCalories.toDouble()
                        callback()
                    } else {
                        Toast.makeText(this@HomeActivity, "Failed to log calories update", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error accessing user-caloric-log: ${error.message}")
            }
        })
    }

    private fun updateMiles(newMiles: Double, callback: () -> Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val yearWeekFormat = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault())
        val yearWeek = yearWeekFormat.format(Date())
        val mileLogRef = FirebaseDatabase.getInstance().getReference("user-mile-log/$userId/$yearWeek/$currentDate")
        val logEntry = mapOf("miles" to newMiles)

        mileLogRef.setValue(logEntry).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                Toast.makeText(this, "Weight log updated successfully", Toast.LENGTH_SHORT).show()
                callback()
            } else {
                Toast.makeText(this, "Failed to log weight update", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeDailyLogsIfNeeded(callback: () -> Unit) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yearWeek = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault()).format(Date())

        val caloricLogRef = FirebaseDatabase.getInstance().getReference("user-caloric-log/$userId/$yearWeek/$currentDate")
        val mileLogRef = FirebaseDatabase.getInstance().getReference("user-mile-log/$userId/$yearWeek/$currentDate")

        caloricLogRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(caloricSnapshot: DataSnapshot) {
                if (!caloricSnapshot.exists()) {
                    // Initialize calories to zero for the new day
                    caloricLogRef.child("calories").setValue(0.0).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FirebaseDebug", "Initialized daily calories to zero")
                        } else {
                            Log.e("FirebaseDebug", "Failed to initialize daily calories")
                        }
                    }
                }

                mileLogRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(mileSnapshot: DataSnapshot) {
                        if (!mileSnapshot.exists()) {
                            // Initialize miles to zero for the new day
                            mileLogRef.child("miles").setValue(0.0).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("FirebaseDebug", "Initialized daily miles to zero")
                                } else {
                                    Log.e("FirebaseDebug", "Failed to initialize daily miles")
                                }
                            }
                        }
                        // Trigger the callback after initialization checks
                        callback()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseDebug", "Error accessing user-mile-log: ${error.message}")
                        callback()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error accessing user-caloric-log: ${error.message}")
                callback()
            }
        })
    }
}

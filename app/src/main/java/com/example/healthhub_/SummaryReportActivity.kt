// SummaryReportActivity.kt
//
// Summary Report Activity handles the summarized health data from the user (weight and miles) to
// display to the user in visual charts
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class SummaryReportActivity : AppCompatActivity() {
    // Declares instance for firebase authentication
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    data class WeightLogEntry(val date: String, val weight: Double)
    data class MileLogEntry(val date: String, val miles: Double)

    // Initializes activity, setting the layout of the the activity, back button, and fetches weight
    // and mile logs to display in charts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary_report)

        // Initialize back button
        val backButton = findViewById<Button>(R.id.btnHomeButton)
        backButton.setOnClickListener {
            val intent = Intent(this@SummaryReportActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        fetchWeightLogs { weightLogs ->
            displayWeightLogs(weightLogs)
        }

        fetchMileLogs { mileLogs ->
            displayMileLogs(mileLogs)
        }

    }

    // fetchWeightLogs fetches weight log from the database and passes it to the callback function
    private fun fetchWeightLogs(callback: (List<WeightLogEntry>) -> Unit) {
        val userWeightLogRef = FirebaseDatabase.getInstance().getReference("user-weight-log/$userId")
        val weightLogs = mutableListOf<WeightLogEntry>()

        userWeightLogRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (weekSnapshot in dataSnapshot.children) {
                    for (daySnapshot in weekSnapshot.children) {
                        val date = daySnapshot.key ?: continue
                        val weight = daySnapshot.child("weight").value.toString().toDoubleOrNull() ?: continue
                        weightLogs.add(WeightLogEntry(date, weight))
                    }
                }
                callback(weightLogs)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error accessing user-weight-log: ${error.message}")
                callback(emptyList())
            }
        })
    }

    // displayWeightLogs sets up and customizes a line chart to display weight over time
    private fun displayWeightLogs(weightLogs: List<WeightLogEntry>) {
        val lineChart = findViewById<LineChart>(R.id.lineChart)
        val entries = ArrayList<Entry>()
        val dateFormat = SimpleDateFormat("yy-MM-dd", Locale.getDefault())
        val labels = ArrayList<String>()

        weightLogs.forEachIndexed { index, log ->
            val date = dateFormat.parse(log.date)?.time?.toFloat() ?: index.toFloat()
            entries.add(Entry(index.toFloat(), log.weight.toFloat()))
            labels.add(log.date)
        }

        val dataSet = LineDataSet(entries, "Weight Over Time")
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Customize the chart appearance
        lineChart.description.text = "Weight Log"
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.axisRight.isEnabled = false
        lineChart.invalidate() // Refresh the chart
    }

    // fetchMileLogs fetches the mile log from the database and passes it to the callback function
    private fun fetchMileLogs(callback: (List<MileLogEntry>) -> Unit) {
        val userMileLogRef = FirebaseDatabase.getInstance().getReference("user-mile-log/$userId")
        val mileLogs = mutableListOf<MileLogEntry>()

        userMileLogRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (weekSnapshot in dataSnapshot.children) {
                    for (daySnapshot in weekSnapshot.children) {
                        val date = daySnapshot.key ?: continue
                        val miles = daySnapshot.child("miles").value.toString().toDoubleOrNull() ?: continue
                        mileLogs.add(MileLogEntry(date, miles))
                    }
                }
                callback(mileLogs)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error accessing user-mile-log: ${error.message}")
                callback(emptyList())
            }
        })
    }

    // displayMileLogs sets up and customizes a bar chart to display run over time
    private fun displayMileLogs(mileLogs: List<MileLogEntry>) {
        val barChart = findViewById<BarChart>(R.id.barChart)
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        mileLogs.forEachIndexed { index, log ->
            entries.add(BarEntry(index.toFloat(), log.miles.toFloat()))
            labels.add(log.date)
        }

        val dataSet = BarDataSet(entries, "Miles Run Over Time")
        val barData = BarData(dataSet)
        barChart.data = barData

        // Customize the chart appearance
        barChart.description.text = "Mile Log"
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.axisRight.isEnabled = false
        barChart.invalidate() // Refresh the chart
    }
}

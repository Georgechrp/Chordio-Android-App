package com.unipi.george.chordshub.viewmodels.user

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.State


data class DayStat(val day: String, val minutes: String)

class WeeklyStatsViewModel : ViewModel() {

    private val _weeklyStats = mutableStateOf<List<DayStat>>(emptyList())
    val weeklyStats: State<List<DayStat>> = _weeklyStats

    private val db = FirebaseFirestore.getInstance()

    fun fetchWeeklyStats(userId: String) {
        println("📥 Fetching stats for userId = $userId")

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                println("Document data: ${document.data}")
                if (!document.exists()) {
                    println("❌ Document does not exist")
                    return@addOnSuccessListener
                }

                val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                val stats = mutableListOf<DayStat>()

                val nestedMap = document.get("totalTimeSpent") as? Map<*, *>
                println("🗺 totalTimeSpent map: $nestedMap")

                for (day in days) {
                    val value = when {
                        nestedMap?.containsKey(day) == true -> nestedMap[day]
                        document.contains("totalTimeSpent.$day") -> document.get("totalTimeSpent.$day")
                        else -> "-"
                    }
                    val text = if (value == "-" || value == null) "-" else "$value min"
                    stats.add(DayStat(day, text))
                }

                println("Weekly Stats Parsed: $stats")
                _weeklyStats.value = stats
            }
            .addOnFailureListener { e ->
                println(" Error fetching stats: ${e.message}")
            }
    }

}

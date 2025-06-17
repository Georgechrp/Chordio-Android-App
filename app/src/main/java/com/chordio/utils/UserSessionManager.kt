package com.chordio.utils

import com.chordio.repository.UserStatsRepository
import java.text.SimpleDateFormat
import java.util.*

class UserSessionManager(private val repository: UserStatsRepository) {
    private var startTime: Long = 0
    private var userId: String? = null

    // Μετατροπή ημερομηνίας στη μορφή "yyyy-MM-dd"
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Επιστρέφει τη Δευτέρα της τρέχουσας εβδομάδας
    private fun getCurrentWeekStartDate(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return dateFormatter.format(calendar.time)
    }

    // Επιστρέφει την ημέρα της εβδομάδας (π.χ. "Monday")
    private fun getCurrentDayOfWeek(): String {
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
    }

    fun startSession(userId: String?) {
        this.userId = userId
        startTime = System.currentTimeMillis()
    }

    fun endSession(isChangingConfigurations: Boolean) {
        if (isChangingConfigurations) return

        val elapsedTime = (System.currentTimeMillis() - startTime) / 60000
        if (userId != null && elapsedTime > 0) {
            repository.updateWeeklyStats(userId!!, getCurrentDayOfWeek(), elapsedTime.toInt())
        }
    }
}

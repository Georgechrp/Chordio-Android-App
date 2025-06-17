package com.chordio.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

class TransposePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("transpose_prefs", Context.MODE_PRIVATE)

    // Αποθήκευση της τονικότητας για το συγκεκριμένο τραγούδι
    fun saveTransposeValue(songId: String, transposeValue: Int) {
        prefs.edit().putInt("transpose_$songId", transposeValue).apply()
    }

    // Ανάκτηση της αποθηκευμένης τονικότητας (ή 0 αν δεν έχει οριστεί)
    fun getTransposeValue(songId: String): Int {
        return prefs.getInt("transpose_$songId", 0)
    }
}

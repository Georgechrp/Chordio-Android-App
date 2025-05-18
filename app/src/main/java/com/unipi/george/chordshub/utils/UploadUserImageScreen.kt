package com.unipi.george.chordshub.utils

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

suspend fun uploadImageToCloudinary(imageUri: Uri, context: android.content.Context): String? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }

            val cloudinaryUrl = "https://api.cloudinary.com/v1_1/ddqf5osur/image/upload"
            val client = OkHttpClient()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.name, RequestBody.create("image/jpeg".toMediaTypeOrNull(), tempFile))
                .addFormDataPart("upload_preset", "YOUR_UPLOAD_PRESET")
                .build()

            val request = Request.Builder()
                .url(cloudinaryUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonObject = org.json.JSONObject(responseBody)
                return@withContext jsonObject.getString("secure_url") // ✅ Επιστρέφουμε το URL της εικόνας
            } else {
                Log.e("Cloudinary", "Upload failed: $responseBody")
            }
        } catch (e: Exception) {
            Log.e("Cloudinary", "Exception: ${e.message}")
        }
        null
    }
}


suspend fun updateUserProfileImage(userId: String, imageUrl: String) {
    val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

    userRef.update("profileImageUrl", imageUrl)
        .addOnSuccessListener {
            Log.d("Firestore", " Profile image updated successfully!")
        }
        .addOnFailureListener {
            Log.e("Firestore", " Failed to update profile image: ${it.message}")
        }
}

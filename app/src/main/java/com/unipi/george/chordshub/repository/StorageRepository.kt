package com.unipi.george.chordshub.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository() {

    suspend fun uploadImageToFirebaseStorage(uri: Uri, userId: String): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileRef = storageRef.child("profile_images/$userId")

            fileRef.putFile(uri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()

            downloadUrl
        } catch (e: Exception)  {
            Log.e("FirebaseStorage", "Error uploading image", e)
            null
        }
    }

    suspend fun getProfileImageUrl(userId: String): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileRef = storageRef.child("profile_images/$userId")
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error fetching profile image", e)
            null
        }
    }


}
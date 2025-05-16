package com.example.shoesstoreapp.controller

import android.util.Log
import com.example.shoesstoreapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.text.get

class UserController {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null

        return try {
            val document = usersCollection.document(currentUser.uid).get().await()
            if (document.exists()) {
                val user = User(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    email = document.getString("email") ?: "",
                    phone = document.getString("phone") ?: "",
                    address = document.getString("address") ?: "",
                    photoUrl = document.getString("photoUrl") ?: "",
                    role = document.getString("role") ?: "user"
                )
                user
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UserController", "Error getting user data", e)
            null
        }
    }

    // Update user profile in Firestore
    suspend fun updateUser(
        userId: String,
        name: String,
        email: String,
        phone: String,
        address: String,
        imagePath : String,
        role: String

    ): Boolean {
        return try {
            val cloudinaryManager = CloudinaryHelp()
            val imageUrl = cloudinaryManager.uploadImage(imagePath) ?: ""
            val userData = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "address" to address,
                "photoUrl" to imageUrl,
                "role" to role
            )

            usersCollection.document(userId).set(userData).await()
            true
        } catch (e: Exception) {
            Log.e("UserController", "Error updating user data", e)
            false
        }
    }
     fun logout(): Boolean {
        return try {
            auth.signOut()
            true
        } catch (e: Exception) {
            Log.e("UserController", "Error signing out", e)
            false
        }
    }
    fun getUserById(userId: String, onComplete: (User?) -> Unit) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = User(
                        id = document.id,
                        name = document.getString("name") ?: "Anonymous User",
                        email = document.getString("email") ?: "",
                        photoUrl = document.getString("photoUrl") ?: "",
                        phone = document.getString("phone") ?: "",
                        address = document.getString("address") ?: "",
                        role = document.getString("role") ?: "user"

                        // Other fields as needed
                    )
                    onComplete(user)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }
}
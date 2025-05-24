package com.example.shoesstoreapp.controller



import com.example.shoesstoreapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Random



class AuthController{
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    suspend fun register(email : String , password : String ): Boolean{
        return try {
            val result =  auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return false
            val randomUsername = generateRandomUsername()
            val user = User(
                id = userId,
                name = randomUsername,
                email = email,
                phone = "",
                address = "",
                photoUrl = "",
                role = "user"
            )
            usersCollection.document(userId).set(user).await()
            true

        } catch (e: Exception) {
            false
        }
    }
    suspend fun login(email: String, password: String): LoginResult {
        return try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid ?: return LoginResult.Error("Login failed")

        val userDoc = usersCollection.document(userId).get().await()
        if (userDoc.exists()) {
            val role = userDoc.getString("role") ?: "user"
            LoginResult.Success(role)
        } else {
            LoginResult.Error("User data not found")
        }
    } catch (e: Exception) {
        LoginResult.Error(e.message ?: "Login failed")
    }
    }
    // Add to AuthController.kt
    suspend fun signInWithGoogle(idToken: String): LoginResult {
        return try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: return  LoginResult.Error("Google sign-in failed")


        // Check if user exists in Firestore
        val userId = user.uid
        val userDoc = usersCollection.document(userId).get().await()
        if (!userDoc.exists()) {
            // First time Google sign-in, create user document
            val randomUsername = generateRandomUsername()
            val newUser = User(
                id = userId,
                name = randomUsername,
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: "",
                phone = "",
                address = "",
                role = "user",
            )

            // Save to Firestore
            usersCollection.document(userId).set(newUser).await()
            return LoginResult.Success("user")
        }
        else {
            val role = userDoc.getString("role") ?: "user"
            return LoginResult.Success(role)
        }

        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Google sign in failed")

        }
    }

    fun logout() {
        auth.signOut()
    }
    private fun generateRandomUsername(): String {
        val random = (100000 + Random().nextInt(900000)).toString() // 6-digit random number
        return "User$random"
    }

suspend fun getCurrentUserRole(): String {
    val currentUser = auth.currentUser ?: return "user"
    return try {
        val userDoc = usersCollection.document(currentUser.uid).get().await()
        userDoc.getString("role") ?: "user"
    } catch (e: Exception) {
        "user"
    }
}
    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to send reset email")
            }
    }
    fun verifyPasswordResetCode(code: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        auth.verifyPasswordResetCode(code)
            .addOnSuccessListener { email ->
                onSuccess(email)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Invalid verification code")
            }
    }
    fun confirmPasswordReset(code: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.confirmPasswordReset(code, newPassword)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to reset password")
            }
    }

}
sealed class LoginResult {
    data class Success(val role: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}
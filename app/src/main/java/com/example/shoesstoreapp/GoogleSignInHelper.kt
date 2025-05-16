package com.example.shoesstoreapppackage

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task



import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavHostController
import com.example.shoesstoreapp.controller.AuthController
import com.example.shoesstoreapp.controller.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleSignInHelper(
    private val activity: ComponentActivity,
    private val authController: AuthController

) {
    private val googleSignInClient: GoogleSignInClient
    private lateinit var navController: NavHostController
    private val signInLauncher: ActivityResultLauncher<Intent>

    init {
        // Configure Google Sign-In với Web Client ID từ Firebase Console
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("736845084802-1ae66ke0g56a3unsj4kecp586s2bnadr.apps.googleusercontent.com") // Thay bằng Web Client ID thực tế
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)

        // Initialize ActivityResultLauncher
        signInLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            } else {
                println("Google Sign-In failed with result code: ${result.resultCode}")
            }
        }
    }

    fun setNavController(controller: NavHostController) {
        this.navController = controller
    }

    fun getGoogleSignInClient(): GoogleSignInClient = googleSignInClient

    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account.idToken?.let { token ->
                // Gọi hàm signInWithGoogle trong Coroutine
                CoroutineScope(Dispatchers.Main).launch {
                    when (val result = authController.signInWithGoogle(token)) {
                        is LoginResult.Success -> {
                            // Điều hướng đến màn hình chính sau khi đăng nhập thành công
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true

                            }
                        }

                        is LoginResult.Error -> {
                            println("Firebase authentication with Google failed")
                        }
                    }
                }
                } ?: println("ID Token is null")
            } catch (e: ApiException) {
                println("Google Sign-In failed: ${e.statusCode} - ${e.message}")
            }
        }
    }

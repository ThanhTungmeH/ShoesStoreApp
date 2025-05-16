package com.example.shoesstoreapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.navigation.compose.rememberNavController
import com.example.shoesstoreapp.controller.AuthController


import com.example.shoesstoreapppackage.GoogleSignInHelper


class MainActivity : ComponentActivity() {
    // Declare as properties of MainActivity, not inside onCreate
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private lateinit var authController: AuthController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize BEFORE setContent - this is crucial
        authController = AuthController()
        googleSignInHelper = GoogleSignInHelper(this, authController)

        setContent {
            val navController = rememberNavController()
            googleSignInHelper.setNavController(navController)

            AppNavGraph(
                navController = navController,
                authController = authController,
                googleSignInClient = googleSignInHelper.getGoogleSignInClient(),
                signIn = { googleSignInHelper.signInWithGoogle() }
            )
        }
    }
}
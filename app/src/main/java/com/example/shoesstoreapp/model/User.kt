package com.example.shoesstoreapp.model

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val photoUrl: String = "",
    val role: String = "user"
)
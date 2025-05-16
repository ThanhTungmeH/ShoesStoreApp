package com.example.shoesstoreapp.model

data class Review(
    val userId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
package com.example.shoesstoreapp.model

data class Product(
    val id: String ="",
    val name: String = "",
    val brand: String = "",
    val price: Double = 0.0,
    val size: List<String> = listOf(),
    val image: String= "",
    val quantity: Int = 0,
    val description: String = "",
    val reviews: List<Review> = listOf(),
    val rating: Float = 0.0f
)
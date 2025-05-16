package com.example.shoesstoreapp.model

data class CartItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val productPrice: Double = 0.0,
    val productBrand: String = "",
    val quantity: Int = 1,
    val size: String = "",
    val userId: String = ""
) {
    // Required empty constructor for Firestore
    constructor() : this(
        id = "",
        productId = "",
        productName = "",
        productImage = "",
        productPrice = 0.0,
        productBrand = "",
        quantity = 1,
        size = "",
        userId = ""
    )
}
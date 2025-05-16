package com.example.shoesstoreapp.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val shippingAddress: String = "",
    val trackingNumber: String = "",
    val estimatedDeliveryTime: Long? = null
)
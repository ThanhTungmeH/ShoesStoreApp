package com.example.shoesstoreapp.model

data class OrderStats(
    val totalOrders: Int,
    val completedOrders: Int,
    val pendingOrders: Int,
    val cancelledOrders: Int,
    val averageOrderValue: Double
)
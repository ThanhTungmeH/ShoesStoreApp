package com.example.shoesstoreapp.model

enum class OrderStatus {
    PENDING,      // Đơn hàng mới đặt
    CONFIRMED,    // Đã xác nhận đơn hàng
    PREPARING,    // Đang chuẩn bị hàng
    SHIPPING,     // Đang giao hàng
    DELIVERED,    // Đã giao hàng
    CANCELLED     // Đã hủy
}
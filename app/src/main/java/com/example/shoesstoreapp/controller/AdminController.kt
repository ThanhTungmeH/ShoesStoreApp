package com.example.shoesstoreapp.controller

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.text.get

class AdminController {
    private val db = FirebaseFirestore.getInstance()
    suspend fun getTotalUsers(): Long {
        val snapshot = db.collection("users").get().await()
        return snapshot.size().toLong()
    }

    suspend fun getTotalProducts(): Long {
        val snapshot = db.collection("products").get().await()
        return snapshot.size().toLong()
    }

    suspend fun getTotalOrders(): Long {
        val snapshot = db.collection("orders").get().await()
        return snapshot.size().toLong()
    }

    suspend fun getTotalRevenue(): Double {
        var total = 0.0
        val snapshot = db.collection("orders").get().await()
        for (doc in snapshot.documents) {
            val status = doc.getString("status") ?: ""
            if (status == "DELIVERED") {
                val price = doc.getDouble("totalAmount") ?: 0.0
                total += price
            }
        }
        return total
    }
}
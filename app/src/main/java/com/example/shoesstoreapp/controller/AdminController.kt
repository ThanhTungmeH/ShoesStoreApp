package com.example.shoesstoreapp.controller

import android.util.Log
import com.example.shoesstoreapp.model.Order
import com.example.shoesstoreapp.model.OrderStats
import com.example.shoesstoreapp.model.OrderStatus
import com.example.shoesstoreapp.model.ProductStats
import com.example.shoesstoreapp.model.SalesDataPoint
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.get
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.java
import kotlin.text.clear
import kotlin.text.format
import kotlin.text.get
import kotlin.text.set

class AdminController {
    private val db = FirebaseFirestore.getInstance()
    private val salesCollection = db.collection("orders")
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

    // Get real sales data from Firebase
    suspend fun getSalesData(period: String): List<SalesDataPoint> = suspendCoroutine { continuation ->
        val salesData = mutableListOf<SalesDataPoint>()
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        // Set start time based on period
        when (period) {
            "daily" -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            "monthly" -> calendar.add(Calendar.MONTH, -1)
            "yearly" -> calendar.add(Calendar.YEAR, -1)
            else -> calendar.add(Calendar.MONTH, -1)
        }
        val startTime = calendar.timeInMillis

        salesCollection
            .whereGreaterThanOrEqualTo("createdAt", startTime)
            .whereLessThanOrEqualTo("createdAt", endTime)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                // Group data by time periods based on selected interval
                val groupedData = when (period) {
                    "daily" -> groupByDays(documents.documents)
                    "weekly" -> groupByWeeks(documents.documents)
                    "monthly" -> groupByMonths(documents.documents)
                    "yearly" -> groupByMonths(documents.documents)
                    else -> groupByDays(documents.documents)
                }
                continuation.resume(groupedData)
            }
            .addOnFailureListener { e ->
                Log.e("AdminController", "Error getting sales data", e)
                continuation.resume(emptyList())
            }
    }





    // Helper methods for grouping data
    private fun groupByHours(documents: List<DocumentSnapshot>): List<SalesDataPoint> {
        val hourlyData = (0..23).associateWith { 0.0 }.toMutableMap()

        for (document in documents) {
            val order = document.toObject(Order::class.java) ?: continue
            // Only count DELIVERED orders
            if (order.status != OrderStatus.DELIVERED) continue
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = order.createdAt
            val hour = calendar.get(Calendar.HOUR_OF_DAY)

            hourlyData[hour] = (hourlyData[hour] ?: 0.0) + order.totalAmount
        }

        return (8..22).map { hour ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            SalesDataPoint("${hour}h", hourlyData[hour]!!, calendar.timeInMillis)
        }
    }

    private fun groupByDays(documents: List<DocumentSnapshot>): List<SalesDataPoint> {
        val dailyData = mutableMapOf<String, Double>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (document in documents) {
            val order = document.toObject(Order::class.java) ?: continue
            // Only count DELIVERED orders
            if (order.status != OrderStatus.DELIVERED) continue
            val date = Date(order.createdAt)
            val dateString = dateFormat.format(date)

            dailyData[dateString] = (dailyData[dateString] ?: 0.0) + order.totalAmount
        }
        val displayFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        return dailyData.map { (dateStr, amount) ->
            val date = dateFormat.parse(dateStr) ?: Date()
            val displayLabel = displayFormat.format(date)
            SalesDataPoint(dateStr, amount, date.time)
        }.sortedBy { it.date }
    }
    // Add this function for weekly grouping
    private fun groupByWeeks(documents: List<DocumentSnapshot>): List<SalesDataPoint> {
        val weeklyData = mutableMapOf<String, Double>()
        val calendar = Calendar.getInstance()

        for (document in documents) {
            val order = document.toObject(Order::class.java) ?: continue
            // Only count DELIVERED orders
            if (order.status != OrderStatus.DELIVERED) continue
            calendar.timeInMillis = order.createdAt

            // Get year and week number
            val year = calendar.get(Calendar.YEAR)
            val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
            val weekKey = "$year-W$weekOfYear"

            weeklyData[weekKey] = (weeklyData[weekKey] ?: 0.0) + order.totalAmount
        }

        return weeklyData.map { (weekStr, amount) ->
            // Extract week information
            val parts = weekStr.split("-W")
            val year = parts[0].toInt()
            val week = parts[1].toInt()

            // Create a calendar for this week
            val cal = Calendar.getInstance()
            cal.clear()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.WEEK_OF_YEAR, week)

            SalesDataPoint("W$week", amount, cal.timeInMillis)
        }.sortedBy { it.date }
    }
    private fun groupByMonths(documents: List<DocumentSnapshot>): List<SalesDataPoint> {
        val monthlyData = mutableMapOf<String, Double>()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        for (document in documents) {
            val order = document.toObject(Order::class.java) ?: continue
            // Only count DELIVERED orders
            if (order.status != OrderStatus.DELIVERED) continue
            val date = Date(order.createdAt)
            val monthString = monthFormat.format(date)

            monthlyData[monthString] = (monthlyData[monthString] ?: 0.0) + order.totalAmount
        }

        // Convert to list and sort by month
        val calendar = Calendar.getInstance()
        return monthlyData.map { (monthStr, amount) ->
            calendar.time = monthFormat.parse(monthStr) ?: Date()
            SalesDataPoint(monthStr, amount, calendar.timeInMillis)
        }.sortedBy { it.date }
    }
}
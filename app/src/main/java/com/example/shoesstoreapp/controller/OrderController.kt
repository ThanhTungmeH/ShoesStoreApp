package com.example.shoesstoreapp.controller

import android.util.Log
import com.example.shoesstoreapp.model.CartItem
import com.example.shoesstoreapp.model.Order
import com.example.shoesstoreapp.model.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlin.text.get
import kotlin.text.toInt

class OrderController {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val ordersCollection = db.collection("orders")
private val productController = ProductController()
    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder

    suspend fun createOrder(
        items: List<CartItem>,
        totalAmount: Double,
        shippingAddress: String,
        onSuccess: (String) -> Unit = {}, // Trả về orderId
        onError: (String) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            for (item in items) {
                val success = productController.decreaseProductQuantity(item.productId, item.quantity)
                if (!success) {
                    onError("Không đủ số lượng sản phẩm ${item.productName}")
                    return
                }
            }
            val order = Order(
                userId = userId,
                items = items,
                totalAmount = totalAmount,
                shippingAddress = shippingAddress,
                trackingNumber = generateTrackingNumber()
            )

            val docRef = ordersCollection.add(order).await()
            // Update the order with its ID
            ordersCollection.document(docRef.id)
                .update("id", docRef.id)
                .await()
            onSuccess(docRef.id)
        } catch (e: Exception) {
            onError(e.message ?: "Failed to create order")
        }
    }

    fun listenToOrderStatus(orderId: String) {
        ordersCollection.document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                snapshot?.let { doc ->
                    val order = doc.toObject(Order::class.java)
                    _currentOrder.value = order
                }
            }
    }

    private fun generateTrackingNumber(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }
    fun getAllOrders(
        onSuccess: (List<Order>) -> Unit,
        onError: (String) -> Unit
    ) {
        ordersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Error fetching orders")
                    return@addSnapshotListener
                }

                val ordersList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)
                } ?: emptyList()

                onSuccess(ordersList)
            }
    }
    fun getUserOrders(
        onSuccess: (List<Order>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("User not authenticated")

        ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Error fetching orders")
                    return@addSnapshotListener
                }

                val ordersList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)
                } ?: emptyList()

                onSuccess(ordersList)
            }
    }
    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            ordersCollection.document(orderId)
                .update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Error updating order status")
        }
    }

    suspend fun updateOrderPaymentStatus(
        orderId: String,
        status: String,
        transactionId: String
    ) {
        try {
            val db = FirebaseFirestore.getInstance()
            val orderRef = db.collection("orders").document(orderId)

            val updates = hashMapOf<String, Any>(
                "paymentStatus" to status,
                "transactionId" to transactionId,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            orderRef.update(updates).await()
        } catch (e: Exception) {
            Log.e("OrderController", "Error updating payment status: ${e.message}")
            throw e
        }
    }
}


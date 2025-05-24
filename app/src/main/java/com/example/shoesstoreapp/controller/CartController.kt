package com.example.shoesstoreapp.controller

import com.example.shoesstoreapp.model.CartItem
import com.example.shoesstoreapp.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.text.set


class CartController {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()



    fun listenToUserCart(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _isLoading.value = true
        val userId = auth.currentUser?.uid ?: "guest"

        db.collection("carts")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoading.value = false
                    onError(error.message ?: "Error fetching cart")
                    return@addSnapshotListener
                }

                val items = mutableListOf<CartItem>()
                for (doc in snapshot?.documents ?: listOf()) {
                    try {
                        val cartItem = CartItem(
                            id = doc.id,
                            productId = doc.getString("productId") ?: "",
                            productName = doc.getString("productName") ?: "",
                            productImage = doc.getString("productImage") ?: "",
                            productPrice = doc.getDouble("productPrice") ?: 0.0,
                            productBrand = doc.getString("productBrand") ?: "",
                            quantity = doc.getLong("quantity")?.toInt() ?: 1,
                            size = doc.getString("size") ?: "",
                            userId = doc.getString("userId") ?: ""
                        )
                        items.add(cartItem)
                    } catch (e: Exception) {
                        onError("Error parsing cart item: ${e.message}")
                    }
                }

                _cartItems.value = items
                _isLoading.value = false
                onSuccess()
            }
    }

    fun addToCart(
        product: Product,
        quantity: Int,
        size: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val userId = auth.currentUser?.uid ?: "guest"


        // Check if the item already exists in cart with same size
        val existingItem = _cartItems.value.find {
            it.productId == product.id && it.size == size
        }

        if (existingItem != null) {
            // Update quantity
            updateCartItemQuantity(
                existingItem.id,
                existingItem.quantity + quantity,
                onSuccess,
                onError
            )
            return
        }

        // Create new cart item
        val cartItem = hashMapOf(
            "productId" to product.id,
            "productName" to product.name,
            "productImage" to product.image,
            "productPrice" to product.price,
            "productBrand" to product.brand,
            "quantity" to quantity,
            "size" to size,
            "userId" to userId
        )

        db.collection("carts")
            .add(cartItem)
            .addOnSuccessListener { doc ->
                // Update the id
                db.collection("carts").document(doc.id)
                    .update("id", doc.id)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Error updating cart item")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error adding to cart")
            }
    }

    fun updateCartItemQuantity(
        itemId: String,
        newQuantity: Int,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (newQuantity <= 0) {
            removeCartItem(itemId, onSuccess, onError)
            return
        }

        db.collection("carts").document(itemId)
            .update("quantity", newQuantity)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error updating quantity")
            }
    }

    fun removeCartItem(
        itemId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        db.collection("carts").document(itemId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error removing item")
            }
    }

    fun getCartTotal(): Double {
        return _cartItems.value.sumOf { it.productPrice * it.quantity }
    }

    fun clearCart(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val userId = auth.currentUser?.uid ?: "guest"
        if (userId == "guest") {
            onError("Please login to manage cart")
            return
        }

        db.collection("carts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (doc in documents) {
                    batch.delete(doc.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Error clearing cart")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error fetching cart items")
            }
    }
}
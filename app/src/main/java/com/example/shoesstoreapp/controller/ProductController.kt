package com.example.shoesstoreapp.controller


import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.shoesstoreapp.model.Product
import com.example.shoesstoreapp.model.Review
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.text.get
import kotlin.text.toFloat
import kotlin.text.toInt

class ProductController {
    private val db = FirebaseFirestore.getInstance()
    private val _products = mutableStateListOf<Product>()
    val products: List<Product> get() = _products

    // Lắng nghe và cập nhật danh sách sản phẩm từ Firestore
    fun listenToProducts(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        db.collection("products")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                snapshots?.let {
                    _products.clear()
                    _products.addAll(it.map { doc ->
                        Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            brand = doc.getString("brand") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            size = doc.get("sizes") as? List<String> ?: emptyList(),
                            quantity = doc.getLong("quantity")?.toInt() ?: 0,
                            image = doc.get("image") as? String ?: "",
                            description = doc.getString("description") ?: "",
                            rating = (doc.getDouble("rating")?.toFloat() ?: 0.0f),
                            reviews = (doc.get("reviews") as? List<Map<String, Any>>)?.map {
                                Review(
                                    userId = it["userId"] as? String ?: "",
                                    rating = (it["rating"] as? Double)?.toFloat() ?: 0f,
                                    comment = it["comment"] as? String ?: "",
                                    timestamp = (it["timestamp"] as? Long) ?: 0L
                                )
                            } ?: emptyList()
                        )
                    })
                    onSuccess()
                }
            }
    }

    suspend fun addReview(
        productId: String,
        userId: String,
        rating: Float,
        comment: String
    ): Boolean {
        return try {
            val review = hashMapOf(
                "userId" to userId,
                "rating" to rating,
                "comment" to comment,
                "timestamp" to System.currentTimeMillis()
            )

            // Add review to reviews array
            db.collection("products").document(productId)
                .update("reviews", FieldValue.arrayUnion(review))
                .await()

            // Update average rating
            val product = db.collection("products").document(productId).get().await()
            val reviews = product.get("reviews") as? List<Map<String, Any>> ?: emptyList()
            val averageRating = reviews.map { (it["rating"] as? Double)?.toFloat() ?: 0f }.average()

            db.collection("products").document(productId)
                .update("rating", averageRating)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateReview(
        productId: String,
        userId: String,
        rating: Float,
        comment: String
    ): Boolean {
        return try {
            val product = db.collection("products").document(productId).get().await()
            val reviews = product.get("reviews") as? List<Map<String, Any>> ?: emptyList()

            // Remove old review
            val oldReview = reviews.find { it["userId"] == userId }
            if (oldReview != null) {
                db.collection("products").document(productId)
                    .update("reviews", FieldValue.arrayRemove(oldReview))
                    .await()
            }

            // Add new review
            val newReview = hashMapOf(
                "userId" to userId,
                "rating" to rating,
                "comment" to comment,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("products").document(productId)
                .update("reviews", FieldValue.arrayUnion(newReview))
                .await()

            // Update average rating
            val updatedProduct = db.collection("products").document(productId).get().await()
            val updatedReviews = updatedProduct.get("reviews") as? List<Map<String, Any>> ?: emptyList()
            val averageRating = updatedReviews.map { (it["rating"] as? Double)?.toFloat() ?: 0f }.average()


            db.collection("products").document(productId)
                .update("rating", averageRating)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    // Thêm sản phẩm mới vào Firestore
    suspend fun addProduct(
        name: String,
        price: Double,
        description: String,
        brand: String,
        sizes: List<String>,
        quantity: Int,
        imagePath: String,
        reviews: List<Review> = emptyList()

    ): Boolean {
        return try {
            val cloudinaryManager = CloudinaryHelp()
            val imageUrl = cloudinaryManager.uploadImage(imagePath) ?: ""
            val productData = hashMapOf(
                "name" to name,
                "price" to price,
                "description" to description,
                "brand" to brand,
                "sizes" to sizes,
                "image" to imageUrl,
                "quantity" to quantity,
                "reviews" to reviews,
            )

            db.collection("products")
                .add(productData)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            db.collection("products")
                .document(productId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun updateProduct(
        productId: String,
        name: String,
        price: Double,
        description: String,
        brand: String,
        sizes: List<String>,
        quantity: Int,
        imagePath: String,
        reviews: List<Review> = emptyList()
    ): Boolean {
        return try {
            val imageUrl = if (imagePath.startsWith("http")) {
                // Ảnh chưa đổi → giữ nguyên URL
                imagePath
            } else {
                // Ảnh mới → upload lên Cloudinary
                val cloudinaryManager = CloudinaryHelp()
                cloudinaryManager.uploadImage(imagePath) ?: ""
            }
            val productData = hashMapOf(
                "name" to name,
                "price" to price,
                "description" to description,
                "brand" to brand,
                "sizes" to sizes,
                "image" to imageUrl,
                "quantity" to quantity,
                "reviews" to reviews,
            )

            db.collection("products")
                .document(productId)
                .set(productData)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun getProductById(productId: String, onSuccess: (Product) -> Unit) {
        try {
            val document = db.collection("products").document(productId).get().await()
            if (document.exists()) {
                val product = Product(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    brand = document.getString("brand") ?: "",
                    price = document.getDouble("price") ?: 0.0,
                    size = document.get("sizes") as? List<String> ?: emptyList(),
                    quantity = document.getLong("quantity")?.toInt() ?: 0,
                    image = document.getString("image") ?: "",
                    description = document.getString("description") ?: "",
                    rating = (document.getDouble("rating")?.toFloat() ?: 0.0f),
                    reviews = (document.get("reviews") as? List<Map<String, Any>>)?.map {
                        Review(
                            userId = it["userId"] as? String ?: "",
                            rating = (it["rating"] as? Double)?.toFloat() ?: 0f,
                            comment = it["comment"] as? String ?: "",
                            timestamp = (it["timestamp"] as? Long) ?: 0L
                        )
                    } ?: emptyList()
                )
                onSuccess(product)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}

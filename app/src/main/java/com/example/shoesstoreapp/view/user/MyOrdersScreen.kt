package com.example.shoesstoreapp.view.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shoesstoreapp.controller.OrderController
import com.example.shoesstoreapp.controller.ProductController
import com.example.shoesstoreapp.model.Order
import com.example.shoesstoreapp.model.OrderStatus
import com.example.shoesstoreapp.model.Product
import com.example.shoesstoreapp.model.Review
import com.example.shoesstoreapp.view.admin.StatusChip
import com.example.shoesstoreapp.view.component.BottomNavigation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    navController: NavController,
    orderController: OrderController = remember { OrderController() },
    productController: ProductController = remember { ProductController() }
) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var selectedStatus by remember { mutableStateOf<OrderStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var productToReview by remember { mutableStateOf<Product?>(null) }
    var existingReview by remember { mutableStateOf<Review?>(null) }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedStatus) {
        orderController.getUserOrders(
            onSuccess = { ordersList ->
                orders = if (selectedStatus != null) {
                    ordersList.filter { it.status == selectedStatus }
                } else {
                    ordersList
                }
                isLoading = false
            },
            onError = {
                isLoading = false
            }
        )
    }
    productToReview?.let { product ->
        val userReview = product.reviews.find { it.userId == currentUserId }

        ReviewDialog(
            product = product,
            initialRating = userReview?.rating ?: 0f,
            initialComment = userReview?.comment ?: "",
            isEdit = userReview != null,
            onDismiss = { productToReview = null },
            onSubmit = { rating, comment ->
                // Submit review
                scope.launch {
                    if (userReview != null) {
                        // Update existing review
                        productController.updateReview(
                            productId = product.id,
                            userId = currentUserId,
                            rating = rating,
                            comment = comment
                        )
                    } else {
                        // Add new review
                        productController.addReview(
                            productId = product.id,
                            userId = currentUserId,
                            rating = rating,
                            comment = comment
                        )
                    }
                    productToReview = null
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                currentRoute = "my_orders"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = if (selectedStatus == null) 0 else OrderStatus.values().indexOf(selectedStatus) + 1,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null },
                    text = { Text("All") }
                )
                OrderStatus.values().forEach { status ->
                    Tab(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        text = { Text(status.name) }
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No orders found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(
                            order = order,
                            onClick = { navController.navigate("order_tracking/${order.id}") },
                            onReviewClick = { product ->
                                // When review button is clicked, load full product details
                                scope.launch {
                                    productController.getProductById(product.id) { fullProduct ->
                                        productToReview = fullProduct
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    onReviewClick: (Product) -> Unit = {} // Add parameter for review action
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Order #${order.id.take(8)}",
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Product Image
                    AsyncImage(
                        model = item.productImage,
                        contentDescription = item.productName,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop
                    )

                    // Product Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.productName,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Size: ${item.size} | Quantity: ${item.quantity}",
                            color = Color.Gray
                        )
                        Text(
                            "$${String.format("%.2f", item.productPrice * item.quantity)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (order.status == OrderStatus.DELIVERED) {
                        IconButton(onClick = {
                            // Create temporary Product object from OrderItem
                            val product = Product(
                                id = item.productId,
                                name = item.productName,
                                image = item.productImage,
                                price = item.productPrice
                            )
                            onReviewClick(product)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Reviews,
                                contentDescription = "Review product",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total Amount",
                        color = Color.Gray
                    )
                    Text(
                        "$${String.format("%.2f", order.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    formatDate1(order.createdAt),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
@Composable
fun ReviewDialog(
    product: Product,
    initialRating: Float = 0f,
    initialComment: String = "",
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Product") },
        text = {
            Column {
                // Product info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = product.image,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text(product.brand, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i.toFloat() }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comment
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your Review") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = rating > 0
            ) {
                Text(if (isEdit) "Update" else "Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


private fun formatDate1(timestamp: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}

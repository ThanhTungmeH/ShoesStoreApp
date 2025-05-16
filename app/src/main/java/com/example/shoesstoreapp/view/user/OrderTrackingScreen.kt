package com.example.shoesstoreapp.view.user
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoesstoreapp.controller.OrderController
import com.example.shoesstoreapp.model.OrderStatus
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBackClick: () -> Unit,
    orderController: OrderController = remember { OrderController() }
) {
    val order by orderController.currentOrder.collectAsState()

    LaunchedEffect(orderId) {
        orderController.listenToOrderStatus(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Order") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            order?.let { currentOrder ->
                // Tracking Number
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Tracking Number",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            currentOrder.trackingNumber,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Status Timeline
                OrderStatusTimeline(currentOrder.status)

                Spacer(modifier = Modifier.height(24.dp))

                // Estimated Delivery
                currentOrder.estimatedDeliveryTime?.let { estimatedTime ->
                    Text(
                        "Estimated Delivery",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Expected by ${formatDate(estimatedTime)}",
                        color = Color.Gray
                    )
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun OrderStatusTimeline(currentStatus: OrderStatus) {
    val statuses = OrderStatus.values()

    Column {
        statuses.forEachIndexed { index, status ->
            val isCompleted = status.ordinal <= currentStatus.ordinal
            val isLast = index == statuses.size - 1

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (status) {
                            OrderStatus.PENDING -> Icons.Default.Receipt
                            OrderStatus.CONFIRMED -> Icons.Default.CheckCircle
                            OrderStatus.PREPARING -> Icons.Default.Inventory
                            OrderStatus.SHIPPING -> Icons.Default.LocalShipping
                            OrderStatus.DELIVERED -> Icons.Default.Done
                            OrderStatus.CANCELLED -> Icons.Default.Cancel
                        },
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Status Text
                Column {
                    Text(
                        status.name,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        getStatusDescription(status),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(start = 19.dp)
                        .width(2.dp)
                        .height(30.dp)
                        .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.Gray)
                )
            }
        }
    }
}

private fun getStatusDescription(status: OrderStatus): String {
    return when (status) {
        OrderStatus.PENDING -> "Order received, waiting for confirmation"
        OrderStatus.CONFIRMED -> "Order confirmed by seller"
        OrderStatus.PREPARING -> "Preparing your order"
        OrderStatus.SHIPPING -> "Your order is on the way"
        OrderStatus.DELIVERED -> "Order has been delivered"
        OrderStatus.CANCELLED -> "Order has been cancelled"
    }
}

internal fun formatDate(timestamp: Long): String {
    // Implement date formatting logic here
    return java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date(timestamp))
}
package com.example.shoesstoreapp.view.admin


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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shoesstoreapp.controller.OrderController
import com.example.shoesstoreapp.model.Order
import com.example.shoesstoreapp.model.OrderStatus
import com.example.shoesstoreapp.view.component.BottomNavigation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrderScreen(
    navController: NavController,
    orderController: OrderController = remember { OrderController() }
) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var showStatusDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        orderController.getAllOrders(
            onSuccess = { ordersList ->
                orders = ordersList
                isLoading = false
            },
            onError = {
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Orders") },
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
                currentRoute = "manage_orders"
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderCard(
                        order = order,
                        onStatusChange = {
                            selectedOrder = order
                            showStatusDialog = true
                        }
                    )
                }
            }
        }

        if (showStatusDialog && selectedOrder != null) {
            StatusUpdateDialog(
                currentStatus = selectedOrder!!.status,
                onStatusSelected = { newStatus ->
                    scope.launch {
                        orderController.updateOrderStatus(
                            selectedOrder!!.id,
                            newStatus,
                            onSuccess = {
                                // Refresh orders list
                                orderController.getAllOrders(
                                    onSuccess = { updatedOrders ->
                                        orders = updatedOrders
                                    },
                                    onError = {}
                                )
                            },
                            onError = {}
                        )
                    }
                    showStatusDialog = false
                },
                onDismiss = { showStatusDialog = false }
            )
        }
    }
}


@Composable
fun OrderCard(
    order: Order,
    onStatusChange: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                StatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Items: ${order.items.size}")
            Text("Total: $${order.totalAmount}")
            Text("Created: ${formatDate(order.createdAt)}")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStatusChange,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Update Status")
            }
        }
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.PENDING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        OrderStatus.CONFIRMED -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        OrderStatus.PREPARING -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
        OrderStatus.SHIPPING -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        OrderStatus.DELIVERED -> Color(0xFFE0F2F1) to Color(0xFF00695C)
        OrderStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusUpdateDialog(
    currentStatus: OrderStatus,
    onStatusSelected: (OrderStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Order Status") },
        text = {
            Column {
                OrderStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onStatusSelected(status) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = status == currentStatus,
                            onClick = { onStatusSelected(status) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(status.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    return java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(timestamp))
}
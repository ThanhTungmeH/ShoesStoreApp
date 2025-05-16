package com.example.shoesstoreapp.view.user

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shoesstoreapp.controller.OrderController
import com.example.shoesstoreapp.controller.UserController
import com.example.shoesstoreapp.model.Order
import com.example.shoesstoreapp.model.OrderStatus
import com.example.shoesstoreapp.model.User
import com.example.shoesstoreapp.view.admin.StatusChip
import com.example.shoesstoreapp.view.admin.getRealPathFromURI
import com.example.shoesstoreapp.view.admin.rememberContext
import com.example.shoesstoreapp.view.component.BottomNavigation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userController: UserController = UserController(),
    orderController: OrderController = OrderController(),
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var user by remember { mutableStateOf(User("", "", "", "", "", "")) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showOrdersDialog by remember { mutableStateOf(false) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var selectedStatus by remember { mutableStateOf<OrderStatus?>(null) }
    val focusManager = LocalFocusManager.current
    val context = rememberContext()
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImagePath = uri?.let { getRealPathFromURI(context, it) }
        }
    val clearFocusOnClick = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }

    LaunchedEffect(key1 = true) {
        isLoading = true
        try {
            val fetchedUser = userController.getCurrentUser()
            if (fetchedUser != null) {
                user = fetchedUser
            }

            // Fetch orders
            orderController.getAllOrders(
                onSuccess = { ordersList ->
                    orders = ordersList
                },
                onError = { error ->
                    errorMessage = error
                }
            )
        } catch (e: Exception) {
            errorMessage = "Failed to load profile: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                currentRoute = navController.currentDestination?.route
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp)
                .then(clearFocusOnClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                Button(onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val fetchedUser = userController.getCurrentUser()
                            if (fetchedUser != null) {
                                user = fetchedUser
                            }
                        } catch (e: Exception) {
                            errorMessage = "Failed to load profile: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Text("Retry")
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = selectedImagePath ?: user.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),  // This makes the image circular
                        contentScale = ContentScale.Crop  // This ensures the image fills the circle properly

                    )

                    Button(
                        onClick = {
                            pickImageLauncher.launch("image/*")
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )
                    ) {
                        Text("Change Photo")
                    }
                }
                // View Orders Button
                Button(
                    onClick = { navController.navigate("my_orders") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    )
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View My Orders")
                }

                OutlinedTextField(
                    value = user.name,
                    onValueChange = { user = user.copy(name = it) },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = user.email,
                    onValueChange = { user = user.copy(email = it) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = user.phone,
                    onValueChange = { user = user.copy(phone = it) },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = user.address,
                    onValueChange = { user = user.copy(address = it) },
                    label = { Text("Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        coroutineScope.launch {

                            val success = userController.updateUser(
                                userId = user.id,
                                name = user.name,
                                email = user.email,
                                phone = user.phone,
                                address = user.address,
                                imagePath = selectedImagePath ?: user.photoUrl,
                                role = user.role
                            )
                            if (!success) {
                                errorMessage = "Failed to update profile"
                            } else {
                                showSuccessDialog = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text("Save Changes")
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            userController.logout()
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        }

                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )

                ) {
                    Text("Logout")
                }

            }
        }

        if (showOrdersDialog) {
            OrdersDialog(
                orders = orders,
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it },
                onDismiss = { showOrdersDialog = false },
                onOrderClick = { orderId ->
                    showOrdersDialog = false
                    navController.navigate("order_tracking/$orderId")
                }
            )
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = { Text("Success") },
                text = { Text("Profile updated successfully!") },
                confirmButton = {
                    Button(onClick = { showSuccessDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }


    }
}


    @Composable
    fun OrdersDialog(
        orders: List<Order>,
        selectedStatus: OrderStatus?,
        onStatusSelected: (OrderStatus?) -> Unit,
        onDismiss: () -> Unit,
        onOrderClick: (String) -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("My Orders") },
            text = {
                Column {
                    // Status filter
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { onStatusSelected(null) },
                            label = { Text("All") }
                        )
                        OrderStatus.values().forEach { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = { onStatusSelected(status) },
                                label = { Text(status.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Orders list
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filteredOrders = if (selectedStatus != null) {
                            orders.filter { it.status == selectedStatus }
                        } else {
                            orders
                        }

                        if (filteredOrders.isEmpty()) {
                            Text("No orders found", modifier = Modifier.padding(16.dp))
                        } else {
                            filteredOrders.forEach { order ->
                                OrderItem(order = order, onClick = { onOrderClick(order.id) })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }

    @Composable
    fun OrderItem(
        order: Order,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    ItemRow(
                        imageUrl = item.productImage,
                        brand = item.productBrand,
                        name = item.productName,
                        size = item.size.toInt(),
                        quantity = item.quantity,
                        price = item.productPrice
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Text("Items: ${order.items.size}")
                Text("Total: $${order.totalAmount}")
                Text("Created: ${formatDate(order.createdAt)}")
            }
        }
    }

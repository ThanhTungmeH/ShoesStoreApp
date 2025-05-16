package com.example.shoesstoreapp.view.user

import android.R.attr.order
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shoesstoreapp.controller.CartController
import com.example.shoesstoreapp.controller.OrderController
import com.example.shoesstoreapp.controller.UserController
import com.example.shoesstoreapp.model.CartItem
import com.example.shoesstoreapp.service.PaymentResult
import com.example.shoesstoreapp.service.VNPayHelper
import kotlinx.coroutines.launch

@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    cartController: CartController = remember { CartController() },
    navController: NavController
) {
    val cartItems = cartController.cartItems.collectAsState().value
    val cartTotal = remember { mutableStateOf(0.0) }
    var userAddress by remember { mutableStateOf("") }
    val userController = remember { UserController() }
    val selectedPaymentMethod = remember { mutableStateOf("Credit Card") }
    LaunchedEffect(cartItems) {
        cartTotal.value = cartController.getCartTotal()
    }
    LaunchedEffect(Unit) {
        cartController.listenToUserCart()

        // Load user address
        val user = userController.getCurrentUser()
        user?.address?.let {
            userAddress = it
        }
    }
    LaunchedEffect(Unit) {
        cartController.listenToUserCart()
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        StatusBar1()
        NavigationHeader(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth().fillMaxHeight()
        ) {
            ShippingSection(
                userAddress = userAddress,
                onAddressChange = { userAddress = it }

            )
            DeliverySection()
            PaymentSection(selectedOption = selectedPaymentMethod.value) {
                selectedPaymentMethod.value = it
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start=15.dp,end=15.dp ,bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ITEMS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "INFORMATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "PRICE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            LazyColumn {
                items(cartItems) { item ->
                    ItemsSection(cartItem = item)
                }

            }
                OrderSummarySection(
                    cartTotal = cartTotal.value
                )
        }

        PlaceOrderButton(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            cartController = cartController,
            orderController = OrderController(),
            userAddress = userAddress,
            selectedPaymentMethod = selectedPaymentMethod.value,
            onOrderPlaced = { orderId ->
                // Navigate using the real NavController
                navController.navigate("order_tracking/$orderId")
            }
        )
    }
}


@Composable
private fun StatusBar1() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color.White)
    ) {
        // Status bar content implementation
    }
}

@Composable
private fun NavigationHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text(
            text = "Checkout",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.34).sp
        )

        // Empty box for alignment
        Box(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun ShippingSection(
    userAddress: String,
    onAddressChange: (String) -> Unit
) {
    CheckoutSection(
        title = "SHIPPING",
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
             OutlinedTextField(
                    value = userAddress,
                    onValueChange = onAddressChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(
                            width = 0.5.dp,
                            color = Color(0xFFE6E6E6),

                        )

             )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.5f)
                )
            }
        }
    )
}

@Composable
private fun DeliverySection() {
    CheckoutSection(
        title = "DELIVERY",
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Free",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Standard | 3-4 days",
                        fontSize = 14.sp
                    )
                }

            }
        }
    )
}

@Composable
private fun PaymentSection(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val options = listOf("Credit Card", "VNPay")

    CheckoutSection(
        title = "PAYMENT",
        content = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onOptionSelected(option) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { onOptionSelected(option) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option, fontSize = 14.sp)
                        }

                        if (option == "VNPay") {
                            Text("VNPay", color = Color(0xFF0066FF))
                        } else if (option == "Credit Card") {
                            Text("Visa *1234", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ItemsSection(
    cartItem: CartItem,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {


        ItemRow(
            imageUrl = cartItem.productImage,
            brand = cartItem.productBrand,
            name = cartItem.productName,
            size = cartItem.size.toInt() ,
            quantity = cartItem.quantity,
            price =cartItem.productPrice
        )

        Spacer(modifier = Modifier.height(16.dp))


    }
}

@Composable
internal fun ItemRow(
    imageUrl: String,
    brand: String,
    name: String,
    size: Int,
    quantity: Int,
    price: Double,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text ="Brand: "+ brand,
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text ="Name: "+ name,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text ="Size: "+ size.toString(),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quantity: ${quantity.toString().padStart(2, '0')}",
                fontSize = 14.sp
            )
        }

        Text(
            text =  "$${String.format("%.2f", price * quantity)}",
            fontSize = 14.sp
        )
    }
}

@Composable
private fun OrderSummarySection(
    cartTotal: Double ,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryRow(title = "Subtotal ", value = cartTotal.toString())
        SummaryRow(title = "Shipping total", value = "Free")
        SummaryRow(
            title = "Total",
            value ="$"+ cartTotal.toString(),
            isBold = true
        )
    }
}

@Composable
private fun SummaryRow(
    title: String,
    value: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Medium else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun PlaceOrderButton(
    modifier: Modifier = Modifier,
    cartController: CartController = remember { CartController() },
    orderController: OrderController = remember { OrderController() },
    onOrderPlaced: (String) -> Unit,
    userAddress: String,
    selectedPaymentMethod: String
) {
    val context = LocalContext.current
    val cartItems = cartController.cartItems.collectAsState().value
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val total = cartController.getCartTotal()

    // Handle VNPay payment result
    val paymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            when (val paymentResult = VNPayHelper.handlePaymentResult(uri)) {
                is PaymentResult.Success -> {
                    scope.launch {
                        try {
                            orderController.updateOrderPaymentStatus(
                                orderId = uri.getQueryParameter("orderId") ?: "",
                                status = "PAID",
                                transactionId = paymentResult.transactionId
                            )
                            cartController.clearCart()
                            onOrderPlaced(uri.getQueryParameter("orderId") ?: "")
                            Toast.makeText(context, "Payment successful!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error updating payment status", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is PaymentResult.Failed -> {
                    Toast.makeText(context, paymentResult.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Button(
        onClick = {
            scope.launch {
                isLoading = true
                try {
                    orderController.createOrder(
                        items = cartItems,
                        totalAmount = total,
                        shippingAddress = userAddress,
                        onSuccess = { orderId ->
                            if (selectedPaymentMethod == "VNPay") {
                                val paymentUrl = VNPayHelper.createPaymentUrl(
                                    context = context,
                                    amount = total,
                                    orderInfo = "Payment for order $orderId"
                                )

                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(paymentUrl)
                                }
                                paymentLauncher.launch(intent)
                            } else {
                                cartController.clearCart()
                                onOrderPlaced(orderId)
                                Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                } finally {
                    isLoading = false
                }
            }
        },
        modifier = modifier,
        enabled = !isLoading && cartItems.isNotEmpty(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
        } else {
            Text(
                text = "Place order",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 14.dp)
            )
        }
    }
}
@Composable
private fun CheckoutSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = Color(0xFFE6E6E6)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(100.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}


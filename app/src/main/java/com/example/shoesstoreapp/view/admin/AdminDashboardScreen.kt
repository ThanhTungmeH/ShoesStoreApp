package com.example.shoesstoreapp.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shoesstoreapp.controller.AdminController
import com.example.shoesstoreapp.view.component.BottomNavigation


@Composable
fun AdminDashboardScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val adminController = AdminController()
    val totalUsers = remember{mutableStateOf(0L)}
    val totalProducts = remember{mutableStateOf(0L)}
    val totalOrders = remember{mutableStateOf(0L)}
    val totalRevenue = remember{mutableStateOf(0.0)}
    LaunchedEffect(Unit){
        totalRevenue.value= adminController.getTotalRevenue()
        totalUsers.value= adminController.getTotalUsers()
        totalProducts.value= adminController.getTotalProducts()
        totalOrders.value= adminController.getTotalOrders()

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
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Admin Dashboard",
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                )

                // Revenue and Orders Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                    StatCard(
                        title = "Total Revenue",
                        value = "$${totalRevenue.value.toDouble()}",
                        icon = Icons.Outlined.AttachMoney, // Changed from AttachMoney
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF4CAF50)
                    )
                    StatCard(
                        title = "Đơn Hàng",
                        value ="${totalOrders.value}",
                        icon = Icons.Outlined.ShoppingCart,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF2196F3)
                    )
                }

                // Products and Users Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        title = "Tổng Sản Phẩm",
                        value = "${totalProducts.value}",
                        icon = Icons.Outlined.Inventory2, // Changed from Inventory2
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFFA000)
                    )
                    StatCard(
                        title = "Khách Hàng",
                        value = "${totalUsers.value}",
                        icon = Icons.Filled.Person, // Changed from Outlined.People
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF9C27B0)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Performance Metrics
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Thống Kê Hiệu Suất",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PerformanceMetric(title = "Sản phẩm bán chạy", value = "Giày Nike Air")
                            PerformanceMetric(title = "Doanh thu hôm nay", value = "₫2,500,000")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PerformanceMetric(title = "Đơn hàng hôm nay", value = "12")
                            PerformanceMetric(title = "Tỷ lệ hoàn thành", value = "95%")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Quản Lý",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Button(
                        onClick = { navController.navigate("manage_product") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory, // Changed from Inventory2
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Quản Lý Sản Phẩm")
                        }
                    }

                    Button(
                        onClick = { navController.navigate("manage_orders") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Quản Lý Đơn Hàng")
                        }
                    }

                    Button(
                        onClick = { /* Navigate to Users */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Person, // Changed from Outlined.People
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Quản Lý Người Dùng")
                        }
                    }

                    Button(
                        onClick = { /* Generate Reports */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Assessment, // Changed from BarChart
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Báo Cáo & Thống Kê")
                        }
                    }
                }
            }
        }

    }

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row (modifier=Modifier,
                verticalAlignment = Alignment.CenterVertically,){
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Row {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

            }
        }
    }
}

@Composable
fun PerformanceMetric(title: String, value: String) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

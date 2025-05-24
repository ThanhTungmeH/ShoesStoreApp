package com.example.shoesstoreapp.view.admin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shoesstoreapp.controller.AdminController
import com.example.shoesstoreapp.model.OrderStats
import com.example.shoesstoreapp.model.ProductStats
import com.example.shoesstoreapp.model.SalesDataPoint


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsAndStatisticsScreen(
    navController: NavController,
    adminController: AdminController = remember { AdminController() }
) {

    var selectedPeriod by remember { mutableStateOf("Monthly") }
    var selectedReportType by remember { mutableStateOf("Sales") }
    var isLoading by remember { mutableStateOf(true) }

    // Statistics data
    var salesData by remember { mutableStateOf<List<SalesDataPoint>>(emptyList()) }
    var topProducts by remember { mutableStateOf<List<ProductStats>>(emptyList()) }
    var orderStats by remember { mutableStateOf<OrderStats?>(null) }

    // Fetch data when period or report type changes
    LaunchedEffect(selectedPeriod, selectedReportType) {
        isLoading = true

        try {
            when (selectedReportType) {
                "Sales" -> {
                    salesData = adminController.getSalesData(selectedPeriod.lowercase())
                }
            }
        } catch (e: Exception) {
            Log.e("ReportsScreen", "Error loading data: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Statistic") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Period selector
            Text(
                "Chọn khoảng thời gian:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodChip("Daily", selectedPeriod) { selectedPeriod = "Daily" }
                PeriodChip("Weekly", selectedPeriod) { selectedPeriod = "Weekly" }
                PeriodChip("Monthly", selectedPeriod) { selectedPeriod = "Monthly" }
                PeriodChip("Yearly", selectedPeriod) { selectedPeriod = "Yearly" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Report type selector
            Text(
                "Loại báo cáo:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportTypeButton("Sales", selectedReportType, Icons.Outlined.AttachMoney) {
                    selectedReportType = "Sales"
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content area
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedReportType) {
                    "Sales" -> SalesReport(salesData, selectedPeriod)
                }
            }
        }
    }
}


@Composable
fun PeriodChip(title: String, selectedPeriod: String, onClick: () -> Unit) {
    FilterChip(
        selected = title == selectedPeriod,
        onClick = onClick,
        label = { Text(title) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun ReportTypeButton(
    title: String,
    selectedType: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val isSelected = title == selectedType
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text(title)
    }
}

@Composable
fun SalesReport(salesData: List<SalesDataPoint>, period: String) {
    Column {
        Text(
            "Báo Cáo Doanh Thu ($period)",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sales summary cards
        val totalSales = salesData.sumOf { it.amount }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatisticCard(
                title = "total revenue",
                value = "$${totalSales}",
                backgroundColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sales chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text("Biểu đồ doanh thu", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                if (salesData.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không có dữ liệu")
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Simple bar chart visualization
                        Row(
                            modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()),

                            verticalAlignment = Alignment.Bottom
                        ) {
                            val maxAmount = salesData.maxOfOrNull { it.amount } ?: 1.0

                            salesData.take(15).forEach { dataPoint ->
                                val barHeight = (dataPoint.amount / maxAmount) * 200
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(8.dp)
                                            .height(barHeight.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(
                                                    topStart = 4.dp,
                                                    topEnd = 4.dp
                                                )
                                            )
                                    )
                                    Text(
                                        dataPoint.label,
                                        style = TextStyle(fontSize = 10.sp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sales data table
    }
}


@Composable
fun StatisticCard(
    title: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}





// Extension function to format numbers as currency

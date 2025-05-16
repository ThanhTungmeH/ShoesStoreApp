package com.example.shoesstoreapp.view.component


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shoesstoreapp.R
import com.example.shoesstoreapp.controller.AuthController

@Composable
fun BottomNavigation(
    navController: NavController,
    currentRoute: String? = null,
    modifier: Modifier = Modifier,
    authController: AuthController = AuthController()
) {
    var userRole by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        userRole = authController.getCurrentUserRole()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = R.drawable.ic_home,
                selected = currentRoute == "home",
                onClick = { navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }}
            )
            NavItem(
                icon = R.drawable.ic_discover,
                selected = currentRoute == "discover",
                onClick = { navController.navigate("discover") {
                    launchSingleTop = true
                }}
            )
            NavItem(
                icon = R.drawable.ic_cart,
                selected = currentRoute == "cart",
                onClick = { navController.navigate("cart") {
                    launchSingleTop = true
                }}
            )
            if(userRole == "admin") {
                NavItem(
                    icon = R.drawable.management,
                    selected = currentRoute == "admin_home",
                    onClick = { navController.navigate("admin_home") {
                        launchSingleTop = true
                    }}
                )
            }

            NavItem(
                icon = R.drawable.ic_profile,
                selected = currentRoute == "profile",
                onClick = { navController.navigate("profile") {
                    launchSingleTop = true
                }}
            )
        }


    }
}

@Composable
fun NavItem(
    icon: Int,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(76.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (selected) Color(0xFF3669C9) else Color.Black
            )
        }
    }
}

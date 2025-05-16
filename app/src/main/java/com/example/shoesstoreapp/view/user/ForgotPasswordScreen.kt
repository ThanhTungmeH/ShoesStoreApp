package com.example.shoesstoreapp.view.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shoesstoreapp.R

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Create a modifier that clears focus when clicked
    val clearFocusOnClick = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0E6FF),
                        Color(0xFFEAF6FF),
                        Color(0xFFE5FFED)
                    ),
                )
            )
            .then(clearFocusOnClick),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rung), // Replace with your image name
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(200.dp) // Adjust the size as needed
            )


            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(10.dp),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),

                ){
            Column(modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
     horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Text("Forgot password", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField( modifier= Modifier.fillMaxWidth(),
            value = email, onValueChange = { email = it }, label = { Text("Email") })

        Button(
            onClick = { /* Xử lý gửi email reset mật khẩu */ },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Send request reset password")
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back sign in")
        }
    }
}
}
}
}
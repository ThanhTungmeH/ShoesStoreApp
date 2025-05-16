package com.example.shoesstoreapp.view.admin

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Deblur
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

import com.example.shoesstoreapp.controller.ProductController
import com.example.shoesstoreapp.model.Product

import kotlinx.coroutines.launch
import kotlin.toString


@Composable
fun ManageProductScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        ProductListScreen(navController)
    }
}
@Composable
internal fun rememberContext(): Context {
    return androidx.compose.ui.platform.LocalContext.current
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    navController: NavController,
    productController: ProductController = remember { ProductController() }
) {
    var showDialog by remember { mutableStateOf(false) }
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var productBrand by remember { mutableStateOf("") }
    var productSizes by remember { mutableStateOf("") }
    var productQuantity by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    val context = rememberContext()
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImagePath = uri?.let { getRealPathFromURI(context, it) }
    }
    // Lấy danh sách sản phẩm qua controller
    LaunchedEffect(Unit) {
        productController.listenToProducts(
            onSuccess = { /* Products updated */ },
            onError = { errorMessage = it.message }
        )
    }

    Column {
        TopAppBar(
            title = { Text("Back") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(productController.products) { product ->
                ProductItem(product, onClick = {
                    navController.navigate("productDetail/${product.id}")
                })
            }
        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 16.dp, bottom = 25.dp)
        ) {
            Text("Add product")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add product") },
            text = {
                Column {
                    Text("Name product")
                    TextField(value = productName, onValueChange = { productName = it })
                    Text("Price")
                    TextField(value = productPrice, onValueChange = { productPrice = it })
                    Text("Description")
                    TextField(value = productDescription, onValueChange = { productDescription = it })
                    Spacer( Modifier.height(8.dp))
                    val brandList = listOf("Nike", "Adidas", "Puma", "Vans")
                    BrandDropdown(
                        brandList = brandList,
                        selectedBrand = productBrand,
                        onBrandSelected = { productBrand = it }
                    )
                    Text("Size")
                    TextField(value = productSizes, onValueChange = { productSizes = it })
                    Text("Quantity")
                    TextField(value = productQuantity, onValueChange = { productQuantity = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { pickImageLauncher.launch("image/*") }) {
                        Text("Select image")
                    }

                    // Hiển thị ảnh đã chọn
                    selectedImagePath?.let {
                        Text("Selected image:", fontWeight = FontWeight.Bold)
                        AsyncImage(
                            model = it,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isLoading = true
                        try {
                            val price = productPrice.toDoubleOrNull() ?: 0.0
                            val sizesList = productSizes.split(",").map { it.trim() }
                            val Quantity = productQuantity.toIntOrNull() ?: 0
                            coroutineScope.launch {
                                val success = productController.addProduct(
                                    name = productName,
                                    price = price,
                                    description = productDescription,
                                    brand = productBrand,
                                    sizes = sizesList,
                                    quantity = Quantity,
                                    imagePath = selectedImagePath ?: ""

                                )

                                if (success) {
                                    showSuccessMessage = true
                                    productName = ""
                                    productPrice = ""
                                    productDescription = ""
                                    productBrand = ""
                                    productSizes = ""
                                    productQuantity = ""
                                    selectedImagePath = null
                                    showDialog = false

                                } else {
                                    errorMessage = "Cannot add product"
                                }
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = e.message
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Text("Loading...")
                    } else {
                        Text("Add")
                    }
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessMessage) {
        AlertDialog(
            onDismissRequest = { showSuccessMessage = false },
            title = { Text("Success") },
            text = { Text("Product added successfully!") },
            confirmButton = {
                Button(onClick = { showSuccessMessage = false }) {
                    Text("OK")
                }
            }
        )
    }

    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}
fun getRealPathFromURI(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        it.moveToFirst()
        return it.getString(columnIndex)
    }
    return uri.path
}

@Composable
fun ProductItem(product: Product, onClick: () -> Unit,productController: ProductController = remember { ProductController() }) {
    val coroutineScope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    // Variables for editing product
    var editName by remember { mutableStateOf(product.name) }
    var editPrice by remember { mutableStateOf(product.price.toString()) }
    var editDescription by remember { mutableStateOf(product.description) }
    var editBrand by remember { mutableStateOf(product.brand) }
    var editSizes by remember { mutableStateOf(product.size.joinToString(",")) }
    var editQuantity by remember { mutableStateOf(product.quantity.toString()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    val context = rememberContext()
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImagePath = uri?.let { getRealPathFromURI(context, it) }
    }
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable {   // Initialize edit fields with current values and show dialog
                editName = product.name
                editPrice = product.price.toString()
                editDescription = product.description
                editBrand = product.brand
                editSizes = product.size.joinToString(",")
                editQuantity = product.quantity.toString()
                showEditDialog = true }
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.fillMaxHeight()) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.name,
                    modifier = Modifier.size(130.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Row(modifier=Modifier.fillMaxWidth()) {
                    Text(text = product.description, fontSize = 17.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text =  product.quantity.toString(),
                        fontSize = 17.sp,
                        color = Color.DarkGray,

                    )
                }
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color.Yellow)
                    Text(text = product.rating.toString(), fontSize = 17.sp)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .size(40.dp)
                            .align(Alignment.CenterVertically),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "$${product.price}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Blue
                            )
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        productController.deleteProduct(product.id)
                                    }
                                },
                                modifier = Modifier
                                    .padding(end = 15.dp)
                                    .align(Alignment.CenterEnd),
                                colors = buttonColors(containerColor = Color.LightGray)
                            ) {
                                Icon(
                                   imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit product") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Name product")
                        TextField(value = editName, onValueChange = { editName = it })

                        Text("Price")
                        TextField(value = editPrice, onValueChange = { editPrice = it })

                        Text("Description")
                        TextField(value = editDescription, onValueChange = { editDescription = it })

                        Spacer( Modifier.height(8.dp))
                        val brandList = listOf("Nike", "Adidas", "Puma", "Vans")

                        BrandDropdown(
                            brandList = brandList,
                            selectedBrand = editBrand,
                            onBrandSelected = { editBrand = it }
                        )

                        Text("Size")
                        TextField(value = editSizes, onValueChange = { editSizes = it })

                        Text("Quantity")
                        TextField(value = editQuantity, onValueChange = { editQuantity = it })
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { pickImageLauncher.launch("image/*") }) {
                            Text("Select new image")
                        }
                        // Show current image
                        Text("Image current:", fontWeight = FontWeight.Bold)
                        AsyncImage(
                            model = product.image,
                            contentDescription = "Current Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        selectedImagePath?.let {
                            Text("New Selected Image:", fontWeight = FontWeight.Bold)
                            AsyncImage(
                                model = it,
                                contentDescription = "New Selected Image",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isLoading = true
                            try {
                                val price = editPrice.toDoubleOrNull() ?: 0.0
                                val sizesList = editSizes.split(",").map { it.trim() }
                                val quantity = editQuantity.toIntOrNull() ?: 0

                                coroutineScope.launch {
                                    val success = productController.updateProduct(
                                        productId = product.id,
                                        name = editName,
                                        price = price,
                                        description = editDescription,
                                        brand = editBrand,
                                        sizes = sizesList,
                                        quantity = quantity,
                                        imagePath = selectedImagePath ?: product.image
                                    )

                                    if (success) {
                                        showEditDialog = false
                                    } else {
                                        errorMessage = "Cannot update product"
                                    }
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = e.message
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            Text("Updating...")
                        } else {
                            Text("Save")
                        }
                    }
                },
                dismissButton = {
                    Button(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandDropdown(
    brandList: List<String>,
    selectedBrand: String,
    onBrandSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedBrand,
            onValueChange = {},
            readOnly = true,
            label = { Text("Brand") },
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            brandList.forEach { brand ->
                DropdownMenuItem(
                    text = { Text(brand) },
                    onClick = {
                        onBrandSelected(brand)
                        expanded = false
                    }
                )
            }
        }
    }
}

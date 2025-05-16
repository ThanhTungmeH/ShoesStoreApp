package com.example.shoesstoreapp.view.user


import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

import com.example.shoesstoreapp.R
import com.example.shoesstoreapp.controller.ProductController
import com.example.shoesstoreapp.model.Product
import com.example.shoesstoreapp.model.ProductItem
import com.example.shoesstoreapp.view.component.BottomNavigation
import com.example.shoesstoreapp.view.component.SearchBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.toString


@Composable
fun HomeScreen(
    navController: NavController,
    productController: ProductController = remember { ProductController() }
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        productController.listenToProducts(
            onSuccess = { /* Products updated */ },
            onError = { errorMessage = it.message }
        )
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                productController.listenToProducts(
                    onSuccess = { isRefreshing = false },
                    onError = {
                        errorMessage = it.message
                        isRefreshing = false
                    }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SearchBar()

                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryChips()

                    Spacer(modifier = Modifier.height(16.dp))
                    BannerSection()

                    Spacer(modifier = Modifier.height(16.dp))
                    ProductSection(
                        title = "Categories",
                        items = listOf(
                            ProductItem("https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg", "Nike"),
                            ProductItem("https://images.pexels.com/photos/1598505/pexels-photo-1598505.jpeg", "Adidas"),
                            ProductItem("https://images.pexels.com/photos/1598508/pexels-photo-1598508.jpeg", "Puma"),
                            ProductItem("https://images.pexels.com/photos/1598507/pexels-photo-1598507.jpeg", "Jordan"),
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(
                        visible = productController.products.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ProductGridSection(
                            title = "Featured Products",
                            products = productController.products,
                            navController = navController
                        )
                    }

                    if (productController.products.isEmpty() && !isRefreshing) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (errorMessage != null) {
                                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(90.dp))
                }
            }

            BottomNavigation(
                navController = navController,
                currentRoute = navController.currentDestination?.route,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ProductSection(
    title: String,
    items: List<ProductItem>
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            // Arrow icon here
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(items.size) { index ->
                CircularProductCard(
                    imageUrl = items[index].imageUrl,
                    title = items[index].title
                )
            }
        }
    }
}

@Composable
 fun ProductGridSection(
    title: String,
    products: List<Product>,
    navController: NavController
) {

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            // Arrow icon here
        }
        val rowCount = (products.size + 5) / 6

        // Create rows
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            for (rowIndex in 0 until rowCount) {
                val startIndex = rowIndex * 6
                val endIndex = minOf(startIndex + 6, products.size)
                val rowProducts = products.subList(startIndex, endIndex)

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rowProducts.size) { index ->
                        ProductCard(product = rowProducts[index],
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChips() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(20.dp),

        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CategoryChip(
                icon = R.drawable.ic_favorite,
                text = "Favorites"
            )
        }
        item {
            CategoryChip(
                icon = R.drawable.ic_history,
                text = "History"
            )
        }

        item {
            CategoryChip(
                icon = R.drawable.ic_orders,
                text = "Orders"
            )
        }
    }
}

@Composable
 fun CategoryChip(
    icon: Int,
    text: String
) {
    Row(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color(0xFFE5E5E5),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BannerSection(
    images: List<Int> = listOf(
        R.drawable.l1,
        R.drawable.l2,
        R.drawable.l3
    )
) {
    val pagerState = rememberPagerState(pageCount = { images.size } )
    val scope = rememberCoroutineScope()

    // Tự động chuyển ảnh mỗi 3 giây
    LaunchedEffect(pagerState.currentPage) {
        delay(3000)
        val nextPage = (pagerState.currentPage + 1) % images.size
        scope.launch {
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
    ) {
        HorizontalPager(

            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(id = images[page]),
                contentDescription = "Banner Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dots Indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(images.size) { index ->
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(
                            color = if (index == pagerState.currentPage) {
                                Color.Black.copy(alpha = 0.8f)
                            } else {
                                Color.Black.copy(alpha = 0.2f)
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
@Composable
fun CircularProductCard(
    imageUrl: String,
    title: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Text(
            text = title,
            fontSize = 14.sp
        )
    }
}
@Composable
fun ProductCard(
    product: Product,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .animateContentSize()
            .clickable {
                navController.navigate("product_detail/${product.id}")
            },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(148.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(product.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    },
                    error = {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Default.BrokenImage,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                )

                // Rating badge
                Surface(
                    modifier = Modifier
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = product.rating.toString(),
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.brand,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$${product.price}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.scrim
                )
            }
        }
    }
}


















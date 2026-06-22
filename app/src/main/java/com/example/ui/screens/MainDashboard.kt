package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.data.model.*
import com.example.ui.theme.LocalOrange
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: MainViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val allStores by viewModel.allStores.collectAsStateWithLifecycle()
    val approvedStores by viewModel.approvedStores.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val trackingOrder by viewModel.trackingOrder.collectAsStateWithLifecycle()

    var showCartDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = "quickz logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "quickz",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "everything nearby delivered quickly",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    // Quick stats/Role indicators or Help
                    FilledTonalButton(
                        onClick = { viewModel.changeRole(if (currentRole == "CUSTOMER") "VENDOR" else if (currentRole == "VENDOR") "RIDER" else if (currentRole == "RIDER") "ADMIN" else "CUSTOMER") },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("role_switcher")
                    ) {
                        Icon(
                            imageVector = when (currentRole) {
                                "CUSTOMER" -> Icons.Default.ShoppingBag
                                "VENDOR" -> Icons.Default.Storefront
                                "RIDER" -> Icons.Default.TwoWheeler
                                else -> Icons.Default.AdminPanelSettings
                            },
                            contentDescription = "role",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Flipped to $currentRole", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // Elegant Multi-Role Navigation bar supporting seamless transitions
            NavigationBar(
                modifier = Modifier.shadow(8.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentRole == "CUSTOMER",
                    onClick = { viewModel.changeRole("CUSTOMER") },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Customer") },
                    label = { Text("Customer") },
                    modifier = Modifier.testTag("nav_customer")
                )
                NavigationBarItem(
                    selected = currentRole == "VENDOR",
                    onClick = { viewModel.changeRole("VENDOR") },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Vendor") },
                    label = { Text("Vendor Portal") },
                    modifier = Modifier.testTag("nav_vendor")
                )
                NavigationBarItem(
                    selected = currentRole == "RIDER",
                    onClick = { viewModel.changeRole("RIDER") },
                    icon = { Icon(Icons.Default.TwoWheeler, contentDescription = "Rider") },
                    label = { Text("Rider App") },
                    modifier = Modifier.testTag("nav_rider")
                )
                NavigationBarItem(
                    selected = currentRole == "ADMIN",
                    onClick = { viewModel.changeRole("ADMIN") },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                    label = { Text("Admin Panel") },
                    modifier = Modifier.testTag("nav_admin")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Check for missing API Key to show friendly helpful warning
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "info",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Search running in legacy pattern. Add your GEMINI_API_KEY in the AI Studio Secrets panel to activate neural matches!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = currentRole,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "role_container"
            ) { role ->
                when (role) {
                    "CUSTOMER" -> CustomerView(viewModel, onOpenCart = { showCartDialog = true })
                    "VENDOR" -> VendorView(viewModel)
                    "RIDER" -> RiderView(viewModel)
                    "ADMIN" -> AdminView(viewModel)
                }
            }
        }

        if (showCartDialog) {
            CartCheckoutDialog(viewModel = viewModel, onDismiss = { showCartDialog = false })
        }
    }
}

// ==========================================
// CUSTOMER VIEW & COMPONENTS
// ==========================================
@Composable
fun CustomerView(viewModel: MainViewModel, onOpenCart: () -> Unit) {
    var customerTab by remember { mutableStateOf("MARKET") } // "MARKET", "AI_SEARCH", "TRACKING"
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Headers
        TabRow(
            selectedTabIndex = when (customerTab) {
                "MARKET" -> 0
                "AI_SEARCH" -> 1
                else -> 2
            }
        ) {
            Tab(
                selected = customerTab == "MARKET",
                onClick = { customerTab = "MARKET" },
                text = { Text("Marketplace") },
                icon = { Icon(Icons.Default.Storefront, null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = customerTab == "AI_SEARCH",
                onClick = { customerTab = "AI_SEARCH" },
                text = { Text("AI Search") },
                icon = { Icon(Icons.Default.Psychology, null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = customerTab == "TRACKING",
                onClick = { customerTab = "TRACKING" },
                text = { Text("Orders & Track") },
                icon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp)) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (customerTab) {
                "MARKET" -> CustomerMarketView(viewModel)
                "AI_SEARCH" -> CustomerAISearchView(viewModel)
                "TRACKING" -> CustomerTrackingView(viewModel)
            }

            // Sticky Bottom Cart Bar
            if (cartItems.isNotEmpty() && customerTab != "TRACKING") {
                val totalQty = cartItems.sumOf { it.quantity }
                val totalAmount = cartItems.sumOf { it.price * it.quantity }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onOpenCart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(8.dp, RoundedCornerShape(27.dp))
                            .testTag("checkout_bar"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(27.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$totalQty item${if (totalQty > 1) "s" else ""} added",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                               )
                                Text(
                                    text = "₹${"%.2f".format(totalAmount)} + taxes",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "View Cart", fontWeight = FontWeight.ExtraBold, color = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = "Go", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerMarketView(viewModel: MainViewModel) {
    val approvedStores by viewModel.approvedStores.collectAsStateWithLifecycle()
    val selectedStoreId by viewModel.selectedStoreId.collectAsStateWithLifecycle()
    val products by viewModel.selectedStoreProducts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val categories = listOf(
        "All", "Restaurants", "Grocery", "Fruits & Vegetables", "Meat & Fish", "Dairy", "Bakery", "Medicines"
    )

    if (selectedStoreId != null) {
        val activeStore = approvedStores.find { it.id == selectedStoreId }
        Column(modifier = Modifier.fillMaxSize()) {
            // Header for active Store
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectStore(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeStore?.name ?: "Store Products",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${activeStore?.type} • 📍 ${activeStore?.address}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Rating Badge
                    BadgeBox(rating = activeStore?.rating ?: 4.5f)
                }
            }

            // Products Grid
            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStatePlaceholder(
                        icon = Icons.Default.Storage,
                        title = "No Products Found",
                        subtitle = "This vendor hasn't uploaded product stocks yet."
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products) { product ->
                        ProductCatalogCard(product = product, onAdd = { viewModel.addToCart(product) })
                    }
                }
            }
        }
    } else {
        // Category selections & Store lists
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // ==========================================
            // Bento Grid Header & Layout (All Categories Home)
            // ==========================================
            if (selectedCategory == "All") {
                // Bento 1: Header / Location with initial badge
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEADDFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color(0xFF21005D),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Home",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = "Expand",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Greenwood Apartments, Block C, MG Road",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        // Profile JD Badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6750A4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Bento 2: "Instant Grocery" Bento Card (Colspan equivalent)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("bento_grocery_card")
                            .clickable { viewModel.selectCategory("Grocery") },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E4FF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(20.dp)
                        ) {
                            // Soft decorative abstract background shape
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 24.dp, y = 24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0061A4).copy(alpha = 0.08f))
                            )

                            // Content Row
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Instant\nGrocery",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        lineHeight = 26.sp,
                                        color = Color(0xFF001D36)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.4f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "⚡ 12 MINS DELIVERY",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF001D36)
                                        )
                                    }
                                }

                                // Interactive mini grocery cards
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🥛", fontSize = 28.sp)
                                        }
                                    }
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🥦", fontSize = 28.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bento 3: Split Row (Food Delivery && Pharmacy)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Food Delivery Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("bento_food_card")
                                .clickable { viewModel.selectCategory("Restaurants") },
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = "Restaurant",
                                    tint = Color(0xFF21005D),
                                    modifier = Modifier.size(36.dp)
                                )
                                Column {
                                    Text(
                                        text = "Food Delivery",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF21005D),
                                        lineHeight = 20.sp
                                    )
                                    Text(
                                        text = "Top Restaurants",
                                        fontSize = 11.sp,
                                        color = Color(0xFF49454F)
                                    )
                                }
                            }
                        }

                        // Pharmacy Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("bento_pharmacy_card")
                                .clickable { viewModel.selectCategory("Medicines") },
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE0D4)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = "Medicines",
                                    tint = Color(0xFF410002),
                                    modifier = Modifier.size(36.dp)
                                )
                                Column {
                                    Text(
                                        text = "Pharmacy",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF410002),
                                        lineHeight = 20.sp
                                    )
                                    Text(
                                        text = "Verified Stores",
                                        fontSize = 11.sp,
                                        color = Color(0xFF49454F)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bento 4: Scrolling category alternatives
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clickable { viewModel.selectCategory("Meat & Fish") },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0F5)),
                            border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🥩", fontSize = 20.sp)
                                    }
                                }
                                Text(
                                    text = "Fresh Meat",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clickable { viewModel.selectCategory("Bakery") },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0F5)),
                            border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🧁", fontSize = 20.sp)
                                    }
                                }
                                Text(
                                    text = "Bakery",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                // Bento 5: Elite Offer Banner (Free Delivery Promo)
                item {
                    var showEliteDialog by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { showEliteDialog = true },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = "loyalty",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Elite Membership".uppercase(Locale.getDefault()),
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Free Delivery on all orders",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Detail",
                                tint = Color.White
                            )
                        }
                    }

                    if (showEliteDialog) {
                        Dialog(onDismissRequest = { showEliteDialog = false }) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEADDFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stars,
                                            contentDescription = "Elite",
                                            tint = Color(0xFF6750A4),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "quickz Elite Club 👑",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Congratulations! You are eligible for our complimentary Elite Membership.",
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E4FF))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(text = "• ₹0 Delivery Fee on items near you", fontSize = 12.sp, color = Color(0xFF001D36), fontWeight = FontWeight.Bold)
                                            Text(text = "• Extra direct discounts on verified grocery items", fontSize = 12.sp, color = Color(0xFF001D36))
                                            Text(text = "• Top-priority lightning fast 10-min fulfillment", fontSize = 12.sp, color = Color(0xFF001D36))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { showEliteDialog = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Claim Benefits")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // If filtered to a specific category, show a simple title back-button card
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.selectCategory("All") }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Clear Filter")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Category: $selectedCategory",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Showing verified local stores offering $selectedCategory",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Categories horizontal scroll list
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat) },
                            leadingIcon = if (selectedCategory == cat) {
                                { Icon(Icons.Default.Check, null) }
                            } else null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Nearby Active Stores", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Nearby Stores List filtered by category query
            val filteredStores = approvedStores.filter { store ->
                if (selectedCategory == "All") true
                else {
                    // map categories to store types
                    val mappedType = when (selectedCategory) {
                        "Restaurants" -> "Restaurant"
                        "Grocery" -> "Grocery"
                        "Fruits & Vegetables" -> "Grocery"
                        "Dairy" -> "Dairy"
                        "Bakery" -> "Bakery"
                        "Medicines" -> "Pharmacy"
                        "Meat & Fish" -> "Meat & Fish"
                        else -> ""
                    }
                    store.type == mappedType
                }
            }

            if (filteredStores.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EmptyStatePlaceholder(
                            icon = Icons.Default.DirectionsRun,
                            title = "No active vendors here",
                            subtitle = "Try selecting another Category tab above!"
                        )
                    }
                }
            } else {
                items(filteredStores) { store ->
                    MerchantStoreCard(store = store, onClick = { viewModel.selectStore(store.id) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // padding for sticky bottom bar
            }
        }
    }
}

@Composable
fun CustomerAISearchView(viewModel: MainViewModel) {
    val aiSearchQuery by viewModel.aiSearchQuery.collectAsStateWithLifecycle()
    val aiSearchResult by viewModel.aiSearchResult.collectAsStateWithLifecycle()
    val isAISearching by viewModel.isAISearching.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Semantic AI Search",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Type natural phrases like 'cheap biryani combo' or 'tomatoes under ₹50' to scan local shops instantly!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_search_input"),
                        placeholder = { Text("What are you craving or needing?") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.searchWithAI(textInput) },
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("ai_search_button")
                    ) {
                        Text("Ask")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isAISearching) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Consulting local marketplace databases...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (aiSearchResult != null) {
            val result = aiSearchResult!!
            Text(text = "quickz AI Recommendations:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            // Response explanation bubble
            Card(
                modifier = Modifier
                    .fillWithGlow()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.SmartButton,
                        contentDescription = "sparkle",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.explanation,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (result.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStatePlaceholder(
                        icon = Icons.Default.Warning,
                        title = "Zero Product Matches",
                        subtitle = "We couldn't locate matching items in active inventories."
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(result.products) { product ->
                        ProductCatalogCard(product = product, onAdd = { viewModel.addToCart(product) })
                    }
                }
            }
        } else {
            // Recommendation suggestions
            Text(text = "Try these standard requests:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val examples = listOf(
                "fresh organic potatoes",
                "cheap biryani combo with raita",
                "medicine for cold and fever",
                "Choco lava sweet dessert"
            )
            examples.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            textInput = item
                            viewModel.searchWithAI(item)
                        },
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerTrackingView(viewModel: MainViewModel) {
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val trackingOrder by viewModel.trackingOrder.collectAsStateWithLifecycle()
    val comments by viewModel.activeComments.collectAsStateWithLifecycle()

    var isRatingOrder by remember { mutableStateOf<Int?>(null) }
    var ratingInput by remember { mutableStateOf(5f) }
    var reviewInput by remember { mutableStateOf("") }

    if (trackingOrder != null) {
        val active = trackingOrder!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.setTrackingOrderId(null) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back")
                }
                Text(text = "Order #${active.id} Tracking", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                FilledTonalButton(onClick = { viewModel.updateOrderStatus(active.id, "DELIVERED") }) {
                    Text("Deemed Deliver")
                }
            }

            // Status Progress Layout
            TrackingStatusBar(status = active.status)

            // Custom Simulated Map Drawing
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 12.dp)
            ) {
                RiderLocationMap(
                    riderLat = active.riderLat,
                    riderLng = active.riderLng,
                    status = active.status
                )
            }

            // Rider Info
            if (active.riderName != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "rider", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = active.riderName ?: "Rider Assigned", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Assigned Delivery Partner", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { /* call mock */ }) {
                            Icon(Icons.Default.Call, contentDescription = "call", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Chat with Rider System
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Chat with Delivery Partner", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        LazyColumn(
                            reverseLayout = false,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            items(comments) { chat ->
                                ChatBubble(chat = chat)
                            }
                        }
                    }
                    var messageText by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Ask about delivery progress...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                if (messageText.trim().isNotEmpty()) {
                                    viewModel.sendChatMessage(active.id, "CUSTOMER", messageText)
                                    messageText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "send", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    } else {
        // Active and Past orders list
        if (allOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStatePlaceholder(
                    icon = Icons.Default.ShoppingBag,
                    title = "No Orders Placed Yet",
                    subtitle = "Browse the marketplace tab and checkout an order first!"
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                item {
                    Text(text = "Your Orders History", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                }
                items(allOrders) { order ->
                    OrderTrackerItemCard(
                        order = order,
                        onTrack = { viewModel.setTrackingOrderId(order.id) },
                        onRate = { isRatingOrder = order.id }
                    )
                }
            }
        }
    }

    if (isRatingOrder != null) {
        val oId = isRatingOrder!!
        Dialog(onDismissRequest = { isRatingOrder = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Rate Delivery Experience", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simple Star Rating
                    Row(horizontalArrangement = Arrangement.Center) {
                        for (star in 1..5) {
                            IconButton(onClick = { ratingInput = star.toFloat() }) {
                                Icon(
                                    imageVector = if (star <= ratingInput) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "star",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reviewInput,
                        onValueChange = { reviewInput = it },
                        placeholder = { Text("Write feedback regarding product quality or driver speed...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { isRatingOrder = null }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.rateOrder(oId, ratingInput, reviewInput)
                                isRatingOrder = null
                                reviewInput = ""
                            }
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// VENDOR VIEW & COMPONENTS
// ==========================================
@Composable
fun VendorView(viewModel: MainViewModel) {
    val store by viewModel.vendorStore.collectAsStateWithLifecycle()
    val orders by viewModel.vendorOrders.collectAsStateWithLifecycle()
    val products by viewModel.vendorProducts.collectAsStateWithLifecycle()

    var vendorTab by remember { mutableStateOf("ORDERS") } // "ORDERS", "STOCKS", "KYC"

    // KYC Form states
    var ownerName by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var panText by remember { mutableStateOf("") }
    var gstText by remember { mutableStateOf("") }
    var licenseText by remember { mutableStateOf("") }
    var storeType by remember { mutableStateOf("Restaurant") }
    var isSubmittedSuccess by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Store Details Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = store?.name ?: "Spice Garden Restaurant",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Owner: ${store?.ownerName ?: "Vijay Singhal"} • Status: APPROVED ✅",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                // Sales stats row
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Sales Today", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(text = "₹${"%.2f".format(store?.totalRevenue ?: 1290.0)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    Column {
                        Text(text = "Today's Orders", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(text = "${store?.totalOrders ?: 6}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    Column {
                        Text(text = "Rating score", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(text = "★ ${"%.1f".format(store?.rating ?: 4.6f)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Sub-Tabs
        TabRow(
            selectedTabIndex = when (vendorTab) {
                "ORDERS" -> 0
                "STOCKS" -> 1
                else -> 2
            }
        ) {
            Tab(selected = vendorTab == "ORDERS", onClick = { vendorTab = "ORDERS" }, text = { Text("Active Orders") })
            Tab(selected = vendorTab == "STOCKS", onClick = { vendorTab = "STOCKS" }, text = { Text("Inventory") })
            Tab(selected = vendorTab == "KYC", onClick = { vendorTab = "KYC" }, text = { Text("New Merchant Register") })
        }

        when (vendorTab) {
            "ORDERS" -> {
                if (orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStatePlaceholder(
                            icon = Icons.Default.Inbox,
                            title = "No Incoming Orders",
                            subtitle = "Customers placing orders from your shop will show up here instantly!"
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        items(orders) { order ->
                            IncomingVendorOrderCard(order = order, onAction = { id, st -> viewModel.updateOrderStatus(id, st) })
                        }
                    }
                }
            }
            "STOCKS" -> {
                LazyColumn(modifier = Modifier.padding(12.dp)) {
                    item {
                        Text(text = "Quick Stock Toggle", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(products) { prod ->
                        VendorProductLineItem(product = prod, onStockUpdate = { id, stock, out -> viewModel.updateProductStock(id, stock, out) })
                    }
                }
            }
            "KYC" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Onboard Your Local Store", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = "Fill up standard KYC verifications below. Admin approvals are evaluated live.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Merchant Owner Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop Business Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Dropdown simulation for shop type
                    var expandedDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = storeType,
                            onValueChange = {},
                            label = { Text("Shop Category Type") },
                            readOnly = true,
                            trailingIcon = { IconButton(onClick = { expandedDropdown = !expandedDropdown }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                            listOf("Restaurant", "Grocery", "Bakery", "Pharmacy", "Retail").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        storeType = option
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = panText,
                        onValueChange = { panText = it },
                        label = { Text("PAN Number (KYC)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = gstText,
                        onValueChange = { gstText = it },
                        label = { Text("GSTIN Identification") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = licenseText,
                        onValueChange = { licenseText = it },
                        label = { Text("Shop License/FSSAI") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (ownerName.isNotEmpty() && shopName.isNotEmpty()) {
                                viewModel.addStore(StoreEntity(
                                    name = shopName,
                                    type = storeType,
                                    rating = 4.5f,
                                    image = "merchant_custom_shop",
                                    address = "MG Road, Bangalore",
                                    isApproved = false,
                                    kycStatus = "SUBMITTED",
                                    pan = panText,
                                    gst = gstText,
                                    license = licenseText,
                                    ownerName = ownerName
                                ))
                                isSubmittedSuccess = true
                                // reset
                                ownerName = ""
                                shopName = ""
                                panText = ""
                                gstText = ""
                                licenseText = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_vendor_kyc")
                    ) {
                        Text("Submit KYC For Review")
                    }

                    if (isSubmittedSuccess) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text(
                                text = "Merchant registered and submitted successfully! Move to the ADMIN PANEL tab below to verify and grant active approvals.",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// RIDER DELIVERY APP VIEW
// ==========================================
@Composable
fun RiderView(viewModel: MainViewModel) {
    val activeRider by viewModel.activeRider.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val riderOrders by viewModel.riderOrders.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Rider profile banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DirectionsBike, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = activeRider?.name ?: "Rahul Kumar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Vehicle Type: ${activeRider?.vehicleType ?: "Electric Bike"} • Active Delivery", fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Rider Payout", fontSize = 10.sp)
                    Text(text = "₹${"%.2f".format(activeRider?.earnings ?: 480.0)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }
        }

        // Active Tasks vs Available Orders
        val activeDeliveringOrder = riderOrders.find { it.status == "ON_THE_WAY" || it.status == "PREPARING" || it.status == "READY" || it.status == "PENDING" }
        val availableOffers = allOrders.filter { (it.status == "PENDING" || it.status == "PREPARING" || it.status == "READY") && it.riderId == null }

        Spacer(modifier = Modifier.height(4.dp))
        if (activeDeliveringOrder != null) {
            Text(text = "🎯 YOUR CURRENT ACTIVE DELIVERY RUN", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Store: ${activeDeliveringOrder.storeName}", fontWeight = FontWeight.Bold)
                    Text(text = "Deliver To: ${activeDeliveringOrder.deliveryAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Consolidated Payout Amount: ₹${"%.2f".format(activeDeliveringOrder.deliveryFee + activeDeliveringOrder.tip)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    // Navigation Draw Map representing coordinates
                    RiderLocationMap(
                        riderLat = activeDeliveringOrder.riderLat,
                        riderLng = activeDeliveringOrder.riderLng,
                        status = activeDeliveringOrder.status
                    )

                    // Action transitions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeDeliveringOrder.status == "PREPARING" || activeDeliveringOrder.status == "PENDING") {
                            Text(text = "Waiting for merchant preparing...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { viewModel.updateOrderStatus(activeDeliveringOrder.id, "READY") }) {
                                Text("Pick up order")
                            }
                        } else if (activeDeliveringOrder.status == "READY") {
                            Button(
                                onClick = { viewModel.updateOrderStatus(activeDeliveringOrder.id, "ON_THE_WAY") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Start Route Navigation & Transit")
                            }
                        } else if (activeDeliveringOrder.status == "ON_THE_WAY") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Navigation, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "GPS Transiting...", fontSize = 11.sp)
                            }
                            Button(onClick = { viewModel.updateOrderStatus(activeDeliveringOrder.id, "DELIVERED") }) {
                                Text("Complete Handover")
                            }
                        }
                    }

                    // Simple quick reply chats
                    Text(text = "Quick communication to Customer:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Arrived at Store", "Stuck in traffic", "Arrived outside gating").forEach { phrase ->
                            FilledTonalButton(
                                onClick = { viewModel.sendChatMessage(activeDeliveringOrder.id, "RIDER", phrase) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = phrase, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Available request queue
            Text(text = "⚡ AVAILABLE HYPERLOCAL ROUTE MATCHES TODAY", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp))
            if (availableOffers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStatePlaceholder(
                        icon = Icons.Default.DirectionsRun,
                        title = "Zero active route demands",
                        subtitle = "When customer orders are placed, route matches show up here."
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.padding(12.dp)) {
                    items(availableOffers) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Offer #${order.id}", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Payout: ₹${"%.1f".format(order.deliveryFee + order.tip)}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(text = "From: ${order.storeName}", fontSize = 13.sp)
                                Text(text = "Routing to: ${order.deliveryAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { activeRider?.id?.let { rId -> viewModel.riderAcceptOrder(order.id, rId) } },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Accept Delivery Request")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// CENTRALIZED ADMIN ECOSYSTEM VIEW
// ==========================================
@Composable
fun AdminView(viewModel: MainViewModel) {
    val allStores by viewModel.allStores.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allRiders by viewModel.allRiders.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "System Administration Panel", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)

        // Aggregated ecosystem metrics cards
        Text(text = " Consolidated Financial Dashboard", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val totalGmv = allOrders.filter { it.status == "DELIVERED" }.sumOf { it.totalAmount }
            val adminCommission = allOrders.filter { it.status == "DELIVERED" }.sumOf { it.platformFee + (it.subtotal * 0.1) } // 10% platform cut

            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Delivered GMV", fontSize = 10.sp)
                    Text(text = "₹${"%.1f".format(totalGmv)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Collected Commission", fontSize = 10.sp)
                    Text(text = "₹${"%.1f".format(adminCommission)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // KYC Verification Queue
        Text(text = "📋 Pending KYC Shop verifications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        val pendingKyc = allStores.filter { !it.isApproved }
        if (pendingKyc.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Text(
                    text = "No pending KYC licenses to review. Registrations are pristine!",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            pendingKyc.forEach { store ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = store.name, fontWeight = FontWeight.Bold)
                        Text(text = "Owner: ${store.ownerName} • Category: ${store.type}", fontSize = 11.sp)
                        Text(text = "PAN: ${store.pan} • GSTIN: ${store.gst}", fontSize = 11.sp)
                        Text(text = "License: ${store.license}", fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.updateStoreApproval(store.id, true) }) {
                                Text("Approve Store")
                            }
                            OutlinedButton(onClick = { viewModel.updateStoreApproval(store.id, false) }) {
                                Text("Reject")
                            }
                        }
                    }
                }
            }
        }

        // Rider performance monitoring diagnostics
        Text(text = "🚴 Active Riders Fleet Dispatch", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                allRiders.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = r.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(text = "${r.vehicleType} • ★${r.rating}", fontSize = 10.sp)
                            }
                        }
                        Text(text = "₹${"%.1f".format(r.earnings)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-LEVEL COMMONS & WIDGET REPRESENTATIONS
// ==========================================
@Composable
fun ProductCatalogCard(product: ProductEntity, onAdd: () -> Unit) {
    val netPrice = product.price * (1 - product.discountPercent / 100.0)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .drawBehind {
                        // Drawing custom modern visual gradients on catalog item backgrounds as fluid design
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(LocalOrange.copy(alpha = 0.2f), Color.Green.copy(alpha = 0.1f))
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Large styled Category letters instead of blank visual defaults
                Text(
                    text = product.name.take(2).uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
                Text(
                    text = product.description,
                    maxLines = 2,
                    fontSize = 10.sp,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 12.sp,
                    modifier = Modifier.height(24.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (product.discountPercent > 0) {
                            Text(
                                text = "₹${"%.0f".format(product.price)}",
                                style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "₹${"%.1f".format(netPrice)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Button(
                        onClick = onAdd,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("add_to_cart_btn_${product.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("ADD", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantStoreCard(store: StoreEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
            .testTag("store_card_${store.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .drawBehind {
                        val colorSeed = when (store.type) {
                            "Restaurant" -> Color(0xFFFFCCBC)
                            "Grocery" -> Color(0xFFC8E6C9)
                            "Bakery" -> Color(0xFFFFF9C4)
                            else -> Color(0xFFB3E5FC)
                        }
                        drawRect(colorSeed)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (store.type) {
                        "Restaurant" -> Icons.Default.Restaurant
                        "Grocery" -> Icons.Default.LocalGroceryStore
                        "Bakery" -> Icons.Default.Cake
                        "Pharmacy" -> Icons.Default.LocalHospital
                        else -> Icons.Default.Storefront
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = store.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "${store.type} • 📍 ${store.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Instant 10 mins delivery", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }

            BadgeBox(rating = store.rating)
        }
    }
}

@Composable
fun BadgeBox(rating: Float) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE8F5E9))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = "", tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "%.1f".format(rating), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        }
    }
}

@Composable
fun TrackingStatusBar(status: String) {
    val statuses = listOf("PENDING", "PREPARING", "READY", "ON_THE_WAY", "DELIVERED")
    val currentIndex = statuses.indexOf(status)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            statuses.forEachIndexed { index, title ->
                val isActive = index <= currentIndex
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (title) {
                                "PENDING" -> Icons.Default.CallReceived
                                "PREPARING" -> Icons.Default.SoupKitchen
                                "READY" -> Icons.Default.DoneAll
                                "ON_THE_WAY" -> Icons.Default.TwoWheeler
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title.replace("_", " "),
                        fontSize = 8.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun RiderLocationMap(riderLat: Double, riderLng: Double, status: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulsing"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFECEFF1))
    ) {
        val w = size.width
        val h = size.height

        // Draw street intersections grid
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        
        // Horizontal streets
        for (y in 1..4) {
            drawLine(
                Color.White,
                start = Offset(0f, h * y / 5),
                end = Offset(w, h * y / 5),
                strokeWidth = 24f,
                cap = StrokeCap.Round
            )
        }
        // Vertical streets
        for (x in 1..4) {
            drawLine(
                Color.White,
                start = Offset(w * x / 5, 0f),
                end = Offset(w * x / 5, h),
                strokeWidth = 24f,
                cap = StrokeCap.Round
            )
        }

        // Draw Route line dashed from Store to House
        val storePos = Offset(w * 0.2f, h * 0.3f)
        val customerPos = Offset(w * 0.8f, h * 0.7f)
        
        drawLine(
            Color(0xFFFF5722).copy(alpha = 0.5f),
            start = storePos,
            end = customerPos,
            strokeWidth = 6f,
            pathEffect = pathEffect
        )

        // Draw Pins
        // Store PointPin
        drawCircle(
            Color(0xFF2E7D32),
            radius = 12f,
            center = storePos
        )
        // Store tag
        drawCircle(
            Color.White,
            radius = 6f,
            center = storePos
        )

        // Customer PointPin
        drawCircle(
            Color(0xFFE53935),
            radius = 12f,
            center = customerPos
        )
        drawCircle(
            Color.White,
            radius = 6f,
            center = customerPos
        )

        // Rider current moving position based on coordinate interpolation
        if (status == "ON_THE_WAY") {
            // map real Bangalore coordinates to canvas grid
            val startLat = 12.9716
            val startLng = 77.5946
            val destLat = 12.9925
            val destLng = 77.6240

            val relativeX = ((riderLng - startLng) / (destLng - startLng)).coerceIn(0.0, 1.0)
            val relativeY = ((riderLat - startLat) / (destLat - startLat)).coerceIn(0.0, 1.0)

            val riderPos = Offset(
                (storePos.x + (customerPos.x - storePos.x) * relativeX).toFloat(),
                (storePos.y + (customerPos.y - storePos.y) * relativeY).toFloat()
            )

            // Pulse anchor glow
            drawCircle(
                Color(0xFFFF5722).copy(alpha = 0.4f),
                radius = pulseSize,
                center = riderPos
            )

            // Rider scooter pinpoint
            drawCircle(
                Color(0xFFFF9100),
                radius = 14f,
                center = riderPos
            )
            drawCircle(
                Color.White,
                radius = 5f,
                center = riderPos
            )
        }
    }
}

@Composable
fun OrderTrackerItemCard(order: OrderEntity, onTrack: () -> Unit, onRate: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = order.storeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = order.status,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = when (order.status) {
                        "PENDING" -> MaterialTheme.colorScheme.primary
                        "DELIVERED" -> Color(0xFF2E7D32)
                        else -> Color(0xFFFF9100)
                    }
                )
            }
            Text(text = "Amount: ₹${"%.1f".format(order.totalAmount)} • Date: ${SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(order.timestamp))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (order.status != "DELIVERED" && order.status != "REJECTED") {
                    Button(onClick = onTrack) {
                        Icon(Icons.Default.Navigation, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live Track")
                    }
                } else {
                    if (order.orderRating == null) {
                        OutlinedButton(onClick = onRate) {
                            Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rate Experience")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Rated: ★${order.orderRating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(chat: ChatEntity) {
    val isCustomer = chat.sender == "CUSTOMER"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCustomer) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isCustomer) 12.dp else 0.dp,
                bottomEnd = if (isCustomer) 0.dp else 12.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isCustomer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = chat.message,
                modifier = Modifier.padding(8.dp),
                color = if (isCustomer) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun IncomingVendorOrderCard(order: OrderEntity, onAction: (Int, String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Order #${order.id}", fontWeight = FontWeight.Bold)
                Text(text = "Amount: ₹${"%.1f".format(order.subtotal)}", fontWeight = FontWeight.Bold)
            }
            Text(text = "Delivery Addr: ${order.deliveryAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Items: ${order.itemsJson}", fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                when (order.status) {
                    "PENDING" -> {
                        Button(onClick = { onAction(order.id, "PREPARING") }) { Text("Accept & Prepare") }
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(onClick = { onAction(order.id, "REJECTED") }) { Text("Reject", color = MaterialTheme.colorScheme.error) }
                    }
                    "PREPARING" -> {
                        Button(onClick = { onAction(order.id, "READY") }) { Text("Mark Ready for Rider") }
                    }
                    "READY" -> {
                        Text(text = "Awaiting Rider Pick up...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    else -> {
                        Text(text = "In transit / Finished", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VendorProductLineItem(product: ProductEntity, onStockUpdate: (Int, Int, Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Current Stock: ${product.stockLevel}", fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onStockUpdate(product.id, (product.stockLevel - 10).coerceAtLeast(0), product.stockLevel <= 1) }) {
                    Icon(Icons.Default.Remove, null)
                }
                Text(text = "${product.stockLevel}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                IconButton(onClick = { onStockUpdate(product.id, product.stockLevel + 10, false) }) {
                    Icon(Icons.Default.Add, null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = !product.isOutOfStock,
                    onCheckedChange = { isAvailable -> onStockUpdate(product.id, if (isAvailable) 20 else 0, !isAvailable) }
                )
            }
        }
    }
}

// Dialog checkout
@Composable
fun CartCheckoutDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val items by viewModel.cartItems.collectAsStateWithLifecycle()
    var address by remember { mutableStateOf("102 Shanti Building, Koramangala, Bangalore") }
    var tipInput by remember { mutableStateOf("20") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Place Hyperlocal Order", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                
                // Item breakdown list
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${item.productName} (x${item.quantity})", fontSize = 13.sp)
                        Text(text = "₹${"%.1f".format(item.price * item.quantity)}", fontSize = 13.sp)
                    }
                }
                Divider()

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Destination Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = tipInput,
                    onValueChange = { tipInput = it },
                    label = { Text("Driver Tip (optional, ₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val sub = items.sumOf { it.price * itemQuantity(it.quantity) }
                val del = 35.0
                val tax = sub * 0.05
                val pf = 5.0
                val tip = tipInput.doubleOrNull() ?: 0.0
                val total = sub + del + tax + pf + tip

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Subtotal:", fontSize = 11.sp)
                        Text(text = "₹${"%.1f".format(sub)}", fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Delivery & Platform charge:", fontSize = 11.sp)
                        Text(text = "₹${"%.1f".format(del + pf)}", fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Taxes:", fontSize = 11.sp)
                        Text(text = "₹${"%.1f".format(tax)}", fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Amount Payable:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "₹${"%.1f".format(total)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.checkout(address, tip)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("submit_checkout")
                    ) {
                        Text("Pay & Checkout")
                    }
                }
            }
        }
    }
}

// Helpers
fun itemQuantity(quantity: Int) = quantity.toDouble()
fun String.doubleOrNull(): Double? = this.toDoubleOrNull()

@Composable
fun EmptyStatePlaceholder(icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Custom modern modifier visual card effects
fun Modifier.fillWithGlow(): Modifier = this.drawBehind {
    drawRoundRect(
        color = Color(0xFFE0E0E0),
        size = Size(width = size.width, height = size.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )
}

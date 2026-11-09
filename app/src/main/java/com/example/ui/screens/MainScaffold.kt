package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.material.icons.filled.Settings
import com.example.ui.navigation.NavigationDestination
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.products.AddProductScreen
import com.example.ui.screens.products.ProductsScreen
import com.example.ui.screens.purchases.PurchasesScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.sales.CartScreen
import com.example.ui.screens.sales.SalesScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepPlumPrimary
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.MutedBlushRose
import com.example.ui.theme.SoftBeige
import com.example.ui.theme.SoftRoseLight
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(NavigationDestination.DASHBOARD) }
    var showAddProductScreen by remember { mutableStateOf(false) }
    var showCartScreen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }

    val uiState by storeViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            storeViewModel.dismissToast()
        }
    }

    // الرجوع للرئيسية عند الضغط على زر الرجوع من أي قسم ثانوي
    BackHandler(enabled = showSettingsScreen || showAddProductScreen || showCartScreen || currentDestination != NavigationDestination.DASHBOARD) {
        when {
            showSettingsScreen -> showSettingsScreen = false
            showAddProductScreen -> showAddProductScreen = false
            showCartScreen -> showCartScreen = false
            else -> currentDestination = NavigationDestination.DASHBOARD
        }
    }

    // واجهة عربية أصيلة مع اتجاه RTL
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (showSettingsScreen) {
            SettingsScreen(
                storeViewModel = storeViewModel,
                onNavigateBack = { showSettingsScreen = false }
            )
        } else if (showAddProductScreen) {
            AddProductScreen(
                onDismiss = { showAddProductScreen = false },
                onSaveProduct = { product ->
                    storeViewModel.addProduct(product)
                    showAddProductScreen = false
                }
            )
        } else if (showCartScreen) {
            CartScreen(
                storeViewModel = storeViewModel,
                onNavigateBackToCatalog = { showCartScreen = false }
            )
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize().testTag("main_scaffold"),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = SoftRoseLight,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ebra_khait_icon_1794081811096),
                                            contentDescription = "شعار إبرة وخيط",
                                            modifier = Modifier.size(28.dp).clip(CircleShape)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "إبرة وخيط",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DeepPlumPrimary
                                    )
                                    Text(
                                        text = "أزياء نسائية فاخرة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        },
                        actions = {
                            // زر الإعدادات والنسخ الاحتياطي
                            IconButton(
                                onClick = { showSettingsScreen = true },
                                modifier = Modifier.testTag("top_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "الإعدادات والنسخ الاحتياطي",
                                    tint = DeepPlumPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // زر سلة المبيعات مع الشارة في الشريط العلوي
                            val cartCount = uiState.cartItems.sumOf { it.quantity }
                            IconButton(
                                onClick = { showCartScreen = true },
                                modifier = Modifier.testTag("top_cart_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (cartCount > 0) {
                                            Badge(containerColor = MutedBlushRose, contentColor = Color.White) {
                                                Text("$cartCount", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = "السلة",
                                        tint = DeepPlumPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamSurface)
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = CreamSurface,
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("bottom_nav_bar")
                    ) {
                        NavigationDestination.values().forEach { destination ->
                            val isSelected = currentDestination == destination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = DeepPlumPrimary,
                                    indicatorColor = DeepPlumPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_${destination.route}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Crossfade(
                    targetState = currentDestination,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    label = "MainTabsCrossfade"
                ) { destination ->
                    when (destination) {
                        NavigationDestination.DASHBOARD -> DashboardScreen(
                            storeViewModel = storeViewModel,
                            onNavigateToSection = { currentDestination = it }
                        )
                        NavigationDestination.PRODUCTS -> ProductsScreen(
                            storeViewModel = storeViewModel,
                            onOpenAddProduct = { showAddProductScreen = true }
                        )
                        NavigationDestination.PURCHASES -> PurchasesScreen(
                            storeViewModel = storeViewModel
                        )
                        NavigationDestination.SALES -> SalesScreen(
                            storeViewModel = storeViewModel,
                            onOpenCart = { showCartScreen = true }
                        )
                        NavigationDestination.EXPENSES -> ExpensesScreen(
                            storeViewModel = storeViewModel
                        )
                        NavigationDestination.REPORTS -> ReportsScreen(
                            storeViewModel = storeViewModel
                        )
                    }
                }
            }
        }
    }
}

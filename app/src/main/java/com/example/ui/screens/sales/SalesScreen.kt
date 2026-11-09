package com.example.ui.screens.sales

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ProductModel
import com.example.ui.components.AppEmptyState
import com.example.ui.components.formatIqd
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepPlumPrimary
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldAccentIqd
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.MutedBlushRose
import com.example.ui.theme.SoftBeige
import com.example.ui.theme.SoftRoseLight
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VelvetRoseError
import com.example.ui.viewmodel.SaleStage
import com.example.ui.viewmodel.StoreViewModel

/**
 * قسم المبيعات:
 * 1. نقطة البيع السريعة (POS) واختيار المنتجات والألوان والقياسات بالتدفق الخماسي
 * 2. إدارة سلة المشتريات
 * 3. استعراض سجل الفواتير والمبيعات السابقة ومشاركتها
 */
@Composable
fun SalesScreen(
    storeViewModel: StoreViewModel,
    onOpenCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val uiState by storeViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(uiState.products, searchQuery) {
        if (searchQuery.isBlank()) uiState.products
        else uiState.products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("sales_screen"),
        floatingActionButton = {
            if (selectedTab == 0 && uiState.currentSaleStage == SaleStage.NONE) {
                val totalCartCount = uiState.cartItems.sumOf { it.quantity }
                val totalCartAmount = uiState.cartItems.sumOf { it.totalItemPriceIqd }

                if (totalCartCount > 0) {
                    // ودجت السلة المكبر بنسبة 200% عريض وبارز مع تفاصيل العدد والإجمالي
                    Surface(
                        onClick = onOpenCart,
                        shape = RoundedCornerShape(22.dp),
                        color = DeepPlumPrimary,
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MutedBlushRose.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .height(68.dp)
                            .padding(horizontal = 4.dp)
                            .testTag("sales_floating_cart_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MutedBlushRose,
                                        contentColor = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Text(
                                            text = "$totalCartCount",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "سلة المبيعات",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "سلة المبيعات ($totalCartCount قطع)",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatIqd(totalCartAmount),
                                    color = GoldAccentIqd,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                } else {
                    // ودجت السلة الفارغة مكبر بنسبة 200% (قطر 72dp وأيقونة 34dp)
                    FloatingActionButton(
                        onClick = onOpenCart,
                        containerColor = DeepPlumPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("sales_floating_cart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "سلة المبيعات",
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(IvoryBackground)
        ) {
            // علامات التبويب العلوية: نقطة البيع السريعة | سجل الفواتير
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CreamSurface,
                contentColor = DeepPlumPrimary,
                modifier = Modifier.fillMaxWidth().testTag("sales_tab_row")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        storeViewModel.cancelSaleFlow()
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نقطة البيع (بيع جديد)", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        storeViewModel.cancelSaleFlow()
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("سجل الفواتير (${uiState.salesInvoices.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }

            if (selectedTab == 1) {
                // ====================================================
                // التبويب الثاني: سجل الفواتير المنفذة
                // ====================================================
                SalesHistoryScreen(storeViewModel = storeViewModel)
            } else {
                // ====================================================
                // التبويب الأول: نقطة البيع والتدفق الخماسي
                // ====================================================
                when (uiState.currentSaleStage) {
                    SaleStage.NONE -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // شريط البحث في الموديلات للبيع السريع
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("ابحثي عن موديل للبيع...", fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotBlank()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("pos_search_field")
                                )

                                val cartItemsCount = uiState.cartItems.sumOf { it.quantity }
                                Button(
                                    onClick = onOpenCart,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                    modifier = Modifier.testTag("pos_open_cart_button")
                                ) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("السلة ($cartItemsCount)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (uiState.products.isEmpty()) {
                                AppEmptyState(
                                    icon = Icons.Default.Checkroom,
                                    title = "لا توجد بضاعة للبيع",
                                    description = "توجهي لقسم المنتجات لإضافة موديلات أزياء وتحديد مخزونها لتبدأ عملية البيع",
                                    actionLabel = null,
                                    onActionClick = null
                                )
                            } else if (filteredProducts.isEmpty()) {
                                AppEmptyState(
                                    icon = Icons.Default.Search,
                                    title = "لا توجد نتائج مطابقة",
                                    description = "تأكدي من صحة اسم الموديل المكتوب في حقل البحث",
                                    actionLabel = "مسح البحث",
                                    onActionClick = { searchQuery = "" }
                                )
                            } else {
                                // شبكة الكتالوج بثلاثة أعمدة لسهولة وسرعة اختيار الموديل بنسبة 3:4
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize().testTag("pos_3_col_grid")
                                ) {
                                    items(filteredProducts, key = { it.id }) { product ->
                                        PosProductCard(
                                            product = product,
                                            onClick = { storeViewModel.startSaleFlow(product) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // المرحلة 2: اختيار اللون
                    SaleStage.SELECT_COLOR -> {
                        val model = uiState.activeSaleModel
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = { storeViewModel.cancelSaleFlow() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "إلغاء")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "المرحلة 2: اختاري اللون",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DeepPlumPrimary
                                    )
                                    Text(
                                        text = model?.name ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            HorizontalDivider(color = BorderSubtle)

                            Text(
                                text = "اضغطي على صورة اللون المطلوب للموديل:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextMain
                            )

                            val colors = model?.colors ?: emptyList()
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(colors, key = { it.colorId }) { color ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { storeViewModel.selectSaleColor(color) }
                                            .testTag("pos_color_item_${color.colorName}"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = CreamSurface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(SoftBeige),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (color.colorImageUri != null) {
                                                    AsyncImage(
                                                        model = color.colorImageUri,
                                                        contentDescription = color.colorName,
                                                        contentScale = ContentScale.Fit,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Palette,
                                                        contentDescription = null,
                                                        tint = MutedBlushRose,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = color.colorName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = DeepPlumPrimary,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "${color.totalColorStock} قطعة متوفرة",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (color.totalColorStock > 0) ForestGreen else VelvetRoseError
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // المرحلة 3: اختيار القياس لهذا اللون
                    SaleStage.SELECT_SIZE -> {
                        val model = uiState.activeSaleModel
                        val color = uiState.activeSaleColor

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = { storeViewModel.startSaleFlow(model!!) }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "تغيير اللون")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "المرحلة 3: اختاري القياس",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DeepPlumPrimary
                                    )
                                    Text(
                                        text = "${model?.name} - لون: ${color?.colorName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            HorizontalDivider(color = BorderSubtle)

                            Text(
                                text = "القياسات المتوفرة في لون (${color?.colorName}):",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextMain
                            )

                            val sizes = color?.sizes ?: emptyList()
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                sizes.forEach { size ->
                                    val isAvailable = size.currentStock > 0
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = isAvailable) {
                                                storeViewModel.selectSaleSize(size)
                                            }
                                            .testTag("pos_size_item_${size.sizeLabel}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isAvailable) CreamSurface else SoftBeige.copy(alpha = 0.5f)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = if (isAvailable) 2.dp else 0.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isAvailable) SoftRoseLight else SoftBeige,
                                                    modifier = Modifier.size(42.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = size.sizeLabel,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isAvailable) DeepPlumPrimary else TextMuted,
                                                            fontSize = 15.sp
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "قياس ${size.sizeLabel}",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isAvailable) TextMain else TextMuted
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isAvailable) ForestGreen.copy(alpha = 0.12f) else VelvetRoseError.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = if (isAvailable) "متوفر ${size.currentStock} قطع" else "نفد المخزون",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAvailable) ForestGreen else VelvetRoseError,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // المرحلة 4: الكمية وسعر البيع
                    SaleStage.SELECT_QTY_PRICE -> {
                        val model = uiState.activeSaleModel
                        val color = uiState.activeSaleColor
                        val size = uiState.activeSaleSize
                        val qty = uiState.activeSaleQuantity
                        val price = uiState.activeSalePrice

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = { storeViewModel.selectSaleColor(color!!) }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "تغيير القياس")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "المرحلة 4: الكمية وسعر البيع",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DeepPlumPrimary
                                    )
                                    Text(
                                        text = "${model?.name} | ${color?.colorName} | قياس ${size?.sizeLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            HorizontalDivider(color = BorderSubtle)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CreamSurface)
                            ) {
                                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    // تحديد الكمية
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "الكمية المطلوبة:",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "المتوفر بالمخزن: ${size?.currentStock} قطع",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ForestGreen
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { storeViewModel.updateSaleQuantity(qty - 1) },
                                                enabled = qty > 1,
                                                modifier = Modifier.size(38.dp).background(SoftBeige, CircleShape)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "تقليل")
                                            }

                                            Text(
                                                text = "$qty",
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )

                                            val canAddMore = qty < (size?.currentStock ?: 1L)
                                            IconButton(
                                                onClick = { storeViewModel.updateSaleQuantity(qty + 1) },
                                                enabled = canAddMore,
                                                modifier = Modifier.size(38.dp).background(if (canAddMore) SoftRoseLight else SoftBeige, CircleShape)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "زيادة")
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))

                                    // سعر بيع القطعة القابل للتعديل لهذه الفاتورة
                                    var priceInput by remember { mutableStateOf(price.toString()) }
                                    OutlinedTextField(
                                        value = priceInput,
                                        onValueChange = { input ->
                                            if (input.all { it.isDigit() }) {
                                                priceInput = input
                                                storeViewModel.updateSaleCustomPrice(input.toLongOrNull() ?: 0L)
                                            }
                                        },
                                        label = { Text("سعر بيع القطعة لهذه الفاتورة (د.ع)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "إجمالي الصنف:",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = formatIqd(qty * price),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = GoldAccentIqd
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // زر الإضافة للسلة والرجوع للكتالوج
                            Button(
                                onClick = { storeViewModel.confirmAddToCart() },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("pos_add_to_cart_confirm_button")
                            ) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إضافة للسلة والرجوع للكتالوج", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * بطاقة عرض الموديل في نقطة البيع بنسبة طولية 3:4 لعدم قص ملابس العرض
 */
@Composable
fun PosProductCard(
    product: ProductModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("pos_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SoftBeige),
                contentAlignment = Alignment.Center
            ) {
                if (product.mainImageUri != null) {
                    AsyncImage(
                        model = product.mainImageUri,
                        contentDescription = product.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        tint = MutedBlushRose.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (product.totalPieces > 0) ForestGreen.copy(alpha = 0.85f) else VelvetRoseError.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Text(
                        text = if (product.totalPieces > 0) "${product.totalPieces} ق" else "نفد",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = TextMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatIqd(product.defaultSellingPriceIqd),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = GoldAccentIqd,
                maxLines = 1
            )
        }
    }
}

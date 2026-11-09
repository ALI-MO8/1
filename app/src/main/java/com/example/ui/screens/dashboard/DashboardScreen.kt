package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.FifoInventoryEngine
import com.example.ui.components.StatCard
import com.example.ui.components.formatIqd
import com.example.ui.navigation.NavigationDestination
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
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    storeViewModel: StoreViewModel,
    onNavigateToSection: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by storeViewModel.uiState.collectAsState()

    // 1. عدد الموديلات
    val totalModelsCount = uiState.products.size

    // 2. إجمالي القطع الموجودة
    val totalStockPieces = uiState.products.sumOf { it.totalPieces }

    // 3. قيمة المخزون بسعر التكلفة (ممنوع سعر البيع)
    val inventoryValueAtCost = FifoInventoryEngine.calculateTotalInventoryValuationAtCost(uiState.purchaseBatches)

    // 4. مبيعات اليوم ومبيعات الشهر
    val todayDatePrefix = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
    val monthDatePrefix = SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(Date())

    val todaySales = uiState.salesInvoices
        .filter { it.saleDateTime.startsWith(todayDatePrefix) }
        .sumOf { it.totalAmountIqd }

    val monthSales = uiState.salesInvoices
        .filter { it.saleDateTime.startsWith(monthDatePrefix) }
        .sumOf { it.totalAmountIqd }

    // 5. الربح الإجمالي وصافي الربح
    val totalRevenue = uiState.salesInvoices.sumOf { it.totalAmountIqd }
    val totalCogs = uiState.salesInvoices.sumOf { it.cogsTotalIqd }
    val grossProfit = totalRevenue - totalCogs
    val totalExpenses = uiState.expenses.sumOf { it.amountIqd }
    val netProfit = grossProfit - totalExpenses

    // 6. المنتجات التي قاربت على النفاد (المقاسات المتبقي منها 3 قطع أو أقل)
    val lowStockItems = mutableListOf<Triple<String, String, Long>>() // ModelName, Details, remaining
    uiState.products.forEach { prod ->
        prod.colors.forEach { col ->
            col.sizes.forEach { sz ->
                if (sz.currentStock in 1..3) {
                    lowStockItems.add(Triple(prod.name, "${col.colorName} - قياس ${sz.sizeLabel}", sz.currentStock))
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // بطاقة ملخص المتجر وقيمة المخزون بسعر التكلفة
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = CreamSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "إبرة وخيط - أزياء نسائية",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepPlumPrimary
                            )
                            Text(
                                text = "قيمة المخزون الفعلية بسعر التكلفة (FIFO)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = SoftRoseLight) {
                            Text(
                                text = "مخزون تكلفة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepPlumPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = formatIqd(inventoryValueAtCost),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccentIqd,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "إجمالي الموديلات: $totalModelsCount موديل",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMain
                        )
                        Text(
                            text = "• إجمالي القطع: $totalStockPieces قطعة",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = ForestGreen
                        )
                    }
                }
            }
        }

        // بطاقات المؤشرات المالية: مبيعات اليوم والشهر
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "مبيعات اليوم",
                    value = formatIqd(todaySales),
                    icon = Icons.Default.PointOfSale,
                    iconTint = ForestGreen,
                    subValue = "إجمالي إيراد اليوم",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "مبيعات الشهر",
                    value = formatIqd(monthSales),
                    icon = Icons.Default.TrendingUp,
                    iconTint = DeepPlumPrimary,
                    subValue = "إيرادات الشهر الحالي",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // بطاقات الأرباح: الربح الإجمالي وصافي الربح
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "الربح الإجمالي",
                    value = formatIqd(grossProfit),
                    icon = Icons.Default.AttachMoney,
                    iconTint = GoldAccentIqd,
                    subValue = "المبيعات - تكلفة FIFO",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "صافي الربح",
                    value = formatIqd(netProfit),
                    icon = Icons.Default.TrendingUp,
                    iconTint = if (netProfit >= 0) ForestGreen else VelvetRoseError,
                    subValue = "بعد خصم المصاريف",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // تنبيه المنتجات التي قاربت على النفاد
        if (lowStockItems.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VelvetRoseError.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = VelvetRoseError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "منتجات قاربت على النفاد (${lowStockItems.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = VelvetRoseError
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        lowStockItems.take(5).forEach { (model, details, remaining) ->
                            Text(
                                text = "• $model ($details) - متبقي $remaining قطع فقط",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMain
                            )
                        }
                    }
                }
            }
        }

        // أزرار الوصول السريع
        item {
            Text(
                text = "الإجراءات السريعة في المحل",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepPlumPrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    title = "إدارة المنتجات",
                    icon = Icons.Default.Checkroom,
                    containerColor = DeepPlumPrimary,
                    onClick = { onNavigateToSection(NavigationDestination.PRODUCTS) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    title = "نقطة البيع",
                    icon = Icons.Default.PointOfSale,
                    containerColor = MutedBlushRose,
                    onClick = { onNavigateToSection(NavigationDestination.SALES) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    title = "توريد دفعة",
                    icon = Icons.Default.Inventory,
                    containerColor = GoldAccentIqd,
                    onClick = { onNavigateToSection(NavigationDestination.PURCHASES) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    title = "تسجيل مصروف",
                    icon = Icons.Default.ReceiptLong,
                    containerColor = ForestGreen,
                    onClick = { onNavigateToSection(NavigationDestination.EXPENSES) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // أحدث الموديلات المضافة
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أحدث الموديلات في المحل",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepPlumPrimary
                )
                OutlinedButton(
                    onClick = { onNavigateToSection(NavigationDestination.PRODUCTS) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("عرض المنتجات", fontSize = 12.sp)
                }
            }
        }

        if (uiState.products.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لم تتم إضافة أي موديل للمحل حتى الآن",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onNavigateToSection(NavigationDestination.PRODUCTS) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary)
                        ) {
                            Icon(Icons.Default.Checkroom, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الذهاب لقسم المنتجات لإضافة موديل")
                        }
                    }
                }
            }
        } else {
            items(uiState.products.take(4), key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextMain
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${product.colors.size} ألوان متوفرة",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatIqd(product.defaultSellingPriceIqd),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldAccentIqd
                            )
                            Text(
                                text = "${product.totalPieces} قطعة بالمخزن",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (product.totalPieces > 0) ForestGreen else VelvetRoseError
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
        modifier = modifier.height(60.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

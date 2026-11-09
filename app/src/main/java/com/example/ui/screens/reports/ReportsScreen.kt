package com.example.ui.screens.reports

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.FifoInventoryEngine
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
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by storeViewModel.uiState.collectAsState()

    // 1. إجمالي الإيرادات وتكلفة FIFO
    val totalRevenue = uiState.salesInvoices.sumOf { it.totalAmountIqd }
    val totalCogs = uiState.salesInvoices.sumOf { it.cogsTotalIqd }
    val grossProfit = totalRevenue - totalCogs

    // 2. المصاريف التشغيلية وصافي الربح
    val totalExpenses = uiState.expenses.sumOf { it.amountIqd }
    val netProfit = grossProfit - totalExpenses

    // 3. قيمة المخزون الحقيقية بسعر التكلفة لكل دفعة (ممنوع حسابها بسعر البيع)
    val inventoryValueAtCost = FifoInventoryEngine.calculateTotalInventoryValuationAtCost(uiState.purchaseBatches)

    // مبيعات اليوم والشهر
    val todayDatePrefix = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
    val monthDatePrefix = SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(Date())

    val todaySales = uiState.salesInvoices
        .filter { it.saleDateTime.startsWith(todayDatePrefix) }
        .sumOf { it.totalAmountIqd }

    val monthSales = uiState.salesInvoices
        .filter { it.saleDateTime.startsWith(monthDatePrefix) }
        .sumOf { it.totalAmountIqd }

    // تحليلات الأكثر مبيعاً
    val allSoldItems = uiState.salesInvoices.flatMap { it.items }

    val topModels = allSoldItems
        .groupBy { it.productName }
        .mapValues { entry -> entry.value.sumOf { it.quantity } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    val topColors = allSoldItems
        .groupBy { it.colorName }
        .mapValues { entry -> entry.value.sumOf { it.quantity } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    val topSizes = allSoldItems
        .groupBy { it.sizeLabel }
        .mapValues { entry -> entry.value.sumOf { it.quantity } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "التقارير المحاسبية والأرباح الدقيقة",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = DeepPlumPrimary
            )
            Text(
                text = "حسابات FIFO الدقيقة: الإيرادات، تكلفة البضاعة المباعة، وقيمة المخزون بسعر التكلفة",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        // كشف الأرباح والمبيعات الرئيسي
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profit_summary_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "كشف حساب الأرباح (FIFO)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepPlumPrimary
                        )
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ForestGreen)
                    }

                    HorizontalDivider(color = SoftBeige)

                    ReportRow("إجمالي الإيرادات (المبيعات):", formatIqd(totalRevenue), ForestGreen, isBold = true)
                    ReportRow("تكلفة البضاعة المباعة (COGS):", formatIqd(totalCogs), TextMuted)
                    ReportRow("الربح الإجمالي (الإيرادات - التكلفة):", formatIqd(grossProfit), GoldAccentIqd, isBold = true)
                    ReportRow("المصاريف التشغيلية للمحل:", formatIqd(totalExpenses), VelvetRoseError)

                    HorizontalDivider(color = SoftBeige)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "صافي الربح الفعلي:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = DeepPlumPrimary
                        )
                        Text(
                            text = formatIqd(netProfit),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (netProfit >= 0) ForestGreen else VelvetRoseError
                        )
                    }
                }
            }
        }

        // بطاقة المبيعات الدورية وقيمة المخزون بسعر التكلفة
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("inventory_cost_report_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SoftBeige)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "المبيعات الدورية ورأس المال المخزني",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepPlumPrimary
                    )

                    ReportRow("مبيعات اليوم:", formatIqd(todaySales), DeepPlumPrimary)
                    ReportRow("مبيعات الشهر الحالي:", formatIqd(monthSales), DeepPlumPrimary)

                    HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))

                    ReportRow(
                        label = "قيمة المخزون بسعر التكلفة (FIFO):",
                        value = formatIqd(inventoryValueAtCost),
                        color = GoldAccentIqd,
                        isBold = true
                    )
                    Text(
                        text = "* تم احتساب قيمة المخزون بمجموع (الكميات المتبقية في كل دفعة × تكلفة الشراء الأصلية لتلك الدفعة).",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // تقرير الأصناف الأكثر طلباً والألوان والقياسات
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "تحليلات الأكثر مبيعاً في المحل",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepPlumPrimary
                    )

                    // أكثر الموديلات مبيعاً
                    Text(text = "👗 أكثر الموديلات طلباً:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (topModels.isEmpty()) {
                        Text("لا توجد مبيعات مسجلة حتى الآن", fontSize = 12.sp, color = TextMuted)
                    } else {
                        topModels.forEach { (name, qty) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, fontSize = 13.sp)
                                Text("$qty قطعة", fontWeight = FontWeight.Bold, color = ForestGreen)
                            }
                        }
                    }

                    HorizontalDivider(color = SoftBeige)

                    // الألوان الأكثر مبيعاً
                    Text(text = "🎨 الألوان الأكثر مبيعاً:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (topColors.isEmpty()) {
                        Text("لا توجد مبيعات مسجلة حتى الآن", fontSize = 12.sp, color = TextMuted)
                    } else {
                        topColors.forEach { (color, qty) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(color, fontSize = 13.sp)
                                Text("$qty قطعة", fontWeight = FontWeight.Bold, color = DeepPlumPrimary)
                            }
                        }
                    }

                    HorizontalDivider(color = SoftBeige)

                    // القياسات الأكثر طلباً
                    Text(text = "📏 القياسات الأكثر طلباً:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (topSizes.isEmpty()) {
                        Text("لا توجد مبيعات مسجلة حتى الآن", fontSize = 12.sp, color = TextMuted)
                    } else {
                        topSizes.forEach { (size, qty) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("قياس $size", fontSize = 13.sp)
                                Text("$qty قطعة", fontWeight = FontWeight.Bold, color = GoldAccentIqd)
                            }
                        }
                    }
                }
            }
        }

        // الكميات المتبقية من كل موديل في المخزن
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "الكميات المتبقية من كل موديل:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepPlumPrimary
                    )

                    if (uiState.products.isEmpty()) {
                        Text("لا توجد موديلات مضافة بالمخزن", fontSize = 12.sp, color = TextMuted)
                    } else {
                        uiState.products.forEach { prod ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prod.name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (prod.totalPieces > 0) ForestGreen.copy(alpha = 0.12f) else VelvetRoseError.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${prod.totalPieces} قطعة متوفرة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (prod.totalPieces > 0) ForestGreen else VelvetRoseError,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(
    label: String,
    value: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = TextMain
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = color
        )
    }
}

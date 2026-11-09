package com.example.ui.screens.sales

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
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppEmptyState
import com.example.ui.components.formatIqd
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepPlumPrimary
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldAccentIqd
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.SoftBeige
import com.example.ui.theme.SoftRoseLight
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun SalesHistoryScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by storeViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .testTag("sales_history_screen")
    ) {
        val totalRevenue = uiState.salesInvoices.sumOf { it.totalAmountIqd }
        val totalInvoicesCount = uiState.salesInvoices.size

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "سجل الفواتير والمبيعات",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = DeepPlumPrimary
                )
                Text(
                    text = "$totalInvoicesCount فاتورة منفذة",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SoftRoseLight
            ) {
                Text(
                    text = "الإجمالي: ${formatIqd(totalRevenue)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepPlumPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        if (uiState.salesInvoices.isEmpty()) {
            AppEmptyState(
                icon = Icons.Default.PointOfSale,
                title = "لا توجد مبيعات مسجلة بعد",
                description = "اختاري الملابس النسائية من الكتالوج لإتمام أول فاتورة بيع مع حساب تكلفة FIFO والأرباح تلقائياً.",
                actionLabel = null,
                onActionClick = null
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.salesInvoices, key = { it.invoiceId }) { invoice ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("sale_invoice_${invoice.invoiceNumber}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SoftBeige,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = DeepPlumPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "فاتورة: ${invoice.invoiceNumber}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = DeepPlumPrimary
                                        )
                                        Text(
                                            text = invoice.saleDateTime,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatIqd(invoice.totalAmountIqd),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GoldAccentIqd,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "الربح: +${formatIqd(invoice.grossProfitIqd)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { shareInvoiceText(context, invoice) },
                                        modifier = Modifier.size(32.dp).background(SoftBeige, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "مشاركة",
                                            tint = DeepPlumPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (invoice.customerInfo?.hasData == true) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "الزبون: ${invoice.customerInfo.name.ifBlank { "عميل المحل" }} ${if (invoice.customerInfo.phone.isNotBlank()) "(${invoice.customerInfo.phone})" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SoftBeige)

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                invoice.items.forEach { detail ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• ${detail.productName} (${detail.colorName} - ${detail.sizeLabel}) × ${detail.quantity}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = formatIqd(detail.totalPriceIqd),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
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
}

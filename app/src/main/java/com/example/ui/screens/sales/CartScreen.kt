package com.example.ui.screens.sales

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CartItem
import com.example.data.model.CustomerInfo
import com.example.data.model.SaleInvoice
import com.example.ui.components.AppEmptyState
import com.example.ui.components.formatIqd
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepPlumPrimary
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldAccentIqd
import com.example.ui.theme.MutedBlushRose
import com.example.ui.theme.SoftBeige
import com.example.ui.theme.SoftRoseLight
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VelvetRoseError
import com.example.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    storeViewModel: StoreViewModel,
    onNavigateBackToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by storeViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var showInvoiceDialog by remember { mutableStateOf(false) }
    var currentInvoice by remember { mutableStateOf<SaleInvoice?>(null) }
    var checkoutError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("cart_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "سلة المبيعات والفاتورة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackToCatalog) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع للكتالوج")
                    }
                },
                actions = {
                    if (uiState.cartItems.isNotEmpty()) {
                        TextButton(onClick = { storeViewModel.clearCart() }) {
                            Text("تفريغ السلة", color = VelvetRoseError, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamSurface)
            )
        }
    ) { innerPadding ->
        if (uiState.cartItems.isEmpty()) {
            AppEmptyState(
                icon = Icons.Default.ShoppingBag,
                title = "سلة المبيعات فارغة",
                description = "اختاري الملابس من الكتالوج لإضافتها إلى هذه الفاتورة",
                actionLabel = "الرجوع لكتالوج الملابس",
                onActionClick = onNavigateBackToCatalog,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            val totalBillAmount = uiState.cartItems.sumOf { it.totalItemPriceIqd }
            val totalItemsCount = uiState.cartItems.sumOf { it.quantity }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("cart_lazy_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // قائمة عناصر السلة
                items(uiState.cartItems, key = { it.cartItemId }) { cartItem ->
                    CartItemCard(
                        item = cartItem,
                        onUpdateQty = { newQty -> storeViewModel.updateCartItemQuantity(cartItem.cartItemId, newQty) },
                        onUpdatePrice = { newPrice -> storeViewModel.updateCartItemPrice(cartItem.cartItemId, newPrice) },
                        onRemove = { storeViewModel.removeCartItem(cartItem.cartItemId) }
                    )
                }

                // قسم بيانات الزبون (اختياري تماماً)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("customer_info_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PersonOutline,
                                    contentDescription = null,
                                    tint = DeepPlumPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "بيانات الزبون (اختياري)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepPlumPrimary
                                )
                            }
                            Text(
                                text = "يمكن إتمام عملية البيع دون إدخال أي معلومة",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )

                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("اسم الزبون (اختياري)") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("customer_name_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customerPhone,
                                    onValueChange = { customerPhone = it },
                                    label = { Text("رقم الهاتف (اختياري)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("customer_phone_input")
                                )

                                OutlinedTextField(
                                    value = customerAddress,
                                    onValueChange = { customerAddress = it },
                                    label = { Text("العنوان / المدينة") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // بطاقة إجمالي الفاتورة
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftBeige)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("إجمالي عدد القطع في الفاتورة:", style = MaterialTheme.typography.bodyMedium)
                                Text("$totalItemsCount قطعة", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "صافي المبلغ الإجمالي:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DeepPlumPrimary
                                )
                                Text(
                                    text = formatIqd(totalBillAmount),
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = GoldAccentIqd
                                )
                            }
                        }
                    }
                }

                if (checkoutError != null) {
                    item {
                        Text(
                            text = checkoutError ?: "",
                            color = VelvetRoseError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                // زر تأكيد الفاتورة وإتمام البيع
                item {
                    Button(
                        onClick = {
                            checkoutError = null
                            val cust = CustomerInfo(
                                name = customerName.trim(),
                                phone = customerPhone.trim(),
                                address = customerAddress.trim()
                            )
                            val result = storeViewModel.completeSaleTransaction(cust)
                            result.onSuccess { invoice ->
                                currentInvoice = invoice
                                showInvoiceDialog = true
                            }.onFailure { err ->
                                checkoutError = err.localizedMessage ?: "فشلت عملية البيع"
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("confirm_sale_button")
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تأكيد البيع وحفظ الفاتورة (${formatIqd(totalBillAmount)})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // حوار عرض الفاتورة الصادرة وإمكانية المشاركة
    if (showInvoiceDialog && currentInvoice != null) {
        val invoice = currentInvoice!!
        InvoiceDialog(
            invoice = invoice,
            onShare = { shareInvoiceText(context, invoice) },
            onDone = {
                showInvoiceDialog = false
                currentInvoice = null
                onNavigateBackToCatalog()
            }
        )
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onUpdateQty: (Long) -> Unit,
    onUpdatePrice: (Long) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.cartItemId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftBeige),
                        contentAlignment = Alignment.Center
                    ) {
                        val img = item.colorImageUri ?: item.mainImageUri
                        if (img != null) {
                            AsyncImage(
                                model = img,
                                contentDescription = item.productName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Checkroom, contentDescription = null, tint = MutedBlushRose)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.productName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMain
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = SoftRoseLight) {
                                Text(
                                    text = item.colorName,
                                    fontSize = 11.sp,
                                    color = DeepPlumPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = SoftBeige) {
                                Text(
                                    text = "قياس ${item.sizeLabel}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepPlumPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف من السلة",
                        tint = VelvetRoseError
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSubtle.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // محدد الكمية
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onUpdateQty(item.quantity - 1) },
                        modifier = Modifier.size(32.dp).background(SoftBeige, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "تقليل", modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "${item.quantity}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    val canAdd = item.quantity < item.availableStock
                    IconButton(
                        onClick = { onUpdateQty(item.quantity + 1) },
                        enabled = canAdd,
                        modifier = Modifier.size(32.dp).background(if (canAdd) SoftRoseLight else SoftBeige, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(16.dp))
                    }
                }

                // سعر البيع وإجمالي البند
                Column(horizontalAlignment = Alignment.End) {
                    var priceEdit by remember(item.unitSellingPriceIqd) { mutableStateOf(item.unitSellingPriceIqd.toString()) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = priceEdit,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    priceEdit = input
                                    onUpdatePrice(input.toLongOrNull() ?: 0L)
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.width(95.dp).height(46.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("د.ع", fontSize = 11.sp, color = TextMuted)
                    }
                    Text(
                        text = "= ${formatIqd(item.totalItemPriceIqd)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldAccentIqd,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceDialog(
    invoice: SaleInvoice,
    onShare: () -> Unit,
    onDone: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDone,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("فاتورة بيع - متجر إبرة وخيط", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "رقم الفاتورة: ${invoice.invoiceNumber}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepPlumPrimary
                )
                Text(
                    text = "التاريخ والوقت: ${invoice.saleDateTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                if (invoice.customerInfo != null && invoice.customerInfo.hasData) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftBeige)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "بيانات الزبون:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (invoice.customerInfo.name.isNotBlank()) {
                                Text(text = "الاسم: ${invoice.customerInfo.name}", fontSize = 12.sp)
                            }
                            if (invoice.customerInfo.phone.isNotBlank()) {
                                Text(text = "الهاتف: ${invoice.customerInfo.phone}", fontSize = 12.sp)
                            }
                            if (invoice.customerInfo.address.isNotBlank()) {
                                Text(text = "العنوان: ${invoice.customerInfo.address}", fontSize = 12.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderSubtle)

                Text(text = "البنود المباعة:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                invoice.items.forEach { detail ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${detail.productName} (${detail.colorName} - ${detail.sizeLabel}) × ${detail.quantity}",
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatIqd(detail.totalPriceIqd),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = BorderSubtle)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "الإجمالي النهائي:", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Text(
                        text = formatIqd(invoice.totalAmountIqd),
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldAccentIqd,
                        fontSize = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("مشاركة الفاتورة")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDone, shape = RoundedCornerShape(10.dp)) {
                Text("تم والعودة")
            }
        }
    )
}

fun shareInvoiceText(context: Context, invoice: SaleInvoice) {
    val sb = StringBuilder()
    sb.appendLine("═══════════════════════")
    sb.appendLine("👗 متجر إبرة وخيط - للأزياء النسائية")
    sb.appendLine("فاتورة بيع رقم: ${invoice.invoiceNumber}")
    sb.appendLine("التاريخ: ${invoice.saleDateTime}")
    if (invoice.customerInfo?.hasData == true) {
        sb.appendLine("الزبون: ${invoice.customerInfo.name.ifBlank { "زبون المتجر" }}")
        if (invoice.customerInfo.phone.isNotBlank()) sb.appendLine("الهاتف: ${invoice.customerInfo.phone}")
        if (invoice.customerInfo.address.isNotBlank()) sb.appendLine("العنوان: ${invoice.customerInfo.address}")
    }
    sb.appendLine("───────────────────────")
    sb.appendLine("القطع المباعة:")
    for (item in invoice.items) {
        sb.appendLine("• ${item.productName} [${item.colorName} - قياس ${item.sizeLabel}]")
        sb.appendLine("  الكمية: ${item.quantity} × ${formatIqd(item.unitSellingPriceIqd)} = ${formatIqd(item.totalPriceIqd)}")
    }
    sb.appendLine("───────────────────────")
    sb.appendLine("💰 إجمالي الفاتورة: ${formatIqd(invoice.totalAmountIqd)}")
    sb.appendLine("شكراً لزيارتكم متجر إبرة وخيط! ✨")
    sb.appendLine("═══════════════════════")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "فاتورة مبيعات - إبرة وخيط")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة الفاتورة عبر"))
}

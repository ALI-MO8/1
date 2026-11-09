package com.example.ui.screens.purchases

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppEmptyState
import com.example.ui.components.formatIqd
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepPlumPrimary
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldAccentIqd
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.MutedBlushRose
import com.example.ui.theme.SoftBeige
import com.example.ui.theme.SoftRoseLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VelvetRoseError
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by storeViewModel.uiState.collectAsState()
    var showAddBatchDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("purchases_screen"),
        floatingActionButton = {
            if (uiState.products.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showAddBatchDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("توريد دفعة جديدة (FIFO)", fontWeight = FontWeight.Bold) },
                    containerColor = DeepPlumPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_purchase_batch_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(IvoryBackground)
        ) {
            Text(
                text = "دفعات الشراء وتوريد البضاعة (FIFO)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = DeepPlumPrimary,
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.purchaseBatches.isEmpty()) {
                AppEmptyState(
                    icon = Icons.Default.ShoppingCart,
                    title = "لا توجد دفعات شراء مسجلة",
                    description = "تُنشأ الدفعة الأولى تلقائياً عند إضافة أي موديل بالرصيد الافتتاحي وتكلفة القطعة. ويمكنك إضافة دفعات شراء جديدة بتكاليف مستقلة.",
                    actionLabel = null,
                    onActionClick = null
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.purchaseBatches.reversed(), key = { it.batchId }) { batch ->
                        val productName = uiState.products.find { it.id == batch.productId }?.name ?: "موديل محذوف"
                        val colorName = uiState.products.find { it.id == batch.productId }?.colors?.find { it.colorId == batch.colorId }?.colorName ?: "لون"

                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("batch_item_${batch.batchId}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = productName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = DeepPlumPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = SoftRoseLight) {
                                                Text(
                                                    text = colorName,
                                                    fontSize = 11.sp,
                                                    color = DeepPlumPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Surface(shape = RoundedCornerShape(4.dp), color = SoftBeige) {
                                                Text(
                                                    text = "قياس ${batch.sizeLabel}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepPlumPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "تكلفة القطعة: ${formatIqd(batch.unitCostIqd)}",
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccentIqd,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "التاريخ: ${batch.purchaseDate}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SoftBeige)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الكمية المشتراة: ${batch.purchasedQty} قطعة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (batch.remainingQty > 0) ForestGreen.copy(alpha = 0.12f) else VelvetRoseError.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = if (batch.remainingQty > 0) "المتبقي للاستهلاك: ${batch.remainingQty} قطعة" else "تم استهلاكها بالكامل (FIFO)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (batch.remainingQty > 0) ForestGreen else VelvetRoseError,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

    // حوار توريد بضاعة جديدة بتكلفة مستقلة لموديل موجود
    if (showAddBatchDialog && uiState.products.isNotEmpty()) {
        var selectedProduct by remember { mutableStateOf(uiState.products.first()) }
        var selectedColor by remember(selectedProduct) { mutableStateOf(selectedProduct.colors.firstOrNull()) }
        val sizeOptions = remember(selectedColor) {
            val existing = selectedColor?.sizes?.map { it.sizeLabel } ?: emptyList()
            if (existing.isNotEmpty()) existing else listOf("S", "M", "L", "XL")
        }
        var selectedSizeLabel by remember(sizeOptions) { mutableStateOf(sizeOptions.firstOrNull() ?: "M") }
        var newQtyText by remember { mutableStateOf("10") }
        var newCostPriceText by remember(selectedProduct) { mutableStateOf(selectedProduct.defaultCostPriceIqd.toString()) }
        var purchaseDateText by remember {
            mutableStateOf(SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date()))
        }

        var productMenuExpanded by remember { mutableStateOf(false) }
        var colorMenuExpanded by remember { mutableStateOf(false) }
        var sizeMenuExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddBatchDialog = false },
            title = {
                Text("توريد دفعة بضاعة جديدة (FIFO)", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // اختيار الموديل
                    ExposedDropdownMenuBox(
                        expanded = productMenuExpanded,
                        onExpandedChange = { productMenuExpanded = !productMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProduct.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الموديل") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = productMenuExpanded,
                            onDismissRequest = { productMenuExpanded = false }
                        ) {
                            uiState.products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text(prod.name) },
                                    onClick = {
                                        selectedProduct = prod
                                        selectedColor = prod.colors.firstOrNull()
                                        productMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // اختيار اللون
                    if (selectedProduct.colors.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = colorMenuExpanded,
                            onExpandedChange = { colorMenuExpanded = !colorMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedColor?.colorName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("اللون") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = colorMenuExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = colorMenuExpanded,
                                onDismissRequest = { colorMenuExpanded = false }
                            ) {
                                selectedProduct.colors.forEach { col ->
                                    DropdownMenuItem(
                                        text = { Text(col.colorName) },
                                        onClick = {
                                            selectedColor = col
                                            colorMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // اختيار القياس
                    ExposedDropdownMenuBox(
                        expanded = sizeMenuExpanded,
                        onExpandedChange = { sizeMenuExpanded = !sizeMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSizeLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("القياس") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = sizeMenuExpanded,
                            onDismissRequest = { sizeMenuExpanded = false }
                        ) {
                            sizeOptions.forEach { sz ->
                                DropdownMenuItem(
                                    text = { Text(sz) },
                                    onClick = {
                                        selectedSizeLabel = sz
                                        sizeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // الكمية الجديدة
                    OutlinedTextField(
                        value = newQtyText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) newQtyText = it },
                        label = { Text("الكمية المشتراة (قطع)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // تكلفة القطعة في هذه الدفعة (د.ع)
                    OutlinedTextField(
                        value = newCostPriceText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) newCostPriceText = it },
                        label = { Text("تكلفة القطعة في هذه الدفعة (د.ع)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // تاريخ الشراء
                    OutlinedTextField(
                        value = purchaseDateText,
                        onValueChange = { purchaseDateText = it },
                        label = { Text("تاريخ الشراء") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "ملاحظة: تكلفة الدفعة الجديدة ستُحفظ بشكل مستقل تماماً ولن تؤثر على تكاليف الدفعات القديمة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestGreen
                    )
                }
            },
            confirmButton = {
                val qty = newQtyText.toLongOrNull() ?: 0L
                val cost = newCostPriceText.toLongOrNull() ?: 0L
                val colorId = selectedColor?.colorId ?: ""

                Button(
                    onClick = {
                        if (qty > 0 && cost > 0 && colorId.isNotBlank()) {
                            storeViewModel.addPurchaseBatch(
                                productId = selectedProduct.id,
                                colorId = colorId,
                                sizeLabel = selectedSizeLabel,
                                quantity = qty,
                                unitCostIqd = cost,
                                purchaseDate = purchaseDateText.ifBlank { "اليوم" }
                            )
                            showAddBatchDialog = false
                        }
                    },
                    enabled = qty > 0 && cost > 0 && colorId.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary)
                ) {
                    Text("حفظ الدفعة وتحديث المخزون")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBatchDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

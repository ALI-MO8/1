package com.example.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.ui.viewmodel.StoreViewModel

/**
 * قسم المنتجات: مخصص بالكامل لإدارة وعرض موديلات الملابس النسائية
 * - عرض الموديلات
 * - زر إضافة موديل جديد (هنا فقط)
 * - تعديل بيانات الموديل (الاسم، التكلفة، سعر البيع)
 * - حذف الموديل
 * - إدارة صور الموديل والألوان والقياسات
 */
@Composable
fun ProductsScreen(
    storeViewModel: StoreViewModel,
    onOpenAddProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by storeViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // حالة حوار تعديل الموديل
    var editingProduct by remember { mutableStateOf<ProductModel?>(null) }
    // حالة حوار تأكيد الحذف
    var productToDelete by remember { mutableStateOf<ProductModel?>(null) }

    val filteredProducts = remember(uiState.products, searchQuery) {
        if (searchQuery.isBlank()) uiState.products
        else uiState.products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("products_management_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(IvoryBackground)
        ) {
            // شريط العنوان والبحث والزر الحصري لإضافة موديل جديد
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إدارة موديلات الملابس",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DeepPlumPrimary
                        )
                        Text(
                            text = "إجمالي الموديلات: ${uiState.products.size} موديل",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    // زر إضافة موديل جديد - داخل قسم المنتجات فقط
                    Button(
                        onClick = onOpenAddProduct,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("products_screen_add_model_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ إضافة موديل جديد", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // حقل البحث باسم الموديل
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحثي باسم الموديل في المحل...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
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
                    modifier = Modifier.fillMaxWidth().testTag("products_search_field")
                )
            }

            if (uiState.products.isEmpty()) {
                AppEmptyState(
                    icon = Icons.Default.Checkroom,
                    title = "لا توجد موديلات مضافة بالمحل",
                    description = "اضغطي على زر 'إضافة موديل جديد' بالأعلى لتعريف موديل جديد وتحديد ألوانه وقياساته",
                    actionLabel = "+ إضافة أول موديل نسائي",
                    onActionClick = onOpenAddProduct
                )
            } else if (filteredProducts.isEmpty()) {
                AppEmptyState(
                    icon = Icons.Default.Search,
                    title = "لا توجد نتائج بحث مطابقة",
                    description = "جرّبي البحث باسم موديل آخر أو مسح البحث",
                    actionLabel = "مسح البحث",
                    onActionClick = { searchQuery = "" }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("products_list_column"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductManagementCard(
                            product = product,
                            onEdit = { editingProduct = product },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    // شاشة تعديل بيانات وتفاصيل الموديل الشاملة
    editingProduct?.let { product ->
        EditProductScreen(
            product = product,
            onDismiss = { editingProduct = null },
            onSaveUpdatedProduct = { updatedModel, movements ->
                storeViewModel.updateProductFull(updatedModel, movements)
                editingProduct = null
            }
        )
    }

    // حوار تأكيد حذف الموديل
    productToDelete?.let { product ->
        val hasStock = product.totalPieces > 0
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Text("حذف الموديل", fontWeight = FontWeight.Bold, color = VelvetRoseError)
            },
            text = {
                Column {
                    Text("هل أنتِ متأكدة من حذف الموديل '${product.name}'؟")
                    if (hasStock) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تنبيه: يحتوي هذا الموديل على ${product.totalPieces} قطعة في المخزن حالياً.",
                            fontWeight = FontWeight.Bold,
                            color = VelvetRoseError,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        storeViewModel.deleteProduct(product.id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VelvetRoseError)
                ) {
                    Text("تأكيد الحذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

/**
 * بطاقة إدارة الموديل في قسم المنتجات
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductManagementCard(
    product: ProductModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_mgmt_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // صورة الموديل
                Box(
                    modifier = Modifier
                        .size(90.dp, 120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftBeige)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
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
                            tint = MutedBlushRose,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // بيانات الموديل والتسعير والمخزون
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepPlumPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("سعر البيع المقترح:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = formatIqd(product.defaultSellingPriceIqd),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = GoldAccentIqd
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("تكلفة القطعة:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = formatIqd(product.defaultCostPriceIqd),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextMain
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // إجمالي القطع في المخزن
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (product.totalPieces > 0) ForestGreen.copy(alpha = 0.12f) else VelvetRoseError.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (product.totalPieces > 0) "إجمالي المخزن: ${product.totalPieces} قطعة" else "المخزون نافد",
                            fontWeight = FontWeight.Bold,
                            color = if (product.totalPieces > 0) ForestGreen else VelvetRoseError,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // استعراض الألوان والقياسات المتوفرة للموديل
            Text(
                text = "الألوان والقياسات المتوفرة (${product.colors.size} ألوان):",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DeepPlumPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                product.colors.forEach { color ->
                    val colorStock = color.totalColorStock
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SoftRoseLight
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MutedBlushRose, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${color.colorName} ($colorStock قطعة)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepPlumPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // أزرار إدارة الموديل: تعديل وحذف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("edit_product_${product.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تعديل الموديل", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_product_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف الموديل",
                        tint = VelvetRoseError
                    )
                }
            }
        }
    }
}

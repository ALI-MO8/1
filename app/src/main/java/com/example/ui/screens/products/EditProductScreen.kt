package com.example.ui.screens.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.local.entity.StockMovementEntity
import com.example.data.model.ProductColorVariant
import com.example.data.model.ProductModel
import com.example.data.model.SizeStock
import com.example.data.storage.ImageStorageManager
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepPlumPrimary
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedBlushRose
import com.example.ui.theme.SoftBeige
import com.example.ui.theme.SoftRoseLight
import com.example.ui.theme.VelvetRoseError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * مسودة قابلة للتعديل لكل لون في الموديل
 */
class EditableColorDraft(
    val colorId: String = UUID.randomUUID().toString(),
    initialName: String = "",
    initialImageUri: String? = null,
    initialSizes: List<SizeStock> = emptyList()
) {
    var name by mutableStateOf(initialName)
    var imageUri by mutableStateOf(initialImageUri)
    val sizeMap = mutableStateMapOf<String, String>().apply {
        initialSizes.forEach { put(it.sizeLabel, it.currentStock.toString()) }
    }
    // لحساب فارق تعديل المخزون (الرصيد الأصلي قبل التعديل)
    val originalStockMap: Map<String, Long> = initialSizes.associate { it.sizeLabel to it.currentStock }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    product: ProductModel,
    onDismiss: () -> Unit,
    onSaveUpdatedProduct: (ProductModel, List<StockMovementEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // البيانات الأساسية
    var modelName by remember { mutableStateOf(product.name) }
    var mainImageUri by remember { mutableStateOf(product.mainImageUri) }
    var costPriceText by remember { mutableStateOf(product.defaultCostPriceIqd.toString()) }
    var sellingPriceText by remember { mutableStateOf(product.defaultSellingPriceIqd.toString()) }

    // مسودات الألوان
    val editableColors = remember {
        mutableStateListOf<EditableColorDraft>().apply {
            product.colors.forEach { color ->
                add(
                    EditableColorDraft(
                        colorId = color.colorId,
                        initialName = color.colorName,
                        initialImageUri = color.colorImageUri,
                        initialSizes = color.sizes
                    )
                )
            }
            if (isEmpty()) add(EditableColorDraft())
        }
    }

    val mainImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = ImageStorageManager.saveProductImage(context, uri)
            mainImageUri = localPath ?: uri.toString()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("edit_product_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تعديل الموديل: ${product.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("edit_product_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // تنبيه الحماية المحاسبية
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftRoseLight.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MutedBlushRose.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = DeepPlumPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حماية السجلات المالية: تعديل التكلفة وسعر البيع يطبق على العمليات الجديدة فقط ولا يغير الفواتير أو دفعات الشراء السابقة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepPlumPrimary
                        )
                    }
                }
            }

            // ==========================================
            // بطاقة المعلومات الأساسية
            // ==========================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "المعلومات الأساسية للموديل",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepPlumPrimary
                        )

                        // تغيير صورة الموديل
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp, 110.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SoftBeige)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .clickable {
                                        mainImagePicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (mainImageUri != null) {
                                    AsyncImage(
                                        model = mainImageUri,
                                        contentDescription = "صورة الموديل",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MutedBlushRose)
                                }
                            }

                            Column {
                                Text("صورة الموديل الرئيسية", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("اضغطي لتغيير صورة الموديل وحفظها دائماً", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // اسم الموديل
                        OutlinedTextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            label = { Text("اسم الموديل *") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // تكلفة القطعة الافتراضية
                        OutlinedTextField(
                            value = costPriceText,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) costPriceText = it },
                            label = { Text("تكلفة القطعة الافتراضية (د.ع) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // سعر البيع المقترح
                        OutlinedTextField(
                            value = sellingPriceText,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) sellingPriceText = it },
                            label = { Text("سعر البيع المقترح للمحل (د.ع) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ==========================================
            // رأس قسم الألوان والقياسات
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الألوان والقياسات المستقلة للموديل",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepPlumPrimary
                    )

                    Button(
                        onClick = { editableColors.add(EditableColorDraft()) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MutedBlushRose),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ إضافة لون جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ==========================================
            // بطاقات الألوان القابلة للتعديل
            // ==========================================
            itemsIndexed(editableColors, key = { _, item -> item.colorId }) { index, colorDraft ->
                EditableColorCard(
                    index = index + 1,
                    colorDraft = colorDraft,
                    canDelete = editableColors.size > 1,
                    onDelete = { editableColors.removeAt(index) }
                )
            }

            // زر إضافي لإضافة لون في نهاية القائمة
            item {
                OutlinedButton(
                    onClick = { editableColors.add(EditableColorDraft()) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ إضافة لون آخر للموديل", fontWeight = FontWeight.Bold)
                }
            }

            // ==========================================
            // أزرار الحفظ النهائي للتعديلات
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("إلغاء")
                    }

                    val canSave = modelName.isNotBlank() &&
                        costPriceText.isNotBlank() &&
                        sellingPriceText.isNotBlank() &&
                        editableColors.all { it.name.isNotBlank() }

                    Button(
                        onClick = {
                            val cost = costPriceText.toLongOrNull() ?: product.defaultCostPriceIqd
                            val sell = sellingPriceText.toLongOrNull() ?: product.defaultSellingPriceIqd
                            val dateTimeFormatted = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()).format(Date())

                            val stockMovements = mutableListOf<StockMovementEntity>()

                            val updatedColors = editableColors.map { draft ->
                                val sizes = draft.sizeMap.mapNotNull { (sz, qtyStr) ->
                                    val qty = qtyStr.toLongOrNull() ?: 0L
                                    val originalQty = draft.originalStockMap[sz] ?: 0L
                                    val delta = qty - originalQty

                                    // إذا تم تعديل الكمية، يتم تسجيل حركة تعديل جردي مستقلة
                                    if (delta != 0L) {
                                        stockMovements.add(
                                            StockMovementEntity(
                                                productId = product.id,
                                                colorId = draft.colorId,
                                                sizeLabel = sz,
                                                movementType = "ADJUSTMENT",
                                                quantityDelta = delta,
                                                reason = "تعديل جردي للمقاس في الموديل (${if (delta > 0) "+$delta" else "$delta"})",
                                                dateTimeFormatted = dateTimeFormatted
                                            )
                                        )
                                    }

                                    SizeStock(sizeLabel = sz, currentStock = qty)
                                }

                                ProductColorVariant(
                                    colorId = draft.colorId,
                                    colorName = draft.name.trim(),
                                    colorImageUri = draft.imageUri,
                                    sizes = sizes
                                )
                            }

                            val updatedModel = product.copy(
                                name = modelName.trim(),
                                mainImageUri = mainImageUri,
                                defaultCostPriceIqd = cost,
                                defaultSellingPriceIqd = sell,
                                colors = updatedColors
                            )

                            onSaveUpdatedProduct(updatedModel, stockMovements)
                            onDismiss()
                        },
                        enabled = canSave,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary),
                        modifier = Modifier.weight(2f).height(50.dp).testTag("save_model_edits_button")
                    ) {
                        Text("حفظ التعديلات وتحديث المخزون", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

/**
 * بطاقة اللون القابلة للتعديل
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditableColorCard(
    index: Int,
    colorDraft: EditableColorDraft,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var customDropdownExpanded by remember { mutableStateOf(false) }

    val letterSizes = listOf("XS", "S", "M", "L", "XL", "XXL", "XXXL")
    val numericSizes = listOf("36", "38", "40", "42", "44", "46", "48", "50", "Free Size")

    val colorImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = ImageStorageManager.saveColorImage(context, uri)
            colorDraft.imageUri = localPath ?: uri.toString()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // شريط رأس اللون
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MutedBlushRose.copy(alpha = 0.2f), modifier = Modifier.size(28.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("$index", fontWeight = FontWeight.Bold, color = DeepPlumPrimary, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (colorDraft.name.isNotBlank()) "اللون: ${colorDraft.name}" else "لون $index",
                        fontWeight = FontWeight.Bold,
                        color = DeepPlumPrimary
                    )
                }

                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف اللون", tint = VelvetRoseError)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // اسم اللون وصورته
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SoftBeige)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .clickable {
                            colorImagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (colorDraft.imageUri != null) {
                        AsyncImage(
                            model = colorDraft.imageUri,
                            contentDescription = "صورة اللون",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MutedBlushRose)
                    }
                }

                OutlinedTextField(
                    value = colorDraft.name,
                    onValueChange = { colorDraft.name = it },
                    label = { Text("اسم اللون *") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // قياسات هذا اللون
            Text("القياسات المتوفرة للون ${colorDraft.name}:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepPlumPrimary)
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                letterSizes.forEach { sz ->
                    val isChecked = colorDraft.sizeMap.containsKey(sz)
                    FilterChip(
                        selected = isChecked,
                        onClick = {
                            if (isChecked) colorDraft.sizeMap.remove(sz)
                            else colorDraft.sizeMap[sz] = "0"
                        },
                        label = { Text(sz, fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                Box {
                    FilterChip(
                        selected = false,
                        onClick = { customDropdownExpanded = true },
                        label = { Text("+ قياسات رقمية") }
                    )
                    DropdownMenu(
                        expanded = customDropdownExpanded,
                        onDismissRequest = { customDropdownExpanded = false }
                    ) {
                        numericSizes.forEach { num ->
                            DropdownMenuItem(
                                text = { Text("قياس $num") },
                                onClick = {
                                    if (!colorDraft.sizeMap.containsKey(num)) {
                                        colorDraft.sizeMap[num] = "0"
                                    }
                                    customDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // تعديل كمية المخزون لكل قياس
            colorDraft.sizeMap.keys.sorted().forEach { sz ->
                val currentQty = colorDraft.sizeMap[sz] ?: "0"
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = SoftBeige, modifier = Modifier.width(60.dp).height(38.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(sz, fontWeight = FontWeight.Bold, color = DeepPlumPrimary)
                        }
                    }

                    OutlinedTextField(
                        value = currentQty,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                colorDraft.sizeMap[sz] = input
                            }
                        },
                        label = { Text("الكمية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.width(130.dp)
                    )

                    IconButton(onClick = { colorDraft.sizeMap.remove(sz) }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف القياس", tint = VelvetRoseError.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

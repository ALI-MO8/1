package com.example.ui.screens.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ProductColorVariant
import com.example.data.model.ProductModel
import com.example.data.model.SizeStock
import com.example.ui.components.formatIqd
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepPlumPrimary
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldAccentIqd
import com.example.ui.theme.MutedBlushRose
import com.example.ui.theme.SoftBeige
import com.example.ui.theme.VelvetRoseError
import java.util.UUID

/**
 * كائن مسودة اللون - تفاعلي بالكامل مع محرك Jetpack Compose
 */
class ColorDraft(
    val id: String = UUID.randomUUID().toString(),
    initialName: String = "",
    initialImageUri: Uri? = null,
    initialSizes: Map<String, String> = emptyMap()
) {
    var name by mutableStateOf(initialName)
    var imageUri by mutableStateOf(initialImageUri)
    val sizeMap = mutableStateMapOf<String, String>().apply { putAll(initialSizes) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onDismiss: () -> Unit,
    onSaveProduct: (ProductModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }

    // الخطوة الأولى: معلومات الموديل الأساسية
    var modelName by remember { mutableStateOf("") }
    var mainImageUri by remember { mutableStateOf<Uri?>(null) }
    var costPriceText by remember { mutableStateOf("") }
    var defaultSellingPriceText by remember { mutableStateOf("") }

    // الخطوة الثانية: قائمة مسودات الألوان التفاعلية (تبدأ بلون أول فارغ بدون بيانات افتراضية وهمية)
    val colorsList = remember {
        mutableStateListOf(ColorDraft())
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val mainPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = com.example.data.storage.ImageStorageManager.saveProductImage(context, uri)
            mainImageUri = if (savedPath != null) Uri.parse(savedPath) else uri
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("add_product_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (step == 1) "إضافة موديل - خطوة 1 من 2" else "الألوان والقياسات - خطوة 2 من 2",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step == 2) step = 1 else onDismiss()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (step == 1) {
            // ====================================================
            // الخطوة الأولى: معلومات الموديل
            // ====================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "بيانات الموديل الأساسية",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = DeepPlumPrimary
                )
                Text(
                    text = "أدخلي اسم الموديل والصورة وتكلفة القطعة وسعر البيع المقترح للمحل",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // صورة الموديل الرئيسية
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftBeige)
                        .border(1.5.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .clickable {
                            mainPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("pick_main_image_box"),
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MutedBlushRose,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اضغطي لاختيار صورة الموديل الرئيسية",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = DeepPlumPrimary
                            )
                        }
                    }
                }

                // اسم الموديل
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("اسم الموديل *") },
                    placeholder = { Text("مثال: فستان سهرة شيفون ملكي") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_model_name_input")
                )

                // سعر تكلفة القطعة (د.ع)
                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) costPriceText = it },
                    label = { Text("سعر تكلفة القطعة (د.ع) *") },
                    placeholder = { Text("12000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_cost_price_input")
                )

                // سعر البيع الافتراضي (د.ع)
                OutlinedTextField(
                    value = defaultSellingPriceText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) defaultSellingPriceText = it },
                    label = { Text("سعر البيع الافتراضي (د.ع) *") },
                    placeholder = { Text("20000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_selling_price_input")
                )

                Text(
                    text = "* سعر البيع الافتراضي يمكن تغييره أثناء تسجيل أي فاتورة بيع دون تعديل السعر الأساسي.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                val isStep1Valid = modelName.isNotBlank() &&
                    costPriceText.isNotBlank() &&
                    defaultSellingPriceText.isNotBlank()

                Button(
                    onClick = { step = 2 },
                    enabled = isStep1Valid,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_to_step2_button")
                ) {
                    Text("التالي: الألوان والقياسات", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // ====================================================
            // الخطوة الثانية: الألوان والقياسات المستقلة تماماً (LazyColumn انسيابية)
            // ====================================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("step2_lazy_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // رأس الصفحة مع زر إضافة لون بارز ودائم
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تحديد ألوان وقياسات: $modelName",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepPlumPrimary
                            )
                            Text(
                                text = "كل لون يحتوي على قياسات وكميات مستقلة دون نسخ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // زر إضافة لون علوي دائم
                        Button(
                            onClick = {
                                colorsList.add(ColorDraft())
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MutedBlushRose),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("add_color_button_top")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ إضافة لون", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // أقسام الألوان المستقلة
                itemsIndexed(colorsList, key = { _, item -> item.id }) { index, colorDraft ->
                    ColorSectionCard(
                        index = index + 1,
                        colorDraft = colorDraft,
                        canDelete = colorsList.size > 1,
                        onDelete = { colorsList.removeAt(index) }
                    )
                }

                // زر بارز لإضافة لون آخر أسفل قائمة الألوان
                item {
                    OutlinedButton(
                        onClick = {
                            colorsList.add(ColorDraft())
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("add_color_button_bottom")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = DeepPlumPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+ إضافة لون إضافي (لون ${colorsList.size + 1})",
                            fontWeight = FontWeight.Bold,
                            color = DeepPlumPrimary,
                            fontSize = 15.sp
                        )
                    }
                }

                // بطاقة ملخص الموديل
                item {
                    val totalColorsCount = colorsList.size
                    val totalPiecesSum = colorsList.sumOf { color ->
                        color.sizeMap.values.sumOf { it.toLongOrNull() ?: 0L }
                    }
                    val totalSizesVariants = colorsList.sumOf { it.sizeMap.size }
                    val unitCost = costPriceText.toLongOrNull() ?: 0L

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("model_summary_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftBeige)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "ملخص الموديل ودفعة المخزون الأولى:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepPlumPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("إجمالي الألوان المضافة:", style = MaterialTheme.typography.bodyMedium)
                                Text("$totalColorsCount ألوان", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("إجمالي القياسات المتنوعة:", style = MaterialTheme.typography.bodyMedium)
                                Text("$totalSizesVariants قياس", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("إجمالي القطع في المستودع:", style = MaterialTheme.typography.bodyMedium)
                                Text("$totalPiecesSum قطعة", fontWeight = FontWeight.ExtraBold, color = ForestGreen)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("تكلفة الدفعة الأولى المخزنية:", style = MaterialTheme.typography.bodyMedium)
                                Text(formatIqd(totalPiecesSum * unitCost), fontWeight = FontWeight.Bold, color = GoldAccentIqd)
                            }
                        }
                    }
                }

                // أزرار الحفظ النهائي والرجوع
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Text("رجوع للخطوة 1")
                        }

                        val canSave = colorsList.all { it.name.isNotBlank() } &&
                            colorsList.any { it.sizeMap.isNotEmpty() } &&
                            colorsList.any { it.sizeMap.values.any { q -> (q.toLongOrNull() ?: 0L) > 0 } }

                        Button(
                            onClick = {
                                val cost = costPriceText.toLongOrNull() ?: 0L
                                val sell = defaultSellingPriceText.toLongOrNull() ?: 0L

                                val convertedColors = colorsList.map { draft ->
                                    val sizes = draft.sizeMap.mapNotNull { (sz, qtyStr) ->
                                        val qty = qtyStr.toLongOrNull() ?: 0L
                                        if (qty >= 0) SizeStock(sizeLabel = sz, currentStock = qty) else null
                                    }
                                    ProductColorVariant(
                                        colorId = draft.id,
                                        colorName = draft.name.trim(),
                                        colorImageUri = draft.imageUri?.toString(),
                                        sizes = sizes
                                    )
                                }

                                val newProduct = ProductModel(
                                    name = modelName.trim(),
                                    mainImageUri = mainImageUri?.toString(),
                                    defaultCostPriceIqd = cost,
                                    defaultSellingPriceIqd = sell,
                                    colors = convertedColors
                                )

                                onSaveProduct(newProduct)
                                onDismiss()
                            },
                            enabled = canSave,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary),
                            modifier = Modifier.weight(2f).height(52.dp).testTag("save_model_final_button")
                        ) {
                            Text("حفظ الموديل في المخزون", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * بطاقة كل لون مستقلة بذاتها: اسم وصورة وقياسات وكميات
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorSectionCard(
    index: Int,
    colorDraft: ColorDraft,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    var customDropdownExpanded by remember { mutableStateOf(false) }
    var customSizeInput by remember { mutableStateOf("") }
    var showCustomSizeDialog by remember { mutableStateOf(false) }

    val defaultLetterSizes = listOf("XS", "S", "M", "L", "XL", "XXL", "XXXL")
    val standardNumericSizes = listOf("36", "38", "40", "42", "44", "46", "48", "50", "Free Size")

    val context = androidx.compose.ui.platform.LocalContext.current
    val colorPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = com.example.data.storage.ImageStorageManager.saveColorImage(context, uri)
            colorDraft.imageUri = if (savedPath != null) Uri.parse(savedPath) else uri
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("color_section_$index"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // شريط رأس اللون
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MutedBlushRose.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$index",
                                fontWeight = FontWeight.Bold,
                                color = DeepPlumPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (colorDraft.name.isNotBlank()) "اللون $index: ${colorDraft.name}" else "اللون $index",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepPlumPrimary
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_color_$index")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف اللون",
                            tint = VelvetRoseError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // اسم اللون وصورته الخاصة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // صورة خاصة بهذا اللون
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftBeige)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable {
                            colorPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("color_image_picker_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    if (colorDraft.imageUri != null) {
                        AsyncImage(
                            model = colorDraft.imageUri,
                            contentDescription = "صورة ${colorDraft.name}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "إضافة صورة للون",
                                tint = MutedBlushRose,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("صورة", fontSize = 10.sp, color = DeepPlumPrimary)
                        }
                    }
                }

                OutlinedTextField(
                    value = colorDraft.name,
                    onValueChange = { colorDraft.name = it },
                    label = { Text("اسم اللون *") },
                    placeholder = { Text("مثال: أسود، أزرق، أبيض...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("color_name_input_$index")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            // القياسات المتوفرة لهذا اللون (اختيار القياس يفتح حقله المستقل)
            Text(
                text = "القياسات المتوفرة للون (${if (colorDraft.name.isNotBlank()) colorDraft.name else index.toString()}):",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepPlumPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // شرائح القياسات الحرفية
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                defaultLetterSizes.forEach { sz ->
                    val isChecked = colorDraft.sizeMap.containsKey(sz)
                    FilterChip(
                        selected = isChecked,
                        onClick = {
                            if (isChecked) {
                                colorDraft.sizeMap.remove(sz)
                            } else {
                                colorDraft.sizeMap[sz] = "" // كمية فارغة يحددها المستخدم
                            }
                        },
                        label = { Text(sz, fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepPlumPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // زر القائمة المنسدلة للقياسات الرقمية والمخصصة
                Box {
                    FilterChip(
                        selected = false,
                        onClick = { customDropdownExpanded = true },
                        label = { Text("+ قياسات رقمية") },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )

                    DropdownMenu(
                        expanded = customDropdownExpanded,
                        onDismissRequest = { customDropdownExpanded = false }
                    ) {
                        standardNumericSizes.forEach { numSize ->
                            DropdownMenuItem(
                                text = { Text("قياس $numSize") },
                                onClick = {
                                    if (!colorDraft.sizeMap.containsKey(numSize)) {
                                        colorDraft.sizeMap[numSize] = ""
                                    }
                                    customDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // حقول إدخال الكمية المستقلة لكل قياس تم اختياره لهذا اللون
            if (colorDraft.sizeMap.isNotEmpty()) {
                Text(
                    text = "أدخلي الكمية المتوفرة لكل قياس في هذا اللون:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                colorDraft.sizeMap.keys.sorted().forEach { sz ->
                    val currentQty = colorDraft.sizeMap[sz] ?: ""
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftBeige,
                            modifier = Modifier.width(64.dp).height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = sz,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepPlumPrimary,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        OutlinedTextField(
                            value = currentQty,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    colorDraft.sizeMap[sz] = input
                                }
                            },
                            label = { Text("الكمية (قطع)") },
                            placeholder = { Text("مثال: 5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(140.dp).testTag("qty_${colorDraft.id}_$sz")
                        )

                        IconButton(
                            onClick = {
                                colorDraft.sizeMap.remove(sz)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف القياس",
                                tint = VelvetRoseError.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

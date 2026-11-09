package com.example.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupRestoreManager
import com.example.data.backup.BackupSummary
import com.example.data.storage.ImageStorageManager
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepPlumPrimary
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.MutedBlushRose
import com.example.ui.theme.SoftBeige
import com.example.ui.theme.SoftRoseLight
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VelvetRoseError
import com.example.ui.viewmodel.StoreViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    storeViewModel: StoreViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by storeViewModel.uiState.collectAsState()

    var isProcessing by remember { mutableStateOf(false) }
    var operationMessage by remember { mutableStateOf<String?>(null) }
    var resultDialogSummary by remember { mutableStateOf<BackupSummary?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    // لاونشر تصدير النسخة الاحتياطية (SAF CreateDocument)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            operationMessage = "جاري إنشاء وتصدير النسخة الاحتياطية الشاملة..."
            scope.launch {
                val result = BackupRestoreManager.exportBackupZip(context, uri)
                isProcessing = false
                result.onSuccess { summary ->
                    resultDialogSummary = summary
                }.onFailure { error ->
                    errorMessage = "فشل تصدير النسخة الاحتياطية: ${error.localizedMessage}"
                }
            }
        }
    }

    // لاونشر استيراد النسخة الاحتياطية (SAF OpenDocument)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الإعدادات والنسخ الاحتياطي",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .background(IvoryBackground)
                .testTag("settings_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // بطاقة حالة أمان البيانات
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = ForestGreen.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "قاعدة بيانات محلية دائمة (Room)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DeepPlumPrimary
                                )
                                Text(
                                    text = "البيانات والصور محفوظة في الهاتف وتستمر بعد إغلاق التطبيق",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = BorderSubtle.copy(alpha = 0.5f))

                        val lastBackup = uiState.backupMetadata?.lastBackupDate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("آخر نسخة احتياطية ناجحة:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = lastBackup ?: "لم يتم إنشاء نسخة بعد",
                                fontWeight = FontWeight.Bold,
                                color = if (lastBackup != null) ForestGreen else VelvetRoseError
                            )
                        }

                        if (lastBackup == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VelvetRoseError.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = VelvetRoseError, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تنبيه: يُنصح بإنشاء نسخة احتياطية دورية لحماية سجلاتك المالية.",
                                        fontSize = 11.sp,
                                        color = VelvetRoseError,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // بطاقة أزرار التصدير والاستيراد
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "النسخ الاحتياطي واستعادة البيانات (ملف ZIP)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepPlumPrimary
                        )

                        Text(
                            text = "يتيح لك ملف النسخة الاحتياطية نقل كامل بيانات المحل والصور والفواتير والمخزون إلى هاتف آخر أو استعادتها عند الحاجة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMain
                        )

                        // زر تصدير النسخة الاحتياطية
                        Button(
                            onClick = {
                                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                exportLauncher.launch("EbraWaKhait_Backup_$dateStr.zip")
                            },
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary),
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("export_backup_button")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تصدير نسخة احتياطية كاملة (ZIP)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // زر استيراد النسخة الاحتياطية
                        OutlinedButton(
                            onClick = {
                                importLauncher.launch(
                                    arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*")
                                )
                            },
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("import_backup_button")
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = DeepPlumPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استيراد واستعادة نسخة احتياطية (ZIP)", fontWeight = FontWeight.Bold, color = DeepPlumPrimary, fontSize = 14.sp)
                        }
                    }
                }
            }

            // إحصائيات البيانات الحالية المحفوظة
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftBeige)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ملخص السجلات المخزنة في الهاتف:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepPlumPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("عدد الموديلات المسجلة:", style = MaterialTheme.typography.bodySmall)
                            Text("${uiState.products.size} موديل", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("عدد فواتير المبيعات:", style = MaterialTheme.typography.bodySmall)
                            Text("${uiState.salesInvoices.size} فاتورة", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("دفعات الشراء (FIFO):", style = MaterialTheme.typography.bodySmall)
                            Text("${uiState.purchaseBatches.size} دفعة", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الصور المحفوظة محلياً:", style = MaterialTheme.typography.bodySmall)
                            val imagesCount = ImageStorageManager.countStoredImages(context)
                            Text("$imagesCount صورة", fontWeight = FontWeight.Bold, color = ForestGreen)
                        }
                    }
                }
            }

            // معلومات التطبيق والمحل
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("إبرة وخيط - أزياء نسائية فاخرة", fontWeight = FontWeight.Bold, color = DeepPlumPrimary)
                    Text("إصدار النظام 1.0 • محاسبة FIFO متوافقة مع الدينار العراقي", fontSize = 11.sp, color = TextMuted)
                }
            }
        }
    }

    // حوار التقدم أثناء التصدير أو الاستيراد
    if (isProcessing) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("جاري معالجة البيانات...", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = DeepPlumPrimary)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(operationMessage ?: "يرجى الانتظار...", fontSize = 13.sp)
                }
            },
            confirmButton = {}
        )
    }

    // حوار تأكيد الاستيراد المسبق لحماية البيانات الحالية
    pendingImportUri?.let { importUri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = {
                Text("تأكيد استعادة النسخة الاحتياطية", fontWeight = FontWeight.Bold, color = DeepPlumPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("هل أنتِ متأكدة من استعادة بيانات المحل من ملف الـ ZIP المختار؟")
                    Text(
                        text = "• سيتم فحص سلامة الملف ومطابقة البنية وتضمين الصور.\n• يتم إنشاء نقطة تراجع وقائية لحماية بياناتك في حال عدم اكتمال الملف.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uriToRestore = importUri
                        pendingImportUri = null
                        isProcessing = true
                        operationMessage = "جاري فحص ملف الـ ZIP واستعادة الجداول والصور..."
                        scope.launch {
                            val result = BackupRestoreManager.importBackupZip(context, uriToRestore)
                            isProcessing = false
                            result.onSuccess { summary ->
                                resultDialogSummary = summary
                            }.onFailure { error ->
                                errorMessage = error.localizedMessage ?: "حدث خطأ أثناء فحص واستعادة الملف"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary)
                ) {
                    Text("بدء الاستعادة الذرية")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // حوار نجاح التصدير أو الاستيراد
    resultDialogSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = { resultDialogSummary = null },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("تمت العملية بنجاح تام", fontWeight = FontWeight.Bold, color = ForestGreen)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("تاريخ النسخة: ${summary.creationDate}", fontSize = 12.sp)
                    Text("• الموديلات: ${summary.productsCount} موديل", fontSize = 12.sp)
                    Text("• فواتير المبيعات: ${summary.salesInvoicesCount} فاتورة", fontSize = 12.sp)
                    Text("• دفعات الشراء: ${summary.purchaseBatchesCount} دفعة", fontSize = 12.sp)
                    Text("• الصور المحفوظة: ${summary.imagesCount} صورة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                }
            },
            confirmButton = {
                Button(
                    onClick = { resultDialogSummary = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary)
                ) {
                    Text("حسناً")
                }
            }
        )
    }

    // حوار الخطأ
    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = VelvetRoseError, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("فشلت العملية", fontWeight = FontWeight.Bold, color = VelvetRoseError)
            },
            text = {
                Text(error, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = { errorMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary)
                ) {
                    Text("إغلاق")
                }
            }
        )
    }
}

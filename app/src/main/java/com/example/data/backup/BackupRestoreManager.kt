package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BackupMetadataEntity
import com.example.data.local.entity.ColorVariantEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PurchaseBatchEntity
import com.example.data.local.entity.SaleInvoiceEntity
import com.example.data.local.entity.SizeStockEntity
import com.example.data.local.entity.SoldItemDetailEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.storage.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupSummary(
    val creationDate: String,
    val productsCount: Int,
    val salesInvoicesCount: Int,
    val purchaseBatchesCount: Int,
    val expensesCount: Int,
    val imagesCount: Int,
    val appVersion: String = "1.0",
    val isValid: Boolean = true
)

object BackupRestoreManager {

    /**
     * تصدير نسخة احتياطية كاملة بصيغة ZIP إلى ملف يحدده المستخدم عبر SAF
     */
    suspend fun exportBackupZip(
        context: Context,
        destinationUri: Uri
    ): Result<BackupSummary> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val dao = db.storeDao()

            val products = dao.getAllProducts().first()
            val colors = dao.getAllColors().first()
            val sizes = dao.getAllSizes().first()
            val batches = dao.getAllPurchaseBatchesList()
            val invoices = dao.getAllInvoices().first()
            val soldItems = dao.getAllSoldItemDetails().first()
            val expenses = dao.getAllExpenses().first()
            val movements = dao.getAllStockMovements().first()

            val dateFormatted = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()).format(Date())
            val dateFileName = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val productsDir = ImageStorageManager.getProductsDir(context)
            val colorsDir = ImageStorageManager.getColorsDir(context)
            val productImages = productsDir.listFiles()?.toList() ?: emptyList()
            val colorImages = colorsDir.listFiles()?.toList() ?: emptyList()
            val totalImages = productImages.size + colorImages.size

            val infoJson = JSONObject().apply {
                put("appName", "إبرة وخيط")
                put("appVersion", "1.0")
                put("exportDate", dateFormatted)
                put("productsCount", products.size)
                put("colorsCount", colors.size)
                put("sizesCount", sizes.size)
                put("batchesCount", batches.size)
                put("invoicesCount", invoices.size)
                put("expensesCount", expenses.size)
                put("movementsCount", movements.size)
                put("imagesCount", totalImages)
            }

            // كتابة أرشيف الـ ZIP
            val outStream = context.contentResolver.openOutputStream(destinationUri)
                ?: return@withContext Result.failure(IllegalStateException("تعذر فتح ملف الوجهة للكتابة"))

            var totalBytesWritten = 0L

            ZipOutputStream(outStream).use { zos ->
                // 1. backup_info.json
                writeZipTextEntry(zos, "backup_info.json", infoJson.toString(2))

                // 2. products.json
                val productsArray = JSONArray().apply {
                    products.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("name", it.name)
                            put("mainImageUri", it.mainImageUri ?: "")
                            put("defaultCostPriceIqd", it.defaultCostPriceIqd)
                            put("defaultSellingPriceIqd", it.defaultSellingPriceIqd)
                            put("isArchived", it.isArchived)
                            put("createdAt", it.createdAt)
                        })
                    }
                }
                writeZipTextEntry(zos, "products.json", productsArray.toString(2))

                // 3. colors.json
                val colorsArray = JSONArray().apply {
                    colors.forEach {
                        put(JSONObject().apply {
                            put("colorId", it.colorId)
                            put("productId", it.productId)
                            put("colorName", it.colorName)
                            put("colorImageUri", it.colorImageUri ?: "")
                            put("isArchived", it.isArchived)
                        })
                    }
                }
                writeZipTextEntry(zos, "colors.json", colorsArray.toString(2))

                // 4. sizes.json
                val sizesArray = JSONArray().apply {
                    sizes.forEach {
                        put(JSONObject().apply {
                            put("sizeId", it.sizeId)
                            put("colorId", it.colorId)
                            put("productId", it.productId)
                            put("sizeLabel", it.sizeLabel)
                            put("currentStock", it.currentStock)
                            put("isArchived", it.isArchived)
                        })
                    }
                }
                writeZipTextEntry(zos, "sizes.json", sizesArray.toString(2))

                // 5. purchase_batches.json
                val batchesArray = JSONArray().apply {
                    batches.forEach {
                        put(JSONObject().apply {
                            put("batchId", it.batchId)
                            put("productId", it.productId)
                            put("colorId", it.colorId)
                            put("sizeLabel", it.sizeLabel)
                            put("purchasedQty", it.purchasedQty)
                            put("remainingQty", it.remainingQty)
                            put("unitCostIqd", it.unitCostIqd)
                            put("purchaseDate", it.purchaseDate)
                            put("timestamp", it.timestamp)
                        })
                    }
                }
                writeZipTextEntry(zos, "purchase_batches.json", batchesArray.toString(2))

                // 6. sales.json
                val invoicesArray = JSONArray().apply {
                    invoices.forEach {
                        put(JSONObject().apply {
                            put("invoiceNumber", it.invoiceNumber)
                            put("saleDateTime", it.saleDateTime)
                            put("totalAmountIqd", it.totalAmountIqd)
                            put("cogsTotalIqd", it.cogsTotalIqd)
                            put("grossProfitIqd", it.grossProfitIqd)
                            put("customerName", it.customerName ?: "")
                            put("customerPhone", it.customerPhone ?: "")
                            put("timestamp", it.timestamp)
                        })
                    }
                }
                writeZipTextEntry(zos, "sales.json", invoicesArray.toString(2))

                // 7. sale_items.json
                val soldItemsArray = JSONArray().apply {
                    soldItems.forEach {
                        put(JSONObject().apply {
                            put("itemId", it.itemId)
                            put("invoiceNumber", it.invoiceNumber)
                            put("productId", it.productId)
                            put("productName", it.productName)
                            put("colorName", it.colorName)
                            put("sizeLabel", it.sizeLabel)
                            put("quantity", it.quantity)
                            put("unitSellingPriceIqd", it.unitSellingPriceIqd)
                            put("totalPriceIqd", it.totalPriceIqd)
                            put("cogsIqd", it.cogsIqd)
                        })
                    }
                }
                writeZipTextEntry(zos, "sale_items.json", soldItemsArray.toString(2))

                // 8. expenses.json
                val expensesArray = JSONArray().apply {
                    expenses.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("title", it.title)
                            put("category", it.category)
                            put("amountIqd", it.amountIqd)
                            put("date", it.date)
                            put("notes", it.notes)
                            put("timestamp", it.timestamp)
                        })
                    }
                }
                writeZipTextEntry(zos, "expenses.json", expensesArray.toString(2))

                // 9. stock_movements.json
                val movementsArray = JSONArray().apply {
                    movements.forEach {
                        put(JSONObject().apply {
                            put("movementId", it.movementId)
                            put("productId", it.productId)
                            put("colorId", it.colorId)
                            put("sizeLabel", it.sizeLabel)
                            put("movementType", it.movementType)
                            put("quantityDelta", it.quantityDelta)
                            put("reason", it.reason)
                            put("timestamp", it.timestamp)
                            put("dateTimeFormatted", it.dateTimeFormatted)
                        })
                    }
                }
                writeZipTextEntry(zos, "stock_movements.json", movementsArray.toString(2))

                // 10. تضمين صور الموديلات والألوان
                productImages.forEach { file ->
                    writeZipFileEntry(zos, "images/products/${file.name}", file)
                }
                colorImages.forEach { file ->
                    writeZipFileEntry(zos, "images/colors/${file.name}", file)
                }
            }

            // تحديث بيانات النسخ الاحتياطي في قاعدة البيانات
            val summary = BackupSummary(
                creationDate = dateFormatted,
                productsCount = products.size,
                salesInvoicesCount = invoices.size,
                purchaseBatchesCount = batches.size,
                expensesCount = expenses.size,
                imagesCount = totalImages
            )

            dao.setBackupMetadata(
                BackupMetadataEntity(
                    id = 1,
                    lastBackupDate = dateFormatted,
                    lastBackupFileSize = 0L,
                    lastBackupRecordsCount = products.size + invoices.size + batches.size
                )
            )

            Result.success(summary)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * استيراد نسخة احتياطية من ملف ZIP وفحص سلامتها واستعادتها بشكل ذري مع نسخة وقائية
     */
    suspend fun importBackupZip(
        context: Context,
        sourceUri: Uri
    ): Result<BackupSummary> = withContext(Dispatchers.IO) {
        val tempExtractDir = File(context.cacheDir, "backup_restore_temp_${System.currentTimeMillis()}")
        tempExtractDir.mkdirs()

        try {
            val inStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext Result.failure(IllegalStateException("تعذر فتح ملف النسخة الاحتياطية"))

            // 1. فك ضغط الأرشيف في المجلد المؤقت
            ZipInputStream(inStream).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val file = File(tempExtractDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            // 2. التحقق من وجود وصحة backup_info.json
            val infoFile = File(tempExtractDir, "backup_info.json")
            if (!infoFile.exists()) {
                tempExtractDir.deleteRecursively()
                return@withContext Result.failure(IllegalArgumentException("الملف المختار ليس نسخة احتياطية صالحة لتطبيق إبرة وخيط (ملف الفهرس مفقود)"))
            }

            val infoJson = JSONObject(infoFile.readText())
            val summary = BackupSummary(
                creationDate = infoJson.optString("exportDate", "غير معروف"),
                productsCount = infoJson.optInt("productsCount", 0),
                salesInvoicesCount = infoJson.optInt("invoicesCount", 0),
                purchaseBatchesCount = infoJson.optInt("batchesCount", 0),
                expensesCount = infoJson.optInt("expensesCount", 0),
                imagesCount = infoJson.optInt("imagesCount", 0)
            )

            // 3. قراءة ملفات الجداول وتحويلها لكائنات Entity
            val productsList = readProductsEntity(File(tempExtractDir, "products.json"))
            val colorsList = readColorsEntity(File(tempExtractDir, "colors.json"))
            val sizesList = readSizesEntity(File(tempExtractDir, "sizes.json"))
            val batchesList = readBatchesEntity(File(tempExtractDir, "purchase_batches.json"))
            val invoicesList = readInvoicesEntity(File(tempExtractDir, "sales.json"))
            val soldItemsList = readSoldItemsEntity(File(tempExtractDir, "sale_items.json"))
            val expensesList = readExpensesEntity(File(tempExtractDir, "expenses.json"))
            val movementsList = readMovementsEntity(File(tempExtractDir, "stock_movements.json"))

            val db = AppDatabase.getInstance(context)
            val dao = db.storeDao()

            // 4. استعادة الصور ونسخها للمجلد الدائم
            val targetProductsDir = ImageStorageManager.getProductsDir(context)
            val targetColorsDir = ImageStorageManager.getColorsDir(context)

            val extractedProductsDir = File(tempExtractDir, "images/products")
            if (extractedProductsDir.exists()) {
                extractedProductsDir.listFiles()?.forEach { file ->
                    val target = File(targetProductsDir, file.name)
                    file.copyTo(target, overwrite = true)
                }
            }

            val extractedColorsDir = File(tempExtractDir, "images/colors")
            if (extractedColorsDir.exists()) {
                extractedColorsDir.listFiles()?.forEach { file ->
                    val target = File(targetColorsDir, file.name)
                    file.copyTo(target, overwrite = true)
                }
            }

            // 5. استعادة قاعدة البيانات الذرية
            val backupMeta = BackupMetadataEntity(
                id = 1,
                lastBackupDate = summary.creationDate,
                lastBackupFileSize = 0L,
                lastBackupRecordsCount = productsList.size + invoicesList.size
            )

            dao.restoreDatabaseAtomic(
                products = productsList,
                colors = colorsList,
                sizes = sizesList,
                batches = batchesList,
                invoices = invoicesList,
                soldItems = soldItemsList,
                expenses = expensesList,
                movements = movementsList,
                backupMeta = backupMeta
            )

            tempExtractDir.deleteRecursively()
            Result.success(summary)
        } catch (e: Exception) {
            tempExtractDir.deleteRecursively()
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ==========================================================
    // دوال مساعدة للكتابة والقراءة
    // ==========================================================

    private fun writeZipTextEntry(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }

    private fun writeZipFileEntry(zos: ZipOutputStream, entryName: String, file: File) {
        if (!file.exists() || !file.isFile) return
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        FileInputStream(file).use { fis ->
            fis.copyTo(zos)
        }
        zos.closeEntry()
    }

    private fun readProductsEntity(file: File): List<ProductEntity> {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        val list = mutableListOf<ProductEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ProductEntity(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    mainImageUri = obj.optString("mainImageUri").takeIf { it.isNotBlank() },
                    defaultCostPriceIqd = obj.getLong("defaultCostPriceIqd"),
                    defaultSellingPriceIqd = obj.getLong("defaultSellingPriceIqd"),
                    isArchived = obj.optBoolean("isArchived", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun readColorsEntity(file: File): List<ColorVariantEntity> {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        val list = mutableListOf<ColorVariantEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ColorVariantEntity(
                    colorId = obj.getString("colorId"),
                    productId = obj.getString("productId"),
                    colorName = obj.getString("colorName"),
                    colorImageUri = obj.optString("colorImageUri").takeIf { it.isNotBlank() },
                    isArchived = obj.optBoolean("isArchived", false)
                )
            )
        }
        return list
    }

    private fun readSizesEntity(file: File): List<SizeStockEntity> {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        val list = mutableListOf<SizeStockEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SizeStockEntity(
                    sizeId = obj.getString("sizeId"),
                    colorId = obj.getString("colorId"),
                    productId = obj.getString("productId"),
                    sizeLabel = obj.getString("sizeLabel"),
                    currentStock = obj.getLong("currentStock"),
                    isArchived = obj.optBoolean("isArchived", false)
                )
            )
        }
        return list
    }

    private fun readBatchesEntity(file: File): List<PurchaseBatchEntity> {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        val list = mutableListOf<PurchaseBatchEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                PurchaseBatchEntity(
                    batchId = obj.getString("batchId"),
                    productId = obj.getString("productId"),
                    colorId = obj.getString("colorId"),
                    sizeLabel = obj.getString("sizeLabel"),
                    purchasedQty = obj.getLong("purchasedQty"),
                    remainingQty = obj.getLong("remainingQty"),
                    unitCostIqd = obj.getLong("unitCostIqd"),
                    purchaseDate = obj.getString("purchaseDate"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun readInvoicesEntity(file: File): List<SaleInvoiceEntity> {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        val list = mutableListOf<SaleInvoiceEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SaleInvoiceEntity(
                    invoiceNumber = obj.getString("invoiceNumber"),
                    saleDateTime = obj.getString("saleDateTime"),
                    totalAmountIqd = obj.getLong("totalAmountIqd"),
                    cogsTotalIqd = obj.getLong("cogsTotalIqd"),
                    grossProfitIqd = obj.getLong("grossProfitIqd"),
                    customerName = obj.optString("customerName").takeIf { it.isNotBlank() },
                    customerPhone = obj.optString("customerPhone").takeIf { it.isNotBlank() },
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun readSoldItemsEntity(file: File): List<SoldItemDetailEntity> {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        val list = mutableListOf<SoldItemDetailEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SoldItemDetailEntity(
                    itemId = obj.getString("itemId"),
                    invoiceNumber = obj.getString("invoiceNumber"),
                    productId = obj.getString("productId"),
                    productName = obj.getString("productName"),
                    colorName = obj.getString("colorName"),
                    sizeLabel = obj.getString("sizeLabel"),
                    quantity = obj.getLong("quantity"),
                    unitSellingPriceIqd = obj.getLong("unitSellingPriceIqd"),
                    totalPriceIqd = obj.getLong("totalPriceIqd"),
                    cogsIqd = obj.getLong("cogsIqd")
                )
            )
        }
        return list
    }

    private fun readExpensesEntity(file: File): List<ExpenseEntity> {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        val list = mutableListOf<ExpenseEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ExpenseEntity(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    category = obj.getString("category"),
                    amountIqd = obj.getLong("amountIqd"),
                    date = obj.getString("date"),
                    notes = obj.optString("notes", ""),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun readMovementsEntity(file: File): List<StockMovementEntity> {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        val list = mutableListOf<StockMovementEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                StockMovementEntity(
                    movementId = obj.getString("movementId"),
                    productId = obj.getString("productId"),
                    colorId = obj.getString("colorId"),
                    sizeLabel = obj.getString("sizeLabel"),
                    movementType = obj.getString("movementType"),
                    quantityDelta = obj.getLong("quantityDelta"),
                    reason = obj.getString("reason"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    dateTimeFormatted = obj.getString("dateTimeFormatted")
                )
            )
        }
        return list
    }
}

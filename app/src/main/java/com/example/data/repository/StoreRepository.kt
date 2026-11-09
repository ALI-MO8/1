package com.example.data.repository

import com.example.data.engine.FifoInventoryEngine
import com.example.data.local.dao.StoreDao
import com.example.data.local.entity.BackupMetadataEntity
import com.example.data.local.entity.ColorVariantEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PurchaseBatchEntity
import com.example.data.local.entity.SaleInvoiceEntity
import com.example.data.local.entity.SizeStockEntity
import com.example.data.local.entity.SoldItemDetailEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.model.CartItem
import com.example.data.model.CustomerInfo
import com.example.data.model.ExpenseRecord
import com.example.data.model.ProductColorVariant
import com.example.data.model.ProductModel
import com.example.data.model.PurchaseBatch
import com.example.data.model.SaleInvoice
import com.example.data.model.SizeStock
import com.example.data.model.SoldItemDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class StoreRepository(private val storeDao: StoreDao) {

    /**
     * دمج الموديلات والألوان والقياسات من جداول Room في Flow تفاعلي من كائنات ProductModel
     */
    val allProductsFlow: Flow<List<ProductModel>> = combine(
        storeDao.getAllProducts(),
        storeDao.getAllColors(),
        storeDao.getAllSizes()
    ) { productsList, colorsList, sizesList ->
        productsList.map { pEntity ->
            val modelColors = colorsList.filter { it.productId == pEntity.id }.map { cEntity ->
                val colorSizes = sizesList.filter { it.colorId == cEntity.colorId }.map { sEntity ->
                    SizeStock(
                        sizeLabel = sEntity.sizeLabel,
                        currentStock = sEntity.currentStock
                    )
                }
                ProductColorVariant(
                    colorId = cEntity.colorId,
                    colorName = cEntity.colorName,
                    colorImageUri = cEntity.colorImageUri,
                    sizes = colorSizes
                )
            }
            ProductModel(
                id = pEntity.id,
                name = pEntity.name,
                mainImageUri = pEntity.mainImageUri,
                defaultCostPriceIqd = pEntity.defaultCostPriceIqd,
                defaultSellingPriceIqd = pEntity.defaultSellingPriceIqd,
                colors = modelColors
            )
        }
    }

    /**
     * دفعات الشراء المحفوظة في قاعدة البيانات
     */
    val allPurchaseBatchesFlow: Flow<List<PurchaseBatch>> = storeDao.getAllPurchaseBatches().map { list ->
        list.map { entity ->
            PurchaseBatch(
                batchId = entity.batchId,
                productId = entity.productId,
                colorId = entity.colorId,
                sizeLabel = entity.sizeLabel,
                purchasedQty = entity.purchasedQty,
                remainingQty = entity.remainingQty,
                unitCostIqd = entity.unitCostIqd,
                purchaseDate = entity.purchaseDate,
                timestamp = entity.timestamp
            )
        }
    }

    /**
     * فواتير المبيعات الصادرة
     */
    val allSalesInvoicesFlow: Flow<List<SaleInvoice>> = combine(
        storeDao.getAllInvoices(),
        storeDao.getAllSoldItemDetails()
    ) { invoices, soldItems ->
        invoices.map { inv ->
            val items = soldItems.filter { it.invoiceNumber == inv.invoiceNumber }.map { item ->
                SoldItemDetail(
                    productId = item.productId,
                    productName = item.productName,
                    colorName = item.colorName,
                    sizeLabel = item.sizeLabel,
                    quantity = item.quantity,
                    unitSellingPriceIqd = item.unitSellingPriceIqd,
                    totalPriceIqd = item.totalPriceIqd,
                    cogsIqd = item.cogsIqd
                )
            }
            SaleInvoice(
                invoiceId = inv.invoiceNumber,
                invoiceNumber = inv.invoiceNumber,
                saleDateTime = inv.saleDateTime,
                items = items,
                totalAmountIqd = inv.totalAmountIqd,
                cogsTotalIqd = inv.cogsTotalIqd,
                grossProfitIqd = inv.grossProfitIqd,
                customerInfo = if (!inv.customerName.isNullOrBlank() || !inv.customerPhone.isNullOrBlank()) {
                    CustomerInfo(name = inv.customerName ?: "", phone = inv.customerPhone ?: "")
                } else null
            )
        }
    }

    /**
     * المصاريف
     */
    val allExpensesFlow: Flow<List<ExpenseRecord>> = storeDao.getAllExpenses().map { list ->
        list.map { entity ->
            ExpenseRecord(
                expenseId = entity.id,
                title = entity.title,
                category = entity.category,
                amountIqd = entity.amountIqd,
                date = entity.date,
                notes = entity.notes
            )
        }
    }

    /**
     * سجل حركات المخزون
     */
    val stockMovementsFlow: Flow<List<StockMovementEntity>> = storeDao.getAllStockMovements()

    /**
     * بيانات النسخ الاحتياطي
     */
    val backupMetadataFlow: Flow<BackupMetadataEntity?> = storeDao.getBackupMetadata()

    // ==========================================================
    // العمليات
    // ==========================================================

    suspend fun addProduct(product: ProductModel) {
        val dateString = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        val dateTimeFormatted = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()).format(Date())

        val pEntity = ProductEntity(
            id = product.id,
            name = product.name,
            mainImageUri = product.mainImageUri,
            defaultCostPriceIqd = product.defaultCostPriceIqd,
            defaultSellingPriceIqd = product.defaultSellingPriceIqd
        )

        val colorEntities = mutableListOf<ColorVariantEntity>()
        val sizeEntities = mutableListOf<SizeStockEntity>()
        val initialBatches = mutableListOf<PurchaseBatchEntity>()
        val movements = mutableListOf<StockMovementEntity>()

        product.colors.forEach { color ->
            colorEntities.add(
                ColorVariantEntity(
                    colorId = color.colorId,
                    productId = product.id,
                    colorName = color.colorName,
                    colorImageUri = color.colorImageUri
                )
            )

            color.sizes.forEach { size ->
                sizeEntities.add(
                    SizeStockEntity(
                        colorId = color.colorId,
                        productId = product.id,
                        sizeLabel = size.sizeLabel,
                        currentStock = size.currentStock
                    )
                )

                if (size.currentStock > 0) {
                    initialBatches.add(
                        PurchaseBatchEntity(
                            productId = product.id,
                            colorId = color.colorId,
                            sizeLabel = size.sizeLabel,
                            purchasedQty = size.currentStock,
                            remainingQty = size.currentStock,
                            unitCostIqd = product.defaultCostPriceIqd,
                            purchaseDate = dateString
                        )
                    )

                    movements.add(
                        StockMovementEntity(
                            productId = product.id,
                            colorId = color.colorId,
                            sizeLabel = size.sizeLabel,
                            movementType = "PURCHASE",
                            quantityDelta = size.currentStock,
                            reason = "دفعة المخزون الافتتاحية للموديل",
                            dateTimeFormatted = dateTimeFormatted
                        )
                    )
                }
            }
        }

        storeDao.insertFullProductTransaction(pEntity, colorEntities, sizeEntities, initialBatches, movements)
    }

    suspend fun updateProductFull(
        updatedProduct: ProductModel,
        stockAdjustmentMovements: List<StockMovementEntity>
    ) {
        val pEntity = ProductEntity(
            id = updatedProduct.id,
            name = updatedProduct.name,
            mainImageUri = updatedProduct.mainImageUri,
            defaultCostPriceIqd = updatedProduct.defaultCostPriceIqd,
            defaultSellingPriceIqd = updatedProduct.defaultSellingPriceIqd
        )

        val colorEntities = mutableListOf<ColorVariantEntity>()
        val sizeEntities = mutableListOf<SizeStockEntity>()

        updatedProduct.colors.forEach { col ->
            colorEntities.add(
                ColorVariantEntity(
                    colorId = col.colorId,
                    productId = updatedProduct.id,
                    colorName = col.colorName,
                    colorImageUri = col.colorImageUri
                )
            )

            col.sizes.forEach { sz ->
                sizeEntities.add(
                    SizeStockEntity(
                        colorId = col.colorId,
                        productId = updatedProduct.id,
                        sizeLabel = sz.sizeLabel,
                        currentStock = sz.currentStock
                    )
                )
            }
        }

        storeDao.updateFullProductTransaction(pEntity, colorEntities, sizeEntities, stockAdjustmentMovements)
    }

    suspend fun archiveOrDeleteProduct(productId: String) {
        storeDao.archiveProduct(productId)
    }

    suspend fun addPurchaseBatch(
        productId: String,
        colorId: String,
        sizeLabel: String,
        quantity: Long,
        unitCostIqd: Long,
        purchaseDate: String
    ) {
        val dateTimeFormatted = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()).format(Date())
        val batchEntity = PurchaseBatchEntity(
            productId = productId,
            colorId = colorId,
            sizeLabel = sizeLabel,
            purchasedQty = quantity,
            remainingQty = quantity,
            unitCostIqd = unitCostIqd,
            purchaseDate = purchaseDate
        )

        val movement = StockMovementEntity(
            productId = productId,
            colorId = colorId,
            sizeLabel = sizeLabel,
            movementType = "PURCHASE",
            quantityDelta = quantity,
            reason = "توريد دفعة جديدة بتكلفة $unitCostIqd د.ع",
            dateTimeFormatted = dateTimeFormatted
        )

        storeDao.addPurchaseBatchAtomic(batchEntity, movement)
    }

    suspend fun completeSaleTransactionAtomic(
        cartItems: List<CartItem>,
        customerInfo: CustomerInfo?
    ): Result<SaleInvoice> {
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalStateException("السلة فارغة"))
        }

        // قراءة الدفعات الحالية من قاعدة البيانات
        val allBatchesEntities = storeDao.getAllPurchaseBatchesList()
        var runningBatches = allBatchesEntities.map { entity ->
            PurchaseBatch(
                batchId = entity.batchId,
                productId = entity.productId,
                colorId = entity.colorId,
                sizeLabel = entity.sizeLabel,
                purchasedQty = entity.purchasedQty,
                remainingQty = entity.remainingQty,
                unitCostIqd = entity.unitCostIqd,
                purchaseDate = entity.purchaseDate,
                timestamp = entity.timestamp
            )
        }

        val soldDetails = mutableListOf<SoldItemDetailEntity>()
        val movements = mutableListOf<StockMovementEntity>()
        val updatedSizes = mutableListOf<SizeStockEntity>()
        var totalInvoiceCogs = 0L
        var totalInvoiceAmount = 0L
        val dateFormatted = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()).format(Date())
        val invoiceNumber = "INV-${System.currentTimeMillis() % 100000}"

        try {
            for (cartItem in cartItems) {
                val deduction = FifoInventoryEngine.processFifoDeduction(
                    productId = cartItem.productId,
                    colorId = cartItem.colorId,
                    sizeLabel = cartItem.sizeLabel,
                    quantityToSell = cartItem.quantity,
                    allBatches = runningBatches
                )

                runningBatches = deduction.updatedBatches
                totalInvoiceCogs += deduction.cogsIqd
                val itemTotal = cartItem.totalItemPriceIqd
                totalInvoiceAmount += itemTotal

                soldDetails.add(
                    SoldItemDetailEntity(
                        invoiceNumber = invoiceNumber,
                        productId = cartItem.productId,
                        productName = cartItem.productName,
                        colorName = cartItem.colorName,
                        sizeLabel = cartItem.sizeLabel,
                        quantity = cartItem.quantity,
                        unitSellingPriceIqd = cartItem.unitSellingPriceIqd,
                        totalPriceIqd = itemTotal,
                        cogsIqd = deduction.cogsIqd
                    )
                )

                movements.add(
                    StockMovementEntity(
                        productId = cartItem.productId,
                        colorId = cartItem.colorId,
                        sizeLabel = cartItem.sizeLabel,
                        movementType = "SALE",
                        quantityDelta = -cartItem.quantity,
                        reason = "فاتورة بيع رقم $invoiceNumber",
                        dateTimeFormatted = dateFormatted
                    )
                )

                // تجهيز تحديث كمية المخزون للقياس
                val existingSize = storeDao.getSizeByLabel(cartItem.colorId, cartItem.sizeLabel)
                if (existingSize != null) {
                    updatedSizes.add(
                        existingSize.copy(
                            currentStock = maxOf(0L, existingSize.currentStock - cartItem.quantity)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        // تحويل الدفعات المحدثة إلى كيانات Room
        val updatedBatchesEntities = runningBatches.map { b ->
            PurchaseBatchEntity(
                batchId = b.batchId,
                productId = b.productId,
                colorId = b.colorId,
                sizeLabel = b.sizeLabel,
                purchasedQty = b.purchasedQty,
                remainingQty = b.remainingQty,
                unitCostIqd = b.unitCostIqd,
                purchaseDate = b.purchaseDate,
                timestamp = b.timestamp
            )
        }

        val grossProfit = totalInvoiceAmount - totalInvoiceCogs
        val invoiceEntity = SaleInvoiceEntity(
            invoiceNumber = invoiceNumber,
            saleDateTime = dateFormatted,
            totalAmountIqd = totalInvoiceAmount,
            cogsTotalIqd = totalInvoiceCogs,
            grossProfitIqd = grossProfit,
            customerName = customerInfo?.name?.takeIf { it.isNotBlank() },
            customerPhone = customerInfo?.phone?.takeIf { it.isNotBlank() }
        )

        // تنفيذ المعاملة الذرية
        storeDao.executeSaleAtomic(
            invoice = invoiceEntity,
            soldItems = soldDetails,
            updatedBatches = updatedBatchesEntities,
            updatedSizes = updatedSizes,
            movements = movements
        )

        val resultInvoice = SaleInvoice(
            invoiceId = invoiceNumber,
            invoiceNumber = invoiceNumber,
            saleDateTime = dateFormatted,
            items = soldDetails.map {
                SoldItemDetail(
                    productId = it.productId,
                    productName = it.productName,
                    colorName = it.colorName,
                    sizeLabel = it.sizeLabel,
                    quantity = it.quantity,
                    unitSellingPriceIqd = it.unitSellingPriceIqd,
                    totalPriceIqd = it.totalPriceIqd,
                    cogsIqd = it.cogsIqd
                )
            },
            totalAmountIqd = totalInvoiceAmount,
            cogsTotalIqd = totalInvoiceCogs,
            grossProfitIqd = grossProfit,
            customerInfo = if (customerInfo?.hasData == true) customerInfo else null
        )

        return Result.success(resultInvoice)
    }

    suspend fun addExpense(title: String, category: String, amountIqd: Long, notes: String = "") {
        val dateString = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        val entity = ExpenseEntity(
            title = title.trim(),
            category = category,
            amountIqd = amountIqd,
            date = dateString,
            notes = notes.trim()
        )
        storeDao.insertExpense(entity)
    }

    suspend fun updateBackupMetadata(metadata: BackupMetadataEntity) {
        storeDao.setBackupMetadata(metadata)
    }
}

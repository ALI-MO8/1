package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.BackupMetadataEntity
import com.example.data.local.entity.ColorVariantEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PurchaseBatchEntity
import com.example.data.local.entity.SaleInvoiceEntity
import com.example.data.local.entity.SizeStockEntity
import com.example.data.local.entity.SoldItemDetailEntity
import com.example.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    // ==========================================
    // المنتجات والألوان والقياسات
    // ==========================================
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM color_variants WHERE isArchived = 0")
    fun getAllColors(): Flow<List<ColorVariantEntity>>

    @Query("SELECT * FROM color_variants WHERE productId = :productId AND isArchived = 0")
    fun getColorsByProduct(productId: String): Flow<List<ColorVariantEntity>>

    @Query("SELECT * FROM size_stocks WHERE isArchived = 0")
    fun getAllSizes(): Flow<List<SizeStockEntity>>

    @Query("SELECT * FROM size_stocks WHERE colorId = :colorId AND isArchived = 0")
    fun getSizesByColor(colorId: String): Flow<List<SizeStockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColors(colors: List<ColorVariantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSizes(sizes: List<SizeStockEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Update
    suspend fun updateColor(color: ColorVariantEntity)

    @Update
    suspend fun updateSize(size: SizeStockEntity)

    @Query("UPDATE products SET isArchived = 1 WHERE id = :productId")
    suspend fun archiveProduct(productId: String)

    @Query("UPDATE color_variants SET isArchived = 1 WHERE colorId = :colorId")
    suspend fun archiveColor(colorId: String)

    @Query("UPDATE size_stocks SET isArchived = 1 WHERE sizeId = :sizeId")
    suspend fun archiveSize(sizeId: String)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductPermanent(productId: String)

    @Query("DELETE FROM color_variants WHERE colorId = :colorId")
    suspend fun deleteColorPermanent(colorId: String)

    @Query("DELETE FROM size_stocks WHERE sizeId = :sizeId")
    suspend fun deleteSizePermanent(sizeId: String)

    // ==========================================
    // دفعات الشراء (FIFO)
    // ==========================================
    @Query("SELECT * FROM purchase_batches ORDER BY timestamp ASC")
    fun getAllPurchaseBatches(): Flow<List<PurchaseBatchEntity>>

    @Query("SELECT * FROM purchase_batches ORDER BY timestamp ASC")
    suspend fun getAllPurchaseBatchesList(): List<PurchaseBatchEntity>

    @Query("SELECT * FROM purchase_batches WHERE productId = :productId AND colorId = :colorId AND sizeLabel = :sizeLabel AND remainingQty > 0 ORDER BY timestamp ASC")
    suspend fun getAvailableBatchesFifo(productId: String, colorId: String, sizeLabel: String): List<PurchaseBatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseBatch(batch: PurchaseBatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseBatches(batches: List<PurchaseBatchEntity>)

    @Update
    suspend fun updatePurchaseBatches(batches: List<PurchaseBatchEntity>)

    // ==========================================
    // المبيعات والفواتير
    // ==========================================
    @Query("SELECT * FROM sale_invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<SaleInvoiceEntity>>

    @Query("SELECT * FROM sold_item_details")
    fun getAllSoldItemDetails(): Flow<List<SoldItemDetailEntity>>

    @Query("SELECT * FROM sold_item_details WHERE invoiceNumber = :invoiceNumber")
    suspend fun getSoldItemsByInvoice(invoiceNumber: String): List<SoldItemDetailEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleInvoice(invoice: SaleInvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoldItemDetails(items: List<SoldItemDetailEntity>)

    // ==========================================
    // المصاريف
    // ==========================================
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    // ==========================================
    // حركات المخزون والتدقيق
    // ==========================================
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllStockMovements(): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovements(movements: List<StockMovementEntity>)

    // ==========================================
    // النسخ الاحتياطي
    // ==========================================
    @Query("SELECT * FROM backup_metadata WHERE id = 1 LIMIT 1")
    fun getBackupMetadata(): Flow<BackupMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBackupMetadata(metadata: BackupMetadataEntity)

    // ==========================================
    // المعاملات الذرية (Transactions)
    // ==========================================

    @Transaction
    suspend fun insertFullProductTransaction(
        product: ProductEntity,
        colors: List<ColorVariantEntity>,
        sizes: List<SizeStockEntity>,
        initialBatches: List<PurchaseBatchEntity>,
        movements: List<StockMovementEntity>
    ) {
        insertProduct(product)
        insertColors(colors)
        insertSizes(sizes)
        if (initialBatches.isNotEmpty()) {
            insertPurchaseBatches(initialBatches)
        }
        if (movements.isNotEmpty()) {
            insertStockMovements(movements)
        }
    }

    @Transaction
    suspend fun updateFullProductTransaction(
        product: ProductEntity,
        updatedColors: List<ColorVariantEntity>,
        updatedSizes: List<SizeStockEntity>,
        stockMovements: List<StockMovementEntity>
    ) {
        updateProduct(product)
        insertColors(updatedColors)
        insertSizes(updatedSizes)
        if (stockMovements.isNotEmpty()) {
            insertStockMovements(stockMovements)
        }
    }

    @Transaction
    suspend fun addPurchaseBatchAtomic(
        batch: PurchaseBatchEntity,
        movement: StockMovementEntity
    ) {
        insertPurchaseBatch(batch)
        // زيادة المخزون في جدول size_stocks
        val existingSize = getSizeByLabel(batch.colorId, batch.sizeLabel)
        if (existingSize != null) {
            updateSize(existingSize.copy(currentStock = existingSize.currentStock + batch.purchasedQty))
        } else {
            insertSizes(
                listOf(
                    SizeStockEntity(
                        colorId = batch.colorId,
                        productId = batch.productId,
                        sizeLabel = batch.sizeLabel,
                        currentStock = batch.purchasedQty
                    )
                )
            )
        }
        insertStockMovement(movement)
    }

    @Query("SELECT * FROM size_stocks WHERE colorId = :colorId AND sizeLabel = :sizeLabel AND isArchived = 0 LIMIT 1")
    suspend fun getSizeByLabel(colorId: String, sizeLabel: String): SizeStockEntity?

    @Transaction
    suspend fun executeSaleAtomic(
        invoice: SaleInvoiceEntity,
        soldItems: List<SoldItemDetailEntity>,
        updatedBatches: List<PurchaseBatchEntity>,
        updatedSizes: List<SizeStockEntity>,
        movements: List<StockMovementEntity>
    ) {
        insertSaleInvoice(invoice)
        insertSoldItemDetails(soldItems)
        updatePurchaseBatches(updatedBatches)
        updatedSizes.forEach { updateSize(it) }
        insertStockMovements(movements)
    }

    @Transaction
    suspend fun restoreDatabaseAtomic(
        products: List<ProductEntity>,
        colors: List<ColorVariantEntity>,
        sizes: List<SizeStockEntity>,
        batches: List<PurchaseBatchEntity>,
        invoices: List<SaleInvoiceEntity>,
        soldItems: List<SoldItemDetailEntity>,
        expenses: List<ExpenseEntity>,
        movements: List<StockMovementEntity>,
        backupMeta: BackupMetadataEntity
    ) {
        // مسح الجداول الحالية وإعادة كتابتها بالكامل بالبيانات المستوردة
        clearAllTablesDirectly()
        insertProductsDirect(products)
        insertColors(colors)
        insertSizes(sizes)
        insertPurchaseBatches(batches)
        invoices.forEach { insertSaleInvoice(it) }
        insertSoldItemDetails(soldItems)
        expenses.forEach { insertExpense(it) }
        insertStockMovements(movements)
        setBackupMetadata(backupMeta)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductsDirect(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clearProducts()
    @Query("DELETE FROM color_variants")
    suspend fun clearColors()
    @Query("DELETE FROM size_stocks")
    suspend fun clearSizes()
    @Query("DELETE FROM purchase_batches")
    suspend fun clearBatches()
    @Query("DELETE FROM sale_invoices")
    suspend fun clearInvoices()
    @Query("DELETE FROM sold_item_details")
    suspend fun clearSoldItems()
    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()
    @Query("DELETE FROM stock_movements")
    suspend fun clearMovements()

    @Transaction
    suspend fun clearAllTablesDirectly() {
        clearProducts()
        clearColors()
        clearSizes()
        clearBatches()
        clearInvoices()
        clearSoldItems()
        clearExpenses()
        clearMovements()
    }
}

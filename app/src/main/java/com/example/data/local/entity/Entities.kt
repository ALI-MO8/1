package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mainImageUri: String? = null,
    val defaultCostPriceIqd: Long,
    val defaultSellingPriceIqd: Long,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "color_variants",
    indices = [Index(value = ["productId"])]
)
data class ColorVariantEntity(
    @PrimaryKey val colorId: String = UUID.randomUUID().toString(),
    val productId: String,
    val colorName: String,
    val colorImageUri: String? = null,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "size_stocks",
    indices = [Index(value = ["colorId"]), Index(value = ["productId"])]
)
data class SizeStockEntity(
    @PrimaryKey val sizeId: String = UUID.randomUUID().toString(),
    val colorId: String,
    val productId: String,
    val sizeLabel: String,
    val currentStock: Long,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "purchase_batches",
    indices = [Index(value = ["productId"]), Index(value = ["colorId"])]
)
data class PurchaseBatchEntity(
    @PrimaryKey val batchId: String = UUID.randomUUID().toString(),
    val productId: String,
    val colorId: String,
    val sizeLabel: String,
    val purchasedQty: Long,
    val remainingQty: Long,
    val unitCostIqd: Long,
    val purchaseDate: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_invoices")
data class SaleInvoiceEntity(
    @PrimaryKey val invoiceNumber: String,
    val saleDateTime: String,
    val totalAmountIqd: Long,
    val cogsTotalIqd: Long,
    val grossProfitIqd: Long,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sold_item_details",
    indices = [Index(value = ["invoiceNumber"])]
)
data class SoldItemDetailEntity(
    @PrimaryKey val itemId: String = UUID.randomUUID().toString(),
    val invoiceNumber: String,
    val productId: String,
    val productName: String,
    val colorName: String,
    val sizeLabel: String,
    val quantity: Long,
    val unitSellingPriceIqd: Long,
    val totalPriceIqd: Long,
    val cogsIqd: Long
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val amountIqd: Long,
    val date: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stock_movements",
    indices = [Index(value = ["productId"]), Index(value = ["colorId"])]
)
data class StockMovementEntity(
    @PrimaryKey val movementId: String = UUID.randomUUID().toString(),
    val productId: String,
    val colorId: String,
    val sizeLabel: String,
    val movementType: String, // "PURCHASE", "SALE", "ADJUSTMENT"
    val quantityDelta: Long, // e.g. +10, -5
    val reason: String, // "شراء دفعة", "فاتورة بيع", "تعديل جردي"
    val timestamp: Long = System.currentTimeMillis(),
    val dateTimeFormatted: String
)

@Entity(tableName = "backup_metadata")
data class BackupMetadataEntity(
    @PrimaryKey val id: Int = 1,
    val lastBackupDate: String? = null,
    val lastBackupFileSize: Long = 0L,
    val lastBackupRecordsCount: Int = 0
)

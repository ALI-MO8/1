package com.example.data.model

import java.util.UUID

data class ProductModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mainImageUri: String? = null,
    val defaultCostPriceIqd: Long,
    val defaultSellingPriceIqd: Long,
    val colors: List<ProductColorVariant> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalPieces: Long
        get() = colors.sumOf { color -> color.sizes.sumOf { it.currentStock } }

    val isAvailable: Boolean
        get() = totalPieces > 0
}

data class ProductColorVariant(
    val colorId: String = UUID.randomUUID().toString(),
    val colorName: String,
    val colorImageUri: String? = null,
    val sizes: List<SizeStock> = emptyList()
) {
    val totalColorStock: Long
        get() = sizes.sumOf { it.currentStock }
}

data class SizeStock(
    val sizeLabel: String,
    val currentStock: Long
)

data class PurchaseBatch(
    val batchId: String = UUID.randomUUID().toString(),
    val productId: String,
    val colorId: String,
    val sizeLabel: String,
    val purchasedQty: Long,
    val remainingQty: Long,
    val unitCostIqd: Long,
    val purchaseDate: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CartItem(
    val cartItemId: String = UUID.randomUUID().toString(),
    val productId: String,
    val productName: String,
    val mainImageUri: String? = null,
    val colorId: String,
    val colorName: String,
    val colorImageUri: String? = null,
    val sizeLabel: String,
    val availableStock: Long,
    val quantity: Long,
    val unitSellingPriceIqd: Long
) {
    val totalItemPriceIqd: Long
        get() = quantity * unitSellingPriceIqd
}

data class CustomerInfo(
    val name: String = "",
    val phone: String = "",
    val address: String = ""
) {
    val hasData: Boolean
        get() = name.isNotBlank() || phone.isNotBlank() || address.isNotBlank()
}

data class SaleInvoice(
    val invoiceId: String = UUID.randomUUID().toString(),
    val invoiceNumber: String,
    val saleDateTime: String,
    val items: List<SoldItemDetail>,
    val totalAmountIqd: Long,
    val cogsTotalIqd: Long,
    val grossProfitIqd: Long,
    val customerInfo: CustomerInfo? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class SoldItemDetail(
    val productId: String,
    val productName: String,
    val colorName: String,
    val sizeLabel: String,
    val quantity: Long,
    val unitSellingPriceIqd: Long,
    val totalPriceIqd: Long,
    val cogsIqd: Long
)

data class ExpenseRecord(
    val expenseId: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val amountIqd: Long,
    val date: String,
    val notes: String = ""
)

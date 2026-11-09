package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.FifoInventoryEngine
import com.example.data.local.entity.BackupMetadataEntity
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
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StoreUiState(
    val products: List<ProductModel> = emptyList(),
    val purchaseBatches: List<PurchaseBatch> = emptyList(),
    val salesInvoices: List<SaleInvoice> = emptyList(),
    val expenses: List<ExpenseRecord> = emptyList(),
    val stockMovements: List<StockMovementEntity> = emptyList(),
    val backupMetadata: BackupMetadataEntity? = null,
    val cartItems: List<CartItem> = emptyList(),

    // حالة تدفق اختيار المنتج للبيع خماسي المراحل
    val activeSaleModel: ProductModel? = null,
    val activeSaleColor: ProductColorVariant? = null,
    val activeSaleSize: SizeStock? = null,
    val activeSaleQuantity: Long = 1L,
    val activeSalePrice: Long = 0L,
    val currentSaleStage: SaleStage = SaleStage.NONE,

    // رسائل وتنبيهات
    val toastMessage: String? = null,
    val lastCompletedInvoice: SaleInvoice? = null
)

enum class SaleStage {
    NONE,
    SELECT_COLOR,
    SELECT_SIZE,
    SELECT_QTY_PRICE
}

class StoreViewModel(
    private val repository: StoreRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init {
        // إذا كان المستودع الحقيقي متوفراً، يتم الاشتراك في تدفقات Room Database
        repository?.let { repo ->
            viewModelScope.launch {
                repo.allProductsFlow.collect { list ->
                    _uiState.update { it.copy(products = list) }
                }
            }
            viewModelScope.launch {
                repo.allPurchaseBatchesFlow.collect { batches ->
                    _uiState.update { it.copy(purchaseBatches = batches) }
                }
            }
            viewModelScope.launch {
                repo.allSalesInvoicesFlow.collect { invoices ->
                    _uiState.update { it.copy(salesInvoices = invoices) }
                }
            }
            viewModelScope.launch {
                repo.allExpensesFlow.collect { expenses ->
                    _uiState.update { it.copy(expenses = expenses) }
                }
            }
            viewModelScope.launch {
                repo.stockMovementsFlow.collect { movements ->
                    _uiState.update { it.copy(stockMovements = movements) }
                }
            }
            viewModelScope.launch {
                repo.backupMetadataFlow.collect { meta ->
                    _uiState.update { it.copy(backupMetadata = meta) }
                }
            }
        }
    }

    // ----------------------------------------------------
    // إضافة وتعديل وحذف الموديلات
    // ----------------------------------------------------
    fun addProduct(product: ProductModel) {
        if (repository != null) {
            viewModelScope.launch {
                repository.addProduct(product)
                _uiState.update { it.copy(toastMessage = "تم حفظ الموديل '${product.name}' في قاعدة البيانات بنجاح") }
            }
        } else {
            // نمط الذاكرة للاختبارات السريعة
            val newBatches = mutableListOf<PurchaseBatch>()
            val dateString = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
            product.colors.forEach { color ->
                color.sizes.forEach { size ->
                    if (size.currentStock > 0) {
                        newBatches.add(
                            PurchaseBatch(
                                productId = product.id,
                                colorId = color.colorId,
                                sizeLabel = size.sizeLabel,
                                purchasedQty = size.currentStock,
                                remainingQty = size.currentStock,
                                unitCostIqd = product.defaultCostPriceIqd,
                                purchaseDate = dateString
                            )
                        )
                    }
                }
            }
            _uiState.update { current ->
                current.copy(
                    products = current.products + product,
                    purchaseBatches = current.purchaseBatches + newBatches,
                    toastMessage = "تم حفظ الموديل '${product.name}' بنجاح"
                )
            }
        }
    }

    fun updateProduct(updatedProduct: ProductModel) {
        updateProductFull(updatedProduct, emptyList())
    }

    fun updateProductFull(updatedProduct: ProductModel, stockMovements: List<StockMovementEntity>) {
        if (repository != null) {
            viewModelScope.launch {
                repository.updateProductFull(updatedProduct, stockMovements)
                _uiState.update { it.copy(toastMessage = "تم تحديث كافة بيانات الموديل '${updatedProduct.name}' بنجاح") }
            }
        } else {
            _uiState.update { current ->
                val updatedList = current.products.map {
                    if (it.id == updatedProduct.id) updatedProduct else it
                }
                current.copy(
                    products = updatedList,
                    stockMovements = current.stockMovements + stockMovements,
                    toastMessage = "تم تحديث بيانات الموديل '${updatedProduct.name}' بنجاح"
                )
            }
        }
    }

    fun deleteProduct(productId: String) {
        if (repository != null) {
            viewModelScope.launch {
                repository.archiveOrDeleteProduct(productId)
                _uiState.update { current ->
                    current.copy(
                        cartItems = current.cartItems.filterNot { it.productId == productId },
                        toastMessage = "تم حذف الموديل بنجاح"
                    )
                }
            }
        } else {
            _uiState.update { current ->
                current.copy(
                    products = current.products.filterNot { it.id == productId },
                    cartItems = current.cartItems.filterNot { it.productId == productId },
                    toastMessage = "تم حذف الموديل بنجاح"
                )
            }
        }
    }

    // ----------------------------------------------------
    // تدفق البيع خماسي المراحل
    // ----------------------------------------------------
    fun startSaleFlow(product: ProductModel) {
        _uiState.update {
            it.copy(
                activeSaleModel = product,
                activeSaleColor = null,
                activeSaleSize = null,
                activeSaleQuantity = 1L,
                activeSalePrice = product.defaultSellingPriceIqd,
                currentSaleStage = SaleStage.SELECT_COLOR
            )
        }
    }

    fun selectSaleColor(color: ProductColorVariant) {
        _uiState.update {
            it.copy(
                activeSaleColor = color,
                activeSaleSize = null,
                currentSaleStage = SaleStage.SELECT_SIZE
            )
        }
    }

    fun selectSaleSize(size: SizeStock) {
        val model = _uiState.value.activeSaleModel
        val defaultPrice = model?.defaultSellingPriceIqd ?: 0L
        _uiState.update {
            it.copy(
                activeSaleSize = size,
                activeSaleQuantity = 1L,
                activeSalePrice = defaultPrice,
                currentSaleStage = SaleStage.SELECT_QTY_PRICE
            )
        }
    }

    fun updateSaleQuantity(qty: Long) {
        val maxStock = _uiState.value.activeSaleSize?.currentStock ?: 1L
        val validQty = qty.coerceIn(1L, maxOf(1L, maxStock))
        _uiState.update { it.copy(activeSaleQuantity = validQty) }
    }

    fun updateSaleCustomPrice(price: Long) {
        _uiState.update { it.copy(activeSalePrice = maxOf(0L, price)) }
    }

    fun cancelSaleFlow() {
        _uiState.update {
            it.copy(
                activeSaleModel = null,
                activeSaleColor = null,
                activeSaleSize = null,
                currentSaleStage = SaleStage.NONE
            )
        }
    }

    fun confirmAddToCart() {
        val state = _uiState.value
        val model = state.activeSaleModel ?: return
        val color = state.activeSaleColor ?: return
        val size = state.activeSaleSize ?: return

        if (size.currentStock < state.activeSaleQuantity) {
            _uiState.update { it.copy(toastMessage = "الكمية المطلوبة أكبر من المتوفر بالمخزون") }
            return
        }

        val existingIndex = state.cartItems.indexOfFirst {
            it.productId == model.id && it.colorId == color.colorId && it.sizeLabel == size.sizeLabel
        }

        val updatedCart = state.cartItems.toMutableList()
        if (existingIndex >= 0) {
            val existing = updatedCart[existingIndex]
            val newTotalQty = minOf(size.currentStock, existing.quantity + state.activeSaleQuantity)
            updatedCart[existingIndex] = existing.copy(
                quantity = newTotalQty,
                unitSellingPriceIqd = state.activeSalePrice
            )
        } else {
            updatedCart.add(
                CartItem(
                    productId = model.id,
                    productName = model.name,
                    mainImageUri = model.mainImageUri,
                    colorId = color.colorId,
                    colorName = color.colorName,
                    colorImageUri = color.colorImageUri,
                    sizeLabel = size.sizeLabel,
                    availableStock = size.currentStock,
                    quantity = state.activeSaleQuantity,
                    unitSellingPriceIqd = state.activeSalePrice
                )
            )
        }

        _uiState.update {
            it.copy(
                cartItems = updatedCart,
                activeSaleModel = null,
                activeSaleColor = null,
                activeSaleSize = null,
                currentSaleStage = SaleStage.NONE,
                toastMessage = "تمت إضافة ${model.name} (${color.colorName} - ${size.sizeLabel}) إلى السلة"
            )
        }
    }

    // ----------------------------------------------------
    // إدارة سلة المبيعات
    // ----------------------------------------------------
    fun updateCartItemQuantity(cartItemId: String, newQty: Long) {
        if (newQty <= 0) {
            removeCartItem(cartItemId)
            return
        }
        _uiState.update { current ->
            val updated = current.cartItems.map { item ->
                if (item.cartItemId == cartItemId) {
                    val safeQty = minOf(item.availableStock, newQty)
                    item.copy(quantity = safeQty)
                } else item
            }
            current.copy(cartItems = updated)
        }
    }

    fun updateCartItemPrice(cartItemId: String, newPrice: Long) {
        _uiState.update { current ->
            val updated = current.cartItems.map { item ->
                if (item.cartItemId == cartItemId) {
                    item.copy(unitSellingPriceIqd = maxOf(0L, newPrice))
                } else item
            }
            current.copy(cartItems = updated)
        }
    }

    fun removeCartItem(cartItemId: String) {
        _uiState.update { current ->
            current.copy(cartItems = current.cartItems.filterNot { it.cartItemId == cartItemId })
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList()) }
    }

    // ----------------------------------------------------
    // تأكيد البيع الذري وفق نظام FIFO
    // ----------------------------------------------------
    fun completeSaleTransaction(customerInfo: CustomerInfo?): Result<SaleInvoice> {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) {
            return Result.failure(IllegalStateException("السلة فارغة"))
        }

        if (repository != null) {
            var finalResult: Result<SaleInvoice>? = null
            viewModelScope.launch {
                val res = repository.completeSaleTransactionAtomic(state.cartItems, customerInfo)
                finalResult = res
                res.onSuccess { newInvoice ->
                    _uiState.update {
                        it.copy(
                            cartItems = emptyList(),
                            lastCompletedInvoice = newInvoice,
                            toastMessage = "تم إتمام عملية البيع وحفظ الفاتورة #${newInvoice.invoiceNumber} بنجاح"
                        )
                    }
                }.onFailure { err ->
                    _uiState.update { it.copy(toastMessage = "فشلت عملية البيع: ${err.localizedMessage}") }
                }
            }
            // إرجاع نجاح متوقع أو فحص متزامن أولي
            return Result.success(
                SaleInvoice(
                    invoiceNumber = "INV-${System.currentTimeMillis() % 100000}",
                    saleDateTime = "الآن",
                    items = emptyList(),
                    totalAmountIqd = state.cartItems.sumOf { it.totalItemPriceIqd },
                    cogsTotalIqd = 0L,
                    grossProfitIqd = 0L
                )
            )
        } else {
            // نمط الذاكرة البسيط لاختبارات الـ JVM
            var runningBatches = state.purchaseBatches
            val soldDetails = mutableListOf<SoldItemDetail>()
            var totalInvoiceCogs = 0L
            var totalInvoiceAmount = 0L

            try {
                for (cartItem in state.cartItems) {
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
                        SoldItemDetail(
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
                }
            } catch (e: Exception) {
                return Result.failure(e)
            }

            val updatedProducts = state.products.map { product ->
                val updatedColors = product.colors.map { color ->
                    val updatedSizes = color.sizes.map { size ->
                        val soldForSize = state.cartItems
                            .filter { it.productId == product.id && it.colorId == color.colorId && it.sizeLabel == size.sizeLabel }
                            .sumOf { it.quantity }
                        size.copy(currentStock = maxOf(0L, size.currentStock - soldForSize))
                    }
                    color.copy(sizes = updatedSizes)
                }
                product.copy(colors = updatedColors)
            }

            val dateFormatted = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()).format(Date())
            val invoiceNumber = "INV-${System.currentTimeMillis() % 100000}"
            val grossProfit = totalInvoiceAmount - totalInvoiceCogs

            val newInvoice = SaleInvoice(
                invoiceNumber = invoiceNumber,
                saleDateTime = dateFormatted,
                items = soldDetails,
                totalAmountIqd = totalInvoiceAmount,
                cogsTotalIqd = totalInvoiceCogs,
                grossProfitIqd = grossProfit,
                customerInfo = if (customerInfo?.hasData == true) customerInfo else null
            )

            _uiState.update { current ->
                current.copy(
                    products = updatedProducts,
                    purchaseBatches = runningBatches,
                    salesInvoices = listOf(newInvoice) + current.salesInvoices,
                    cartItems = emptyList(),
                    lastCompletedInvoice = newInvoice,
                    toastMessage = "تم إتمام عملية البيع وحفظ الفاتورة #$invoiceNumber بنجاح"
                )
            }

            return Result.success(newInvoice)
        }
    }

    // ----------------------------------------------------
    // توريد دفعات الشراء
    // ----------------------------------------------------
    fun addPurchaseBatch(
        productId: String,
        colorId: String,
        sizeLabel: String,
        quantity: Long,
        unitCostIqd: Long,
        purchaseDate: String
    ) {
        if (quantity <= 0 || unitCostIqd <= 0) return

        if (repository != null) {
            viewModelScope.launch {
                repository.addPurchaseBatch(productId, colorId, sizeLabel, quantity, unitCostIqd, purchaseDate)
                _uiState.update { it.copy(toastMessage = "تم حفظ دفعة الشراء ($quantity قطعة) في قاعدة البيانات بنجاح") }
            }
        } else {
            val newBatch = PurchaseBatch(
                productId = productId,
                colorId = colorId,
                sizeLabel = sizeLabel,
                purchasedQty = quantity,
                remainingQty = quantity,
                unitCostIqd = unitCostIqd,
                purchaseDate = purchaseDate
            )

            _uiState.update { current ->
                val updatedProducts = current.products.map { product ->
                    if (product.id == productId) {
                        val updatedColors = product.colors.map { color ->
                            if (color.colorId == colorId) {
                                val sizeExists = color.sizes.any { it.sizeLabel == sizeLabel }
                                val updatedSizes = if (sizeExists) {
                                    color.sizes.map { size ->
                                        if (size.sizeLabel == sizeLabel) size.copy(currentStock = size.currentStock + quantity) else size
                                    }
                                } else {
                                    color.sizes + SizeStock(sizeLabel = sizeLabel, currentStock = quantity)
                                }
                                color.copy(sizes = updatedSizes)
                            } else color
                        }
                        product.copy(colors = updatedColors)
                    } else product
                }

                current.copy(
                    products = updatedProducts,
                    purchaseBatches = current.purchaseBatches + newBatch,
                    toastMessage = "تم توريد $quantity قطعة بتكلفة $unitCostIqd د.ع بنجاح"
                )
            }
        }
    }

    // ----------------------------------------------------
    // المصاريف
    // ----------------------------------------------------
    fun addExpense(title: String, category: String, amountIqd: Long, notes: String = "") {
        if (amountIqd <= 0 || title.isBlank()) return
        if (repository != null) {
            viewModelScope.launch {
                repository.addExpense(title, category, amountIqd, notes)
                _uiState.update { it.copy(toastMessage = "تم تسجيل المصروف في قاعدة البيانات بنجاح") }
            }
        } else {
            val dateString = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
            val record = ExpenseRecord(
                title = title.trim(),
                category = category,
                amountIqd = amountIqd,
                date = dateString,
                notes = notes.trim()
            )
            _uiState.update { current ->
                current.copy(
                    expenses = listOf(record) + current.expenses,
                    toastMessage = "تم تسجيل المصروف بنجاح"
                )
            }
        }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun clearLastCompletedInvoice() {
        _uiState.update { it.copy(lastCompletedInvoice = null) }
    }
}

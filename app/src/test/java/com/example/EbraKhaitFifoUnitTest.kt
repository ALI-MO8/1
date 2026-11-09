package com.example

import com.example.data.engine.FifoInventoryEngine
import com.example.data.model.CartItem
import com.example.data.model.CustomerInfo
import com.example.data.model.ProductColorVariant
import com.example.data.model.ProductModel
import com.example.data.model.PurchaseBatch
import com.example.data.model.SizeStock
import com.example.ui.viewmodel.StoreViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EbraKhaitFifoUnitTest {

    @Test
    fun testExactUserFifoScenario() {
        // الدفعة الأولى: 20 قطعة بتكلفة 12,000 دينار للقطعة.
        val batch1 = PurchaseBatch(
            productId = "prod1",
            colorId = "col1",
            sizeLabel = "M",
            purchasedQty = 20L,
            remainingQty = 20L,
            unitCostIqd = 12000L,
            purchaseDate = "2026/11/01",
            timestamp = 1000L
        )

        // الدفعة الثانية: 30 قطعة بتكلفة 14,000 دينار للقطعة.
        val batch2 = PurchaseBatch(
            productId = "prod1",
            colorId = "col1",
            sizeLabel = "M",
            purchasedQty = 30L,
            remainingQty = 30L,
            unitCostIqd = 14000L,
            purchaseDate = "2026/11/05",
            timestamp = 2000L
        )

        val batches = listOf(batch1, batch2)

        // بيع 25 قطعة بسعر 20,000 دينار للقطعة
        val deductionResult = FifoInventoryEngine.processFifoDeduction(
            productId = "prod1",
            colorId = "col1",
            sizeLabel = "M",
            quantityToSell = 25L,
            allBatches = batches
        )

        // 1. تكلفة البضاعة المباعة COGS: (20 × 12,000) + (5 × 14,000) = 240,000 + 70,000 = 310,000 دينار
        assertEquals(310000L, deductionResult.cogsIqd)

        // 2. إجمالي الإيرادات: 25 × 20,000 = 500,000 دينار
        val totalRevenue = 25L * 20000L
        assertEquals(500000L, totalRevenue)

        // 3. الربح الإجمالي: 500,000 - 310,000 = 190,000 دينار
        val grossProfit = totalRevenue - deductionResult.cogsIqd
        assertEquals(190000L, grossProfit)

        // 4. إجمالي المخزون المتبقي: 25 قطعة (0 من الدفعة الأولى و 25 من الدفعة الثانية)
        val remainingStock = deductionResult.updatedBatches.sumOf { it.remainingQty }
        assertEquals(25L, remainingStock)

        // 5. التحقق من بقاء تكاليف الدفعات الأصلية دون تغيير
        val b1Updated = deductionResult.updatedBatches.find { it.batchId == batch1.batchId }!!
        val b2Updated = deductionResult.updatedBatches.find { it.batchId == batch2.batchId }!!
        assertEquals(0L, b1Updated.remainingQty)
        assertEquals(12000L, b1Updated.unitCostIqd)
        assertEquals(25L, b2Updated.remainingQty)
        assertEquals(14000L, b2Updated.unitCostIqd)
    }

    @Test
    fun testRejectSaleWhenStockInsufficient() {
        val batch1 = PurchaseBatch(
            productId = "prod1",
            colorId = "col1",
            sizeLabel = "S",
            purchasedQty = 5L,
            remainingQty = 5L,
            unitCostIqd = 10000L,
            purchaseDate = "2026/11/01"
        )

        var thrown = false
        try {
            // محاولة بيع 10 قطع والمتبقي 5 فقط
            FifoInventoryEngine.processFifoDeduction(
                productId = "prod1",
                colorId = "col1",
                sizeLabel = "S",
                quantityToSell = 10L,
                allBatches = listOf(batch1)
            )
        } catch (e: IllegalStateException) {
            thrown = true
        }
        assertTrue("يجب رفض البيع عند طلب كمية أكبر من المتوفر بالمخزون", thrown)
    }

    @Test
    fun testStoreViewModelMultiItemCheckoutWithCustomerInfoOptional() {
        val vm = StoreViewModel()

        val product1 = ProductModel(
            name = "فستان شيفون",
            defaultCostPriceIqd = 15000L,
            defaultSellingPriceIqd = 25000L,
            colors = listOf(
                ProductColorVariant(
                    colorName = "أزرق",
                    sizes = listOf(SizeStock("M", 10L), SizeStock("L", 10L))
                )
            )
        )
        vm.addProduct(product1)

        val product2 = ProductModel(
            name = "عباية خليجية",
            defaultCostPriceIqd = 30000L,
            defaultSellingPriceIqd = 50000L,
            colors = listOf(
                ProductColorVariant(
                    colorName = "أسود",
                    sizes = listOf(SizeStock("Free Size", 8L))
                )
            )
        )
        vm.addProduct(product2)

        // إضافة عناصر مختلفة للسلة
        vm.startSaleFlow(product1)
        vm.selectSaleColor(product1.colors.first())
        vm.selectSaleSize(product1.colors.first().sizes.first()) // M
        vm.updateSaleQuantity(2L)
        vm.confirmAddToCart()

        vm.startSaleFlow(product2)
        vm.selectSaleColor(product2.colors.first())
        vm.selectSaleSize(product2.colors.first().sizes.first()) // Free Size
        vm.updateSaleQuantity(1L)
        vm.updateSaleCustomPrice(48000L) // تعديل سعر البيع لهذه العملية
        vm.confirmAddToCart()

        assertEquals(2, vm.uiState.value.cartItems.size)

        // إتمام البيع مع ترك بيانات الزبون فارغة
        val emptyCust = CustomerInfo("", "", "")
        val result = vm.completeSaleTransaction(emptyCust)
        assertTrue(result.isSuccess)

        val invoice = result.getOrNull()!!
        assertEquals(2 * 25000L + 1 * 48000L, invoice.totalAmountIqd)
        assertNull(invoice.customerInfo) // ترك البيانات فارغة لا ينشئ زبوناً
        assertTrue(vm.uiState.value.cartItems.isEmpty()) // السلة تم تفريغها
    }

    @Test
    fun testMultiColorIndependentSizesAndQuantities() {
        val vm = StoreViewModel()

        // اختبار مثال المستخدم الدقيق:
        // الأسود: M = 5, L = 10, XL = 8 (المجموع: 23)
        // الأزرق: S = 4, M = 7, XXL = 3 (المجموع: 14)
        // الأبيض: L = 6, XL = 9 (المجموع: 15)
        // إجمالي الموديل = 23 + 14 + 15 = 52 قطعة
        val blackColor = ProductColorVariant(
            colorName = "أسود",
            sizes = listOf(SizeStock("M", 5L), SizeStock("L", 10L), SizeStock("XL", 8L))
        )
        val blueColor = ProductColorVariant(
            colorName = "أزرق",
            sizes = listOf(SizeStock("S", 4L), SizeStock("M", 7L), SizeStock("XXL", 3L))
        )
        val whiteColor = ProductColorVariant(
            colorName = "أبيض",
            sizes = listOf(SizeStock("L", 6L), SizeStock("XL", 9L))
        )

        val model = ProductModel(
            name = "فستان نسائي ملكي",
            defaultCostPriceIqd = 12000L,
            defaultSellingPriceIqd = 20000L,
            colors = listOf(blackColor, blueColor, whiteColor)
        )

        vm.addProduct(model)

        val state = vm.uiState.value
        assertEquals(1, state.products.size)
        val savedModel = state.products.first()
        assertEquals(3, savedModel.colors.size)
        assertEquals(52L, savedModel.totalPieces)

        // التحقق من استقلال أحجام وكميات كل لون
        val black = savedModel.colors.find { it.colorName == "أسود" }!!
        assertEquals(3, black.sizes.size)
        assertEquals(5L, black.sizes.find { it.sizeLabel == "M" }?.currentStock)
        assertEquals(10L, black.sizes.find { it.sizeLabel == "L" }?.currentStock)
        assertEquals(8L, black.sizes.find { it.sizeLabel == "XL" }?.currentStock)
        assertNull(black.sizes.find { it.sizeLabel == "S" }) // S لم يُضف للأسود

        val blue = savedModel.colors.find { it.colorName == "أزرق" }!!
        assertEquals(3, blue.sizes.size)
        assertEquals(4L, blue.sizes.find { it.sizeLabel == "S" }?.currentStock)
        assertEquals(7L, blue.sizes.find { it.sizeLabel == "M" }?.currentStock)
        assertEquals(3L, blue.sizes.find { it.sizeLabel == "XXL" }?.currentStock)
        assertNull(blue.sizes.find { it.sizeLabel == "L" }) // L لم يُضف للأزرق

        val white = savedModel.colors.find { it.colorName == "أبيض" }!!
        assertEquals(2, white.sizes.size)
        assertEquals(6L, white.sizes.find { it.sizeLabel == "L" }?.currentStock)
        assertEquals(9L, white.sizes.find { it.sizeLabel == "XL" }?.currentStock)

        // التحقق من إنشاء دفعات الشراء الأولى بدقة
        // عدد الدفعات = 3 (للأسود) + 3 (للأزرق) + 2 (للأبيض) = 8 دفعات
        assertEquals(8, state.purchaseBatches.size)

        // اختبار تعديل بيانات الموديل (تعديل السعر والاسم)
        val updatedModel = savedModel.copy(
            name = "فستان نسائي فاخر مطور",
            defaultSellingPriceIqd = 22000L
        )
        vm.updateProduct(updatedModel)
        assertEquals("فستان نسائي فاخر مطور", vm.uiState.value.products.first().name)
        assertEquals(22000L, vm.uiState.value.products.first().defaultSellingPriceIqd)

        // اختبار توريد دفعة مشتريات جديدة لموديل موجود مسبقاً (شراء بضاعة جديدة بتكلفة مستقلة)
        vm.addPurchaseBatch(
            productId = savedModel.id,
            colorId = black.colorId,
            sizeLabel = "M",
            quantity = 15L,
            unitCostIqd = 13500L,
            purchaseDate = "2026/11/09"
        )
        // يجب أن تزداد كمية قياس M في اللون الأسود من 5 إلى 20 قطعة
        val blackAfterRestock = vm.uiState.value.products.first().colors.find { it.colorName == "أسود" }!!
        assertEquals(20L, blackAfterRestock.sizes.find { it.sizeLabel == "M" }?.currentStock)
        // وإجمالي دفعات الشراء أصبح 9 دفعات مع حفظ التكلفة التاريخية 13,500 د.ع
        assertEquals(9, vm.uiState.value.purchaseBatches.size)
        val latestBatch = vm.uiState.value.purchaseBatches.last()
        assertEquals(13500L, latestBatch.unitCostIqd)
        assertEquals(15L, latestBatch.purchasedQty)
    }

    @Test
    fun testEditModelPreservesHistoricalCostAndSales() {
        val vm = StoreViewModel()

        val initialModel = ProductModel(
            name = "فستان سواريه",
            defaultCostPriceIqd = 10000L,
            defaultSellingPriceIqd = 18000L,
            colors = listOf(
                ProductColorVariant(
                    colorName = "ذهبي",
                    sizes = listOf(SizeStock("M", 10L))
                )
            )
        )
        vm.addProduct(initialModel)

        // بيع 3 قطع بسعر 18,000 د.ع
        vm.startSaleFlow(initialModel)
        vm.selectSaleColor(initialModel.colors.first())
        vm.selectSaleSize(initialModel.colors.first().sizes.first())
        vm.updateSaleQuantity(3L)
        vm.confirmAddToCart()

        val saleResult = vm.completeSaleTransaction(CustomerInfo("فاطمة", "07701234567"))
        assertTrue(saleResult.isSuccess)
        val invoice = saleResult.getOrNull()!!
        assertEquals(3 * 18000L, invoice.totalAmountIqd)
        assertEquals(3 * 10000L, invoice.cogsTotalIqd)
        assertEquals(24000L, invoice.grossProfitIqd)

        // الآن نقوم بتعديل الموديل (رفع سعر البيع المقترح إلى 25,000 ورفع التكلفة الافتراضية إلى 15,000 وتعديل المخزون)
        val editedModel = initialModel.copy(
            defaultCostPriceIqd = 15000L, // زيادة التكلفة الافتراضية
            defaultSellingPriceIqd = 25000L, // زيادة سعر البيع
            colors = listOf(
                ProductColorVariant(
                    colorId = initialModel.colors.first().colorId,
                    colorName = "ذهبي ملكي",
                    sizes = listOf(SizeStock("M", 12L)) // تعديل كمية المخزون من 7 إلى 12 (+5 قطع)
                )
            )
        )

        val movement = com.example.data.local.entity.StockMovementEntity(
            productId = initialModel.id,
            colorId = initialModel.colors.first().colorId,
            sizeLabel = "M",
            movementType = "ADJUSTMENT",
            quantityDelta = +5L,
            reason = "تعديل جردي بالموديل (+5)",
            dateTimeFormatted = "2026/11/09"
        )

        vm.updateProductFull(editedModel, listOf(movement))

        val stateAfterEdit = vm.uiState.value

        // 1. التحقق من أن الفاتورة السابقة لم تتغير قيمتها أو ربحها أو تكلفة أصنافها
        val savedInvoice = stateAfterEdit.salesInvoices.first()
        assertEquals(3 * 18000L, savedInvoice.totalAmountIqd)
        assertEquals(3 * 10000L, savedInvoice.cogsTotalIqd)
        assertEquals(24000L, savedInvoice.grossProfitIqd)

        // 2. التحقق من أن تكلفة دفعة الشراء التاريخية الأصلية لم تتغير وبقيت 10,000 د.ع
        val originalBatch = stateAfterEdit.purchaseBatches.first()
        assertEquals(10000L, originalBatch.unitCostIqd)

        // 3. التحقق من تسجيل حركة التعديل الجردي للمخزون
        assertTrue(stateAfterEdit.stockMovements.any { it.movementType == "ADJUSTMENT" && it.quantityDelta == 5L })

        // 4. التحقق من تحديث الكمية في الموديل
        assertEquals(12L, stateAfterEdit.products.first().totalPieces)
    }
}

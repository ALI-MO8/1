package com.example.data.engine

import com.example.data.model.PurchaseBatch

data class FifoDeductionResult(
    val cogsIqd: Long,
    val updatedBatches: List<PurchaseBatch>,
    val consumedDetails: List<FifoBatchConsumption>
)

data class FifoBatchConsumption(
    val batchId: String,
    val quantityTaken: Long,
    val unitCostIqd: Long
)

object FifoInventoryEngine {

    /**
     * حساب واستهلاك المخزون بنظام الوارد أولاً يُصرف أولاً (FIFO)
     * يتم استهلاك الكميات من أقدم دفعات التوريد المتبقية مع حفظ تكاليف كل دفعة تاريخية دون أي تعديل.
     */
    fun processFifoDeduction(
        productId: String,
        colorId: String,
        sizeLabel: String,
        quantityToSell: Long,
        allBatches: List<PurchaseBatch>
    ): FifoDeductionResult {
        require(quantityToSell > 0) { "كمية البيع يجب أن تكون أكبر من صفر" }

        // تصفية وترتيب الدفعات الخاصة بهذه التوليفة (موديل + لون + قياس) تصاعدياً حسب الأقدم
        val matchingBatches = allBatches
            .filter { it.productId == productId && it.colorId == colorId && it.sizeLabel == sizeLabel && it.remainingQty > 0 }
            .sortedBy { it.timestamp }

        val totalAvailable = matchingBatches.sumOf { it.remainingQty }
        if (quantityToSell > totalAvailable) {
            throw IllegalStateException("الكمية المطلوبة ($quantityToSell) غير متوفرة في المخزون (المتبقي: $totalAvailable)")
        }

        var needed = quantityToSell
        var totalCogs = 0L
        val consumptions = mutableListOf<FifoBatchConsumption>()
        val updatedMap = mutableMapOf<String, PurchaseBatch>()

        for (batch in matchingBatches) {
            if (needed <= 0) break

            val take = minOf(batch.remainingQty, needed)
            val cost = take * batch.unitCostIqd
            totalCogs += cost
            needed -= take

            consumptions.add(
                FifoBatchConsumption(
                    batchId = batch.batchId,
                    quantityTaken = take,
                    unitCostIqd = batch.unitCostIqd
                )
            )

            updatedMap[batch.batchId] = batch.copy(
                remainingQty = batch.remainingQty - take
            )
        }

        // دمج التحديثات مع باقي الدفعات
        val finalBatches = allBatches.map { original ->
            updatedMap[original.batchId] ?: original
        }

        return FifoDeductionResult(
            cogsIqd = totalCogs,
            updatedBatches = finalBatches,
            consumedDetails = consumptions
        )
    }

    /**
     * احتساب القيمة الإجمالية للمخزون المتبقي بسعر التكلفة الفعلي لكل دفعة
     */
    fun calculateTotalInventoryValuationAtCost(batches: List<PurchaseBatch>): Long {
        return batches.filter { it.remainingQty > 0 }.sumOf { it.remainingQty * it.unitCostIqd }
    }
}

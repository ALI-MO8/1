package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        ProductEntity::class,
        ColorVariantEntity::class,
        SizeStockEntity::class,
        PurchaseBatchEntity::class,
        SaleInvoiceEntity::class,
        SoldItemDetailEntity::class,
        ExpenseEntity::class,
        StockMovementEntity::class,
        BackupMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun storeDao(): StoreDao

    companion object {
        const val DATABASE_NAME = "ebra_khait_store.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

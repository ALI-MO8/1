package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationDestination(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    DASHBOARD("الرئيسية", Icons.Default.Dashboard, "dashboard"),
    PRODUCTS("المنتجات", Icons.Default.Checkroom, "products"),
    PURCHASES("المشتريات", Icons.Default.Inventory, "purchases"),
    SALES("المبيعات", Icons.Default.PointOfSale, "sales"),
    EXPENSES("المصاريف", Icons.Default.ReceiptLong, "expenses"),
    REPORTS("التقارير", Icons.Default.Assessment, "reports")
}

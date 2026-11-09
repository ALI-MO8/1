package com.example.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppEmptyState
import com.example.ui.components.formatIqd
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepPlumPrimary
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.SoftBeige
import com.example.ui.theme.SoftRoseLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VelvetRoseError
import com.example.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by storeViewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val categoryPresets = listOf("إيجار المحل", "كهرباء", "نقل وتوصيل", "إعلانات ودعاية", "أكياس ومطبوعات", "مصاريف أخرى")

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("expenses_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("تسجيل مصروف جديد", fontWeight = FontWeight.Bold) },
                containerColor = DeepPlumPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_expense_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(IvoryBackground)
        ) {
            val totalExpense = uiState.expenses.sumOf { it.amountIqd }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المصاريف التشغيلية للمحل",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DeepPlumPrimary
                    )
                    Text(
                        text = "تُخصم تلقائياً من الربح الإجمالي لاحتساب صافي الأرباح",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = VelvetRoseError.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "الإجمالي: ${formatIqd(totalExpense)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = VelvetRoseError,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (uiState.expenses.isEmpty()) {
                AppEmptyState(
                    icon = Icons.Default.ReceiptLong,
                    title = "لا توجد مصاريف مسجلة",
                    description = "سجلي الإيجارات وفواتير الكهرباء والنقل والإعلانات لضبط الأرباح المحاسبية الدقيقة للمحل.",
                    actionLabel = "تسجيل أول مصروف",
                    onActionClick = { showDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.expenses, key = { it.expenseId }) { expense ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("expense_item_${expense.expenseId}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SoftRoseLight,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AttachMoney,
                                                contentDescription = null,
                                                tint = DeepPlumPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = expense.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = DeepPlumPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = SoftBeige) {
                                                Text(
                                                    text = expense.category,
                                                    fontSize = 11.sp,
                                                    color = TextMuted,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(text = "• ${expense.date}", fontSize = 11.sp, color = TextMuted)
                                        }
                                        if (expense.notes.isNotBlank()) {
                                            Text(text = expense.notes, fontSize = 11.sp, color = TextMuted)
                                        }
                                    }
                                }

                                Text(
                                    text = formatIqd(expense.amountIqd),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = VelvetRoseError
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf(categoryPresets.first()) }
        var amountText by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var categoryMenuExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("تسجيل مصروف تشغيلي جديد", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // بيان المصروف
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("بيان المصروف *") },
                        placeholder = { Text("مثال: إيجار شهر تشرين الثاني، شحن بضاعة...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                    )

                    // التصنيف
                    ExposedDropdownMenuBox(
                        expanded = categoryMenuExpanded,
                        onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("التصنيف") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            categoryPresets.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // المبلغ بالدينار
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) amountText = it },
                        label = { Text("المبلغ بالدينار العراقي (د.ع) *") },
                        placeholder = { Text("50000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                    )

                    // ملاحظات اختيارية
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val amount = amountText.toLongOrNull() ?: 0L
                Button(
                    onClick = {
                        if (title.isNotBlank() && amount > 0L) {
                            storeViewModel.addExpense(title, selectedCategory, amount, notes)
                            showDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && amount > 0L,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlumPrimary),
                    modifier = Modifier.testTag("save_expense_button")
                ) {
                    Text("حفظ المصروف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

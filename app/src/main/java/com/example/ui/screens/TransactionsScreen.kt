package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.data.model.Subcontractor
import com.example.data.model.Transaction
import com.example.ui.viewmodel.ContractingViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(
    viewModel: ContractingViewModel,
    modifier: Modifier = Modifier,
    initialAddDialogOpen: Boolean = false,
    onAddDialogClosed: () -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val subcontractors by viewModel.subcontractors.collectAsState()

    var showAddDialog by remember { mutableStateOf(initialAddDialogOpen) }
    
    // فلاتر التصفية الحالية
    var selectedProjectFilter by remember { mutableStateOf<Int?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) } // null for all, "مصروف", "إيراد"
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // تطبيق فلاتر التصفية ديناميكياً
    val filteredTransactions = remember(transactions, selectedProjectFilter, selectedTypeFilter, selectedCategoryFilter) {
        transactions.filter { tx ->
            val matchProject = selectedProjectFilter == null || tx.projectId == selectedProjectFilter
            val matchType = selectedTypeFilter == null || tx.type == selectedTypeFilter
            val matchCategory = selectedCategoryFilter == null || tx.category == selectedCategoryFilter
            matchProject && matchType && matchCategory
        }
    }

    LaunchedEffect(initialAddDialogOpen) {
        if (initialAddDialogOpen) {
            showAddDialog = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // صف فلاتر الفئات الإنشائية والتوعية
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // فلتر المشاريع كشريط أفقي ميكانيكي
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedProjectFilter == null,
                            onClick = { selectedProjectFilter = null },
                            label = { Text("جميع المشاريع") }
                        )
                    }
                    items(projects) { proj ->
                        FilterChip(
                            selected = selectedProjectFilter == proj.id,
                            onClick = { selectedProjectFilter = proj.id },
                            label = { Text(proj.name) }
                        )
                    }
                }

                // فلتر نوع التداول المالي (إيراد أو مصروف)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("الكل", "إيراد", "مصروف").forEach { typeLabel ->
                        val actualValue = if (typeLabel == "الكل") null else typeLabel
                        val selected = selectedTypeFilter == actualValue
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .clickable { selectedTypeFilter = actualValue },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // فلتر تصنيف وبنود الصرف والتشغيل
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        InputChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("جميع البنود") }
                        )
                    }
                    val expenseCatsByOrder = listOf("مواد", "أجور عمالة", "مقاول باطن", "معدات", "مصروف عمومي وإداري", "مستخلصات عملاء")
                    items(expenseCatsByOrder) { cat ->
                        InputChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            // جدول أو قائمة المعاملات المصفاة
            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterAltOff,
                            contentDescription = "لا توجد معاملات مصفاة",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد معاملات طابقت فلاتر التصفية المحددة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions) { tx ->
                        val project = projects.find { it.id == tx.projectId }
                        val projectName = project?.name ?: "مشروع غير معروف"
                        
                        TransactionItemWithDelete(
                            transaction = tx,
                            projectName = projectName,
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }

        // زر إضافة حركة مالية جديدة عائم
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.AddCard, contentDescription = "تسجيل حركة مالية")
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            projects = projects,
            subcontractors = subcontractors,
            onDismiss = {
                showAddDialog = false
                onAddDialogClosed()
            },
            onConfirm = { projectId, subcontractorId, amount, type, category, description, paymentMethod ->
                viewModel.addTransaction(projectId, subcontractorId, amount, type, category, description, paymentMethod)
                showAddDialog = false
                onAddDialogClosed()
            }
        )
    }
}

@Composable
fun TransactionItemWithDelete(
    transaction: Transaction,
    projectName: String,
    onDelete: () -> Unit
) {
    val df = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val formattedDate = remember(transaction.date) { df.format(Date(transaction.date)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (transaction.type == "إيراد") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (transaction.type == "إيراد") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = transaction.type,
                            tint = if (transaction.type == "إيراد") Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$projectName • $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (transaction.type == "إيراد") "+" else "-"} ${String.format("%,.1f", transaction.amount)} ر.س",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (transaction.type == "إيراد") Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = transaction.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف القيد",
                            tint = Color(0xFFC62828).copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // تفاصيل الدفع (نقدي/شيك) إذا وجدت
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = "الدفع", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                    Text(
                        text = "طريقة الدفع: ${transaction.paymentMethod}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    projects: List<Project>,
    subcontractors: List<Subcontractor>,
    onDismiss: () -> Unit,
    onConfirm: (projectId: Int, subcontractorId: Int?, amount: Double, type: String, category: String, description: String, paymentMethod: String) -> Unit
) {
    if (projects.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("تنبيه تأسيسي") },
            text = { Text("يرجى إضافة مشروع واحد على الأقل لتستطيع تسجيل حركة محاسبية مالية.") },
            confirmButton = {
                Button(onClick = onDismiss) { Text("حسناً") }
            }
        )
        return
    }

    var selectedProjectId by remember { mutableStateOf(projects.first().id) }
    var selectedSubcontractorId by remember { mutableStateOf<Int?>(null) }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("مصروف") } // مصروف، إيراد
    var category by remember { mutableStateOf("مواد") } // بنود المصروفات
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("تحويل بنكي") } // نقدي، شيك، تحويل بنكي

    val expenseCategories = listOf("مواد", "أجور عمالة", "مقاول باطن", "معدات", "مصروف عمومي وإداري")
    val revenueCategories = listOf("مستخلصات عملاء", "تمويلات طارئة")

    val isFormValid = amount.toDoubleOrNull() != null && description.isNotBlank()

    // تحديث التصنيف التلقائي بناء على نوع الحركة
    LaunchedEffect(type) {
        category = if (type == "إيراد") revenueCategories.first() else expenseCategories.first()
        if (type == "إيراد") {
            selectedSubcontractorId = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل قيد مالي / سند رسمي",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // نوع الحركة: مصروف أو مستخلص إيراد
                item {
                    Text("نوع السند المالي:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("مصروف", "إيراد").forEach { opt ->
                            val active = type == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) (if (opt == "إيراد") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .clickable { type = opt },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) (if (opt == "إيراد") Color(0xFF2E7D32) else Color(0xFFC62828)) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // المشروع المنسوب له الحركة
                item {
                    Text("انسب الحركة لمشروع:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(projects) { p ->
                            val s = selectedProjectId == p.id
                            ElevatedFilterChip(
                                selected = s,
                                onClick = { selectedProjectId = p.id },
                                label = { Text(p.name) }
                            )
                        }
                    }
                }

                // اختيار بند الصرف
                item {
                    Text("بند التصنيف والمحاسبة:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val currentList = if (type == "إيراد") revenueCategories else expenseCategories
                        items(currentList) { cat ->
                            val s = category == cat
                            FilterChip(
                                selected = s,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                // خيار إضافي: تحديد مقاول باطن إذا كانت الفئة "مقاول باطن"
                if (type == "مصروف" && category == "مقاول باطن" && subcontractors.isNotEmpty()) {
                    item {
                        Text("ربط بدفعة مقاول باطن مخصص:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedSubcontractorId == null,
                                    onClick = { selectedSubcontractorId = null },
                                    label = { Text("بدون تحديد") }
                                )
                            }
                            items(subcontractors) { sub ->
                                val s = selectedSubcontractorId == sub.id
                                FilterChip(
                                    selected = s,
                                    onClick = { selectedSubcontractorId = sub.id },
                                    label = { Text(sub.name) }
                                )
                            }
                        }
                    }
                }

                // المبلغ
                item {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("المبلغ (ر.س)") },
                        placeholder = { Text("0.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // طريقة دفع الحوالة
                item {
                    Text("طريقة التحويل / الدفع المالي:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("تحويل بنكي", "نقدي", "شيك").forEach { m ->
                            val s = paymentMethod == m
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (s) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .clickable { paymentMethod = m },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = m,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (s) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // البيان / تفاصيل السند
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("تفاصيل وبيان تفصيلي للسند") },
                        placeholder = { Text("مثال: دفعة توريد خرسانة القواعد المسلحة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtDouble = amount.toDoubleOrNull() ?: 0.0
                    onConfirm(selectedProjectId, selectedSubcontractorId, amtDouble, type, category, description, paymentMethod)
                },
                enabled = isFormValid
            ) {
                Text("حفظ السند")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

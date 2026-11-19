package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.Transaction
import com.example.ui.components.CircularFinancialIndicator
import com.example.ui.viewmodel.ContractingViewModel

@Composable
fun ProjectsScreen(
    viewModel: ContractingViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (selectedProject != null) {
        // عرض تفاصيل المشروع المحدد
        val projectTransactions = remember(transactions, selectedProject) {
            transactions.filter { it.projectId == selectedProject!!.id }
        }
        ProjectDetailsView(
            project = selectedProject!!,
            transactions = projectTransactions,
            onBack = { viewModel.selectProject(null) },
            modifier = modifier,
            onDelete = {
                viewModel.deleteProject(selectedProject!!)
            }
        )
    } else {
        // عرض قائمة المشاريع
        Box(modifier = modifier.fillMaxSize()) {
            if (projects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "لا توجد مشاريع",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لم يتم إضافة أي مشاريع هندسية بعد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("أضف أول مشروع الآن")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
                ) {
                    items(projects) { project ->
                        val projectTxs = transactions.filter { it.projectId == project.id }
                        val totalSpent = projectTxs.filter { it.type == "مصروف" }.sumOf { it.amount }
                        val totalReceived = projectTxs.filter { it.type == "إيراد" }.sumOf { it.amount }

                        ProjectCard(
                            project = project,
                            totalSpent = totalSpent,
                            totalReceived = totalReceived,
                            onClick = { viewModel.selectProject(project) }
                        )
                    }
                }

                // زر إضافة مشروع عائم
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "مشروع جديد")
                }
            }
        }
    }

    // ديالوج إضافة مشروع جديد
    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, location, budget, clientName, contractValue, status ->
                viewModel.addProject(name, location, budget, clientName, contractValue, status)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProjectCard(
    project: Project,
    totalSpent: Double,
    totalReceived: Double,
    onClick: () -> Unit
) {
    val progressRatio = if (project.budget > 0) (totalSpent / project.budget).toFloat() else 0f
    val isOverBudget = totalSpent > project.budget

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // حالة العمل كتاج ملون
                Box(
                    modifier = Modifier
                        .background(
                            when(project.status) {
                                "نشط" -> Color(0xFFE8F5E9)
                                "مكتمل" -> Color(0xFFE3F2FD)
                                else -> Color(0xFFECEFF1)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = project.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = when(project.status) {
                            "نشط" -> Color(0xFF2E7D32)
                            "مكتمل" -> Color(0xFF1565C0)
                            else -> Color(0xFF37474F)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "الموقع", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                Text(
                    text = project.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // خلاصة مالية بسيطة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("قيمة عقد العميل", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${String.format("%,.0f", project.contractValue)} ر.س", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("المصروف الفعلي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "${String.format("%,.0f", totalSpent)} ر.س",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isOverBudget) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // شريط قياس الميزانية
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ميزانية المواد والصرف: ${String.format("%,.0f", project.budget)} ر.س",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${(progressRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverBudget) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progressRatio.coerceIn(0f, 1f),
                    color = if (isOverBudget) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
fun ProjectDetailsView(
    project: Project,
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSpent = transactions.filter { it.type == "مصروف" }.sumOf { it.amount }
    val totalReceived = transactions.filter { it.type == "إيراد" }.sumOf { it.amount }
    val netMargin = totalReceived - totalSpent
    val isOverBudget = totalSpent > project.budget

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // شريط العنوان العلوي مع زر الرجوع
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف المشروع", tint = Color(0xFFC62828))
                }
            }
        }

        // تفاصيل بطاقة الموازنة ومركز التكلفة للمشروع الحالي
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("العميل المالك", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(project.clientName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("الموقع الهندسي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(project.location, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // ميزانية العقد الكلية
                            FinanceItemRow(
                                title = "إجمالي قيمة العقد:",
                                value = "${String.format("%,.0f", project.contractValue)} ر.س",
                                dotColor = Color(0xFF2196F3)
                            )
                            // الموازنة الداخلية للتكلفة
                            FinanceItemRow(
                                title = "الموازنة الداخلية المقررة:",
                                value = "${String.format("%,.0f", project.budget)} ر.س",
                                dotColor = MaterialTheme.colorScheme.outline
                            )
                            // المبالغ الطارئة المستلمة
                            FinanceItemRow(
                                title = "المستخلصات المحصلة:",
                                value = "${String.format("%,.0f", totalReceived)} ر.س",
                                dotColor = Color(0xFF4CAF50)
                            )
                            // التكلفة الفعلية المنصرفة
                            FinanceItemRow(
                                title = "التكلفة الإجمالية المنصرفة:",
                                value = "${String.format("%,.0f", totalSpent)} ر.س",
                                dotColor = if (isOverBudget) Color(0xFFC62828) else Color(0xFFFF9800)
                            )
                        }

                        // الرسمة الدائرية للموازنة المستهلكة
                        CircularFinancialIndicator(
                            revenue = project.budget,
                            expenses = totalSpent,
                            modifier = Modifier.size(110.dp)
                        )
                    }

                    if (isOverBudget) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "تحذير", tint = Color(0xFFC62828))
                                Text(
                                    text = "لقد تجاوزت التكلفة الإنشائية الموازنة المقررة!",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }

        // تفاصيل القيود المالية لهذا المشروع
        item {
            Text(
                text = "سجل الإيرادات والمنصرفات الخاصة بالمشروع",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد بعد أي قيود مالية منسوبة لهذا المشروع",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            items(transactions) { tx ->
                TransactionCompactItem(transaction = tx, projectName = project.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FinanceItemRow(title: String, value: String, dotColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, location: String, budget: Double, clientName: String, contractValue: Double, status: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var contractValue by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("نشط") }

    val isFormValid = name.isNotBlank() && location.isNotBlank() && budget.toDoubleOrNull() != null && clientName.isNotBlank() && contractValue.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل مشروع إنشائي جديد",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المشروع") },
                        placeholder = { Text("مثال: عمارة الرشيد") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("اسم العميل (المالك)") },
                        placeholder = { Text("مثال: شركة التطوير العقاري") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("مقر وموقع العمل") },
                        placeholder = { Text("مثال: الرياض - حي النرجس") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = contractValue,
                        onValueChange = { contractValue = it },
                        label = { Text("قيمة العقد الكلية مع المالك") },
                        placeholder = { Text("0.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = { Text("الموازنة والمصاريف التقديرية") },
                        placeholder = { Text("0.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Text("حالة المشروع الحالية:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("نشط", "مكتمل", "متوقف").forEach { currentStatus ->
                            val selected = status == currentStatus
                            ElevatedFilterChip(
                                selected = selected,
                                onClick = { status = currentStatus },
                                label = { Text(currentStatus) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budgetDb = budget.toDoubleOrNull() ?: 0.0
                    val contractValueDb = contractValue.toDoubleOrNull() ?: 0.0
                    onConfirm(name, location, budgetDb, clientName, contractValueDb, status)
                },
                enabled = isFormValid
            ) {
                Text("حفظ المشروع")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.Subcontractor
import com.example.data.model.Transaction
import com.example.ui.viewmodel.ContractingViewModel

@Composable
fun SubcontractorsScreen(
    viewModel: ContractingViewModel,
    modifier: Modifier = Modifier
) {
    val subcontractors by viewModel.subcontractors.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val selectedSubcontractor by viewModel.selectedSubcontractor.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (selectedSubcontractor != null) {
        val contractorPayments = remember(transactions, selectedSubcontractor) {
            transactions.filter { it.subcontractorId == selectedSubcontractor!!.id }
        }
        SubcontractorDetailsView(
            subcontractor = selectedSubcontractor!!,
            payments = contractorPayments,
            projectsList = projects,
            onBack = { viewModel.selectSubcontractor(null) },
            onDelete = {
                viewModel.deleteSubcontractor(selectedSubcontractor!!)
            },
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            if (subcontractors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "لا يوجد مقاولين",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لم يتم تعميد أو تسجيل مقاولي باطن بعد في النظام",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "إضافة")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسجيل أول مقاول باطن")
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
                    items(subcontractors) { subcon ->
                        val subconPayments = transactions.filter { it.subcontractorId == subcon.id }
                        val totalPaid = subconPayments.filter { it.type == "مصروف" }.sumOf { it.amount }

                        SubcontractorCard(
                            subcontractor = subcon,
                            totalPaid = totalPaid,
                            onClick = { viewModel.selectSubcontractor(subcon) }
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "تسجيل مقاول جديد")
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubcontractorDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, trade, phone, totalContractValue, notes ->
                viewModel.addSubcontractor(name, trade, phone, totalContractValue, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SubcontractorCard(
    subcontractor: Subcontractor,
    totalPaid: Double,
    onClick: () -> Unit
) {
    val remaining = subcontractor.totalContractValue - totalPaid
    val hasUnpaidBalance = remaining > 0

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subcontractor.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subcontractor.trade,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "اتصال",
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = subcontractor.phone,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // البيانات المالية
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("إجمالي قيمة العقد مع الباطن", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${String.format("%,.0f", subcontractor.totalContractValue)} ر.س", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("إجمالي ما تم صرفه له", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${String.format("%,.0f", totalPaid)} ر.س", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF4CAF50))
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("الرصيد المتبقي له", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "${String.format("%,.0f", Math.max(0.0, remaining))} ر.س",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (hasUnpaidBalance) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SubcontractorDetailsView(
    subcontractor: Subcontractor,
    payments: List<Transaction>,
    projectsList: List<com.example.data.model.Project>,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPaid = payments.filter { it.type == "مصروف" }.sumOf { it.amount }
    val remaining = subcontractor.totalContractValue - totalPaid

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // شريط الرأس والرجوع
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subcontractor.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = subcontractor.trade,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف مقاول باطن", tint = Color(0xFFC62828))
                }
            }
        }

        // لوحة الرصيد وكشف حساب مقاول الباطن
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("رقم التواصل", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(subcontractor.phone, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        if (subcontractor.notes.isNotBlank()) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text("ملاحظات وشروط التعاقد", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(subcontractor.notes, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("إجمالي العقد المتفق عليه", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${String.format("%,.0f", subcontractor.totalContractValue)} ر.س", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("إجمالي الدفعات المسلمة له", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${String.format("%,.0f", totalPaid)} ر.س", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("صافي المستحقات المتبقية في ذمته", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${String.format("%,.0f", Math.max(0.0, remaining))} ر.س",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (remaining > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (remaining <= 0) Color(0xFFE8F5E9) else Color(0xFFFFFDE7),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (remaining <= 0) "تمت التصفية" else "تحت الحساب",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (remaining <= 0) Color(0xFF2E7D32) else Color(0xFFF57F17)
                            )
                        }
                    }
                }
            }
        }

        // قائمة كشف حساب الدفعات التاريخية
        item {
            Text(
                text = "كشف حساب الدفعات وحوالات المصروف لمقاول الباطن",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (payments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لم يتم تحويل أو صرف أي دفعات أو تعميدات مالية له بعد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            items(payments) { tx ->
                val project = projectsList.find { it.id == tx.projectId }
                val projName = project?.name ?: "مشروع مجهول"
                TransactionCompactItem(transaction = tx, projectName = projName)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AddSubcontractorDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, trade: String, phone: String, contract: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var trade by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var totalContractValue by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isFormValid = name.isNotBlank() && trade.isNotBlank() && phone.isNotBlank() && totalContractValue.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل ملف تعاقد مقاول باطن",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المقاول / المؤسسة") },
                    placeholder = { Text("مثال: مؤسسة النور للتكييف") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = trade,
                    onValueChange = { trade = it },
                    label = { Text("التخصص / طبيعة العمل") },
                    placeholder = { Text("مثال: تركيب لوحات، تمديد مبردات") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم جوال المقاول") },
                    placeholder = { Text("05xxxxxxxx") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = totalContractValue,
                    onValueChange = { totalContractValue = it },
                    label = { Text("إجمالي ميزانية اتفاقية التعاقد") },
                    placeholder = { Text("0.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات وشروط الدفع") },
                    placeholder = { Text("مثال: دفعات على ٤ مراحل حسب الإنجاز") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val contractDb = totalContractValue.toDoubleOrNull() ?: 0.0
                    onConfirm(name, trade, phone, contractDb, notes)
                },
                enabled = isFormValid
            ) {
                Text("تسجيل وحفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

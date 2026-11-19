package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * يمثل المشروع الإنشائي (مركز التكلفة الرئيسي)
 */
@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val location: String,
    val budget: Double,
    val clientName: String,
    val contractValue: Double,
    val status: String, // نشط، مكتمل، متوقف
    val startDate: Long = System.currentTimeMillis()
)

/**
 * يمثل مقاول الباطن أو المجمع الذي يتم التعاقد معه لتنفيذ أعمال محددة
 */
@Entity(tableName = "subcontractors")
data class Subcontractor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val trade: String, // التخصص (مثال: أعمال الخرسانة، السباكة، اللياسة، الكهرباء)
    val phone: String,
    val totalContractValue: Double, // إجمالي قيمة العقد المتفق عليه مع مقاول الباطن
    val notes: String
)

/**
 * يمثل الحركة المالية (مصروف أو قيد إيراد أو مستخلص أو دفعة لمقاول باطن)
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int, // المشروع المرتبط به الحركة
    val subcontractorId: Int? = null, // مقاول الباطن المرتبط بالحركة (إن وجد كدفعة له)
    val amount: Double, // المبلغ
    val type: String, // "مصروف" أو "إيراد" (مستخلص عميل)
    val category: String, // مواد، أجور عمالة، مقاول باطن، معدات، مصروف عمومي وإداري
    val date: Long = System.currentTimeMillis(),
    val description: String, // تفاصيل الحركة (البيان)
    val paymentMethod: String // نقدي، شيك، تحويل بنكي
)

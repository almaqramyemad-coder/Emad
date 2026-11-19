package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.model.Project
import com.example.data.model.Subcontractor
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainScreen {
    DASHBOARD,
    PROJECTS,
    SUBCONTRACTORS,
    TRANSACTIONS
}

class ContractingViewModel(private val repository: AppRepository) : ViewModel() {

    // التنقل الحالي في التطبيق
    private val _currentScreen = MutableStateFlow(MainScreen.DASHBOARD)
    val currentScreen: StateFlow<MainScreen> = _currentScreen.asStateFlow()

    // المشروع المحدد لمشاهدة التفاصيل المتقدمة
    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    // مقاول الباطن المحدد لمشاهدة كشف الحساب والدفعات
    private val _selectedSubcontractor = MutableStateFlow<Subcontractor?>(null)
    val selectedSubcontractor: StateFlow<Subcontractor?> = _selectedSubcontractor.asStateFlow()

    // الاستماع لقوائم القواعد البيانية بشكل تفاعلي
    val projects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subcontractors: StateFlow<List<Subcontractor>> = repository.allSubcontractors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // الإحصائيات المحسوبة تلقائياً للفاتورة الكلية والتفصيلية للمشروع
    val globalStats = combine(projects, subcontractors, transactions) { projs, subcons, txs ->
        val totalRevenue = txs.filter { it.type == "إيراد" }.sumOf { it.amount }
        val totalExpenses = txs.filter { it.type == "مصروف" }.sumOf { it.amount }
        
        // حساب ذمم ومستحقات مقاولي الباطن
        // إجمالي قيمة عقود مقاولي الباطن
        val totalSubcontractsValue = subcons.sumOf { it.totalContractValue }
        // إجمالي ما تم دفعه لمقاولي الباطن (مصروف من فئة مقاول باطن)
        val totalPaidToSubcontractors = txs.filter { it.type == "مصروف" && it.subcontractorId != null }.sumOf { it.amount }
        val remainingSubcontractorClaims = totalSubcontractsValue - totalPaidToSubcontractors

        val totalActiveProjects = projs.filter { it.status == "نشط" }.size

        // تقسيم المصاريف حسب الفئات
        val expensesByCategory = txs.filter { it.type == "مصروف" }
            .groupBy { it.category }
            .mapValues { (_, txList) -> txList.sumOf { it.amount } }

        GlobalStats(
            totalRevenue = totalRevenue,
            totalExpenses = totalExpenses,
            netProfit = totalRevenue - totalExpenses,
            subcontractorsRemaining = Math.max(0.0, remainingSubcontractorClaims),
            activeProjectsCount = totalActiveProjects,
            expensesByCategory = expensesByCategory
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlobalStats())

    init {
        // فحص بذر البيانات عند التحميل الأول ليكون للتطبيق مظهر مالي تفاعلي
        viewModelScope.launch {
            projects.take(2).collect { list ->
                if (list.isEmpty()) {
                    seedInitialData()
                }
            }
        }
    }

    fun selectScreen(screen: MainScreen) {
        _currentScreen.value = screen
        // فك التحديد تلقائياً بالعودة
        if (screen != MainScreen.PROJECTS) _selectedProject.value = null
        if (screen != MainScreen.SUBCONTRACTORS) _selectedSubcontractor.value = null
    }

    fun selectProject(project: Project?) {
        _selectedProject.value = project
    }

    fun selectSubcontractor(subcontractor: Subcontractor?) {
        _selectedSubcontractor.value = subcontractor
    }

    // إضافة مشروع جديد
    fun addProject(name: String, location: String, budget: Double, clientName: String, contractValue: Double, status: String) {
        viewModelScope.launch {
            repository.insertProject(
                Project(
                    name = name,
                    location = location,
                    budget = budget,
                    clientName = clientName,
                    contractValue = contractValue,
                    status = status
                )
            )
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            repository.updateProject(project)
            if (_selectedProject.value?.id == project.id) {
                _selectedProject.value = project
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (_selectedProject.value?.id == project.id) {
                _selectedProject.value = null
            }
        }
    }

    // إضافة مقاول باطن جديد
    fun addSubcontractor(name: String, trade: String, phone: String, totalContractValue: Double, notes: String) {
        viewModelScope.launch {
            repository.insertSubcontractor(
                Subcontractor(
                    name = name,
                    trade = trade,
                    phone = phone,
                    totalContractValue = totalContractValue,
                    notes = notes
                )
            )
        }
    }

    fun updateSubcontractor(subcontractor: Subcontractor) {
        viewModelScope.launch {
            repository.updateSubcontractor(subcontractor)
            if (_selectedSubcontractor.value?.id == subcontractor.id) {
                _selectedSubcontractor.value = subcontractor
            }
        }
    }

    fun deleteSubcontractor(subcontractor: Subcontractor) {
        viewModelScope.launch {
            repository.deleteSubcontractor(subcontractor)
            if (_selectedSubcontractor.value?.id == subcontractor.id) {
                _selectedSubcontractor.value = null
            }
        }
    }

    // إضافة حركة مالية (سند مصروف أو إيراد)
    fun addTransaction(projectId: Int, subcontractorId: Int?, amount: Double, type: String, category: String, description: String, paymentMethod: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    projectId = projectId,
                    subcontractorId = subcontractorId,
                    amount = amount,
                    type = type,
                    category = category,
                    description = description,
                    paymentMethod = paymentMethod
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // بذر بيانات تأسيسية افتراضية للمقاولات
    private suspend fun seedInitialData() {
        val p1 = Project(
            name = "فيلا الياسمين السكنية",
            location = "الرياض - حي الملقا",
            budget = 450000.0,
            clientName = "أبو عبد العزيز المزيني",
            contractValue = 600000.0,
            status = "نشط"
        )
        val p2 = Project(
            name = "برج نجد التجاري",
            location = "جدة - حي الصحافة",
            budget = 3200000.0,
            clientName = "شركة نجد للاستثمار العقاري",
            contractValue = 4500000.0,
            status = "نشط"
        )
        val p3 = Project(
            name = "استراحة الحمراء",
            location = "الدمام - بحيرة سيهات",
            budget = 180000.0,
            clientName = "م. فهد الهاشم",
            contractValue = 240000.0,
            status = "مكتمل"
        )

        repository.insertProject(p1)
        repository.insertProject(p2)
        repository.insertProject(p3)

        // مقاولي باطن
        val s1 = Subcontractor(
            name = "مؤسسة الرواف لأعمال الهياكل",
            trade = "أعمال الحفر والخرسانة والحديد",
            phone = "0501122334",
            totalContractValue = 180000.0,
            notes = "مقاول رئيسي للهيكل الإنشائي الأسود"
        )
        val s2 = Subcontractor(
            name = "ورشة الفن الحديث للسباكة",
            trade = "أعمال السباكة والشبكات الحرارية",
            phone = "0559988776",
            totalContractValue = 45000.0,
            notes = "يشمل التأسيس والقطع وتوصيل الخزان العلوي"
        )
        val s3 = Subcontractor(
            name = "الأنوار الساطعة للأعمال الكهربائية",
            trade = "أعمال التأسيسات والتمديد والإنارة",
            phone = "0533445566",
            totalContractValue = 35000.0,
            notes = "مصنعيات كاملة مع توريد علب ولوحات توزيع التأسيس"
        )

        repository.insertSubcontractor(s1)
        repository.insertSubcontractor(s2)
        repository.insertSubcontractor(s3)

        // سنربط المعاملات المالية بالمعرفات 1 و2 (بعد الحفظ، ستكون المعرفات 1 و2 و3 للمشاريع والروابط بالترتيب)
        // إيرادات (مستخلصات مستلمة من العميل)
        repository.insertTransaction(Transaction(projectId = 1, amount = 150000.0, type = "إيراد", category = "مستخلصات عملاء", description = "المستخلص الأول - دفعة الدفان وصك القواعد", paymentMethod = "تحويل بنكي"))
        repository.insertTransaction(Transaction(projectId = 1, amount = 100000.0, type = "إيراد", category = "مستخلصات عملاء", description = "المستخلص الثاني - صبة أعمدة الدور الأرضي والأول", paymentMethod = "تحويل بنكي"))
        repository.insertTransaction(Transaction(projectId = 2, amount = 1200000.0, type = "إيراد", category = "مستخلصات عملاء", description = "مستخلص الدفعة التأسيسية المقدمة للبرج", paymentMethod = "تحويل بنكي"))

        // مصروفات المشروع 1 (فيلا الياسمين)
        repository.insertTransaction(Transaction(projectId = 1, subcontractorId = 1, amount = 45000.0, type = "مصروف", category = "مقاول باطن", description = "دفعة مقدمة لمؤسسة الرواف لتجهيز الموقع والحفر", paymentMethod = "تحويل بنكي"))
        repository.insertTransaction(Transaction(projectId = 1, subcontractorId = 1, amount = 30000.0, type = "مصروف", category = "مقاول باطن", description = "الدفعة الثانية لمؤسسة الرواف عقب صب القواعد", paymentMethod = "شيك"))
        repository.insertTransaction(Transaction(projectId = 1, amount = 25000.0, type = "مصروف", category = "مواد", description = "فاتورة شراء حديد سابك تسليح القواعد", paymentMethod = "تحويل بنكي"))
        repository.insertTransaction(Transaction(projectId = 1, amount = 18000.0, type = "مصروف", category = "مواد", description = "فاتورة خرسانة جاهزة مقاومة للكبريتات - القواعد", paymentMethod = "نقدي"))
        repository.insertTransaction(Transaction(projectId = 1, amount = 8500.0, type = "مصروف", category = "أجور عمالة", description = "يوميات أجور عمال التجهيز والردم والصب", paymentMethod = "نقدي"))

        // مصروفات البرج
        repository.insertTransaction(Transaction(projectId = 2, amount = 145000.0, type = "مصروف", category = "مواد", description = "شراء حديد تسليح ثقيل (أعمدة وجدران القص)", paymentMethod = "تحويل بنكي"))
        repository.insertTransaction(Transaction(projectId = 2, amount = 120000.0, type = "مصروف", category = "معدات", description = "إيجار رافعات ثقيلة وحفارات هيدروليكية لمدة شهر", paymentMethod = "شيك"))
        repository.insertTransaction(Transaction(projectId = 2, amount = 15000.0, type = "مصروف", category = "مصروف عمومي وإداري", description = "تراخيص وبلديات ورسوم تأمين هندسي للموقع", paymentMethod = "تحويل بنكي"))
    }
}

data class GlobalStats(
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val subcontractorsRemaining: Double = 0.0,
    val activeProjectsCount: Int = 0,
    val expensesByCategory: Map<String, Double> = emptyMap()
)

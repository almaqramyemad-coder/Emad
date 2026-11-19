package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SubcontractorsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ContractingViewModel
import com.example.ui.viewmodel.MainScreen
import com.example.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // تفعيل تجربة العرض بدون حواف (Edge-to-Edge)
        enableEdgeToEdge()
        
        // تهيئة قواعد البيانات والمستودع يدوياً طبقاً للمحددات
        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(
            database.projectDao(),
            database.subcontractorDao(),
            database.transactionDao()
        )
        val viewModelFactory = ViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: ContractingViewModel = viewModel(factory = viewModelFactory)
                val currentScreen by viewModel.currentScreen.collectAsState()
                
                // حالة ذكية لفتح حواريات الإضافة السريعة عبر الشاشات المختلفة
                var autoOpenTransactionDialog by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (currentScreen) {
                                        MainScreen.DASHBOARD -> "لوحة تحكم كاش المقاولات"
                                        MainScreen.PROJECTS -> "المشاريع ومراكز التكلفة"
                                        MainScreen.SUBCONTRACTORS -> "مقاولو الباطن والموردون"
                                        MainScreen.TRANSACTIONS -> "اليومية وسندات المعاملات"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == MainScreen.DASHBOARD,
                                onClick = { viewModel.selectScreen(MainScreen.DASHBOARD) },
                                icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                                label = { Text("الرئيسية", style = MaterialTheme.typography.labelSmall) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == MainScreen.PROJECTS,
                                onClick = { viewModel.selectScreen(MainScreen.PROJECTS) },
                                icon = { Icon(Icons.Default.Folder, contentDescription = "المشاريع") },
                                label = { Text("المشاريع", style = MaterialTheme.typography.labelSmall) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == MainScreen.SUBCONTRACTORS,
                                onClick = { viewModel.selectScreen(MainScreen.SUBCONTRACTORS) },
                                icon = { Icon(Icons.Default.People, contentDescription = "المقاولون") },
                                label = { Text("المقاولون", style = MaterialTheme.typography.labelSmall) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == MainScreen.TRANSACTIONS,
                                onClick = { viewModel.selectScreen(MainScreen.TRANSACTIONS) },
                                icon = { Icon(Icons.Default.ListAlt, contentDescription = "السندات") },
                                label = { Text("السندات", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            MainScreen.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onAddTransactionClick = {
                                    viewModel.selectScreen(MainScreen.TRANSACTIONS)
                                    autoOpenTransactionDialog = true
                                },
                                onAddSubcontractorClick = {
                                    viewModel.selectScreen(MainScreen.SUBCONTRACTORS)
                                    // يفتح قائمة المقاولين حيث يسهل عليهم النقر على الزر الرئيسي السريع
                                },
                                onAddProjectClick = {
                                    viewModel.selectScreen(MainScreen.PROJECTS)
                                }
                            )
                            MainScreen.PROJECTS -> ProjectsScreen(
                                viewModel = viewModel
                            )
                            MainScreen.SUBCONTRACTORS -> SubcontractorsScreen(
                                viewModel = viewModel
                            )
                            MainScreen.TRANSACTIONS -> TransactionsScreen(
                                viewModel = viewModel,
                                initialAddDialogOpen = autoOpenTransactionDialog,
                                onAddDialogClosed = { autoOpenTransactionDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

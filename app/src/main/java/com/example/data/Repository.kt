package com.example.data

import com.example.data.dao.ProjectDao
import com.example.data.dao.SubcontractorDao
import com.example.data.dao.TransactionDao
import com.example.data.model.Project
import com.example.data.model.Subcontractor
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val projectDao: ProjectDao,
    private val subcontractorDao: SubcontractorDao,
    private val transactionDao: TransactionDao
) {
    // Projects
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()
    
    fun getProjectById(id: Int): Flow<Project?> = projectDao.getProjectById(id)
    
    suspend fun insertProject(project: Project) = projectDao.insertProject(project)
    
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    
    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)

    // Subcontractors
    val allSubcontractors: Flow<List<Subcontractor>> = subcontractorDao.getAllSubcontractors()
    
    fun getSubcontractorById(id: Int): Flow<Subcontractor?> = subcontractorDao.getSubcontractorById(id)
    
    suspend fun insertSubcontractor(subcontractor: Subcontractor) = subcontractorDao.insertSubcontractor(subcontractor)
    
    suspend fun updateSubcontractor(subcontractor: Subcontractor) = subcontractorDao.updateSubcontractor(subcontractor)
    
    suspend fun deleteSubcontractor(subcontractor: Subcontractor) = subcontractorDao.deleteSubcontractor(subcontractor)

    // Transactions
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    fun getTransactionsForProject(projectId: Int): Flow<List<Transaction>> = transactionDao.getTransactionsForProject(projectId)
    
    fun getTransactionsForSubcontractor(subcontractorId: Int): Flow<List<Transaction>> = transactionDao.getTransactionsForSubcontractor(subcontractorId)
    
    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)
    
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)
    
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)
}

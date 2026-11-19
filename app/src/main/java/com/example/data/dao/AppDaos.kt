package com.example.data.dao

import androidx.room.*
import com.example.data.model.Project
import com.example.data.model.Subcontractor
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY startDate DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: Int): Flow<Project?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)
}

@Dao
interface SubcontractorDao {
    @Query("SELECT * FROM subcontractors ORDER BY name ASC")
    fun getAllSubcontractors(): Flow<List<Subcontractor>>

    @Query("SELECT * FROM subcontractors WHERE id = :id LIMIT 1")
    fun getSubcontractorById(id: Int): Flow<Subcontractor?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubcontractor(subcontractor: Subcontractor)

    @Update
    suspend fun updateSubcontractor(subcontractor: Subcontractor)

    @Delete
    suspend fun deleteSubcontractor(subcontractor: Subcontractor)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE projectId = :projectId ORDER BY date DESC")
    fun getTransactionsForProject(projectId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE subcontractorId = :subcontractorId ORDER BY date DESC")
    fun getTransactionsForSubcontractor(subcontractorId: Int): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}

package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ProjectDao
import com.example.data.dao.SubcontractorDao
import com.example.data.dao.TransactionDao
import com.example.data.model.Project
import com.example.data.model.Subcontractor
import com.example.data.model.Transaction

@Database(
    entities = [Project::class, Subcontractor::class, Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun subcontractorDao(): SubcontractorDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "contracting_accounting_db"
                )
                .fallbackToDestructiveMigration() // safe for rapid development / prototypes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.mediminder.data

import android.content.Context
import android.util.Log
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.DatabaseSeeder
import com.example.mediminder.utils.Constants.ERR_SEEDING_DB

/**
 * Initializes the database if it is empty.
 */
class InitializeDatabase(private val context: Context) {
    suspend fun initDatabase() {
        val database = AppDatabase.getDatabase(context)
        val medicationDao = database.medicationDao()
        val medicationCount = medicationDao.getCount()

        // Seed database with data if it is empty
        if (medicationCount == 0) {
            try {
                val seeder = DatabaseSeeder(context, medicationDao, database.dosageDao(),
                    database.remindersDao(), database.scheduleDao(), database.medicationLogDao()
                )
                seeder.seedDatabase()
            } catch (e: Exception) {
                Log.e(TAG, ERR_SEEDING_DB, e)
                throw e
            }
        }
    }

    companion object {
        private const val TAG = "DatabaseInitializer"
    }
}
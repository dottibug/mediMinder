package com.example.mediminder.data

import android.content.Context
import android.util.Log
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.DatabaseSeeder

// Initialize the database if it is empty
class InitializeDatabase(private val context: Context) {
    suspend fun initDatabase() {
        val database = AppDatabase.getDatabase(context)
        val medicationDao = database.medicationDao()

        // Check if database is empty
        val medicationCount = medicationDao.getCount()

        if (medicationCount == 0) {
            // Seed database with data
            val seeder = DatabaseSeeder(
                context,
                medicationDao,
                database.dosageDao(),
                database.remindersDao(),
                database.scheduleDao(),
                database.medicationLogDao()
            )

            try { seeder.seedDatabase() }
            catch (e: Exception) {
                Log.e("DatabaseInitializer", "Error seeding database", e)
                throw e
            }
        }

    }
}
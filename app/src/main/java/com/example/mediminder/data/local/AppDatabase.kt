package com.example.mediminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao

// https://developer.android.com/training/data-storage/room
@Database(entities = [Medication::class, Dosage::class, Schedules::class, MedicationLogs::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun dosageDao(): DosageDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun medicationLogDao(): MedicationLogDao

    // Build and return a single instance of the AppDatabase (using Kotlin companion object: https://kotlinlang.org/docs/object-declarations.html)
    companion object {
        @Volatile // Make sure the database is always up-to-date and from the main memory (not from cache)
        private var database: AppDatabase? = null

        // Get and return the database instance
        fun getDatabase(context: Context): AppDatabase {
            // Create and return database instance if it doesn't exist yet
            return database ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                database = instance
                instance
            }
        }
    }
}
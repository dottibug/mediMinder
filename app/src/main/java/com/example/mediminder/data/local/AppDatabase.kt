package com.example.mediminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao

// https://developer.android.com/training/data-storage/room
@Database(entities = [Medication::class, Dosage::class, MedReminders::class, Schedules::class, MedicationLogs::class], version = 5)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun dosageDao(): DosageDao
    abstract fun remindersDao(): MedRemindersDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun medicationLogDao(): MedicationLogDao

    // Build and return a single instance of the AppDatabase (using Kotlin companion object: https://kotlinlang.org/docs/object-declarations.html)
    companion object {
        @Volatile // Make sure the database is always up-to-date and from the main memory (not from cache)
        private var database: AppDatabase? = null

        // Add this method
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "medication_database")
                .fallbackToDestructiveMigration() // This will delete the old database
                .build()
        }

        // Modify your existing getDatabase method to use the new buildDatabase method
        fun getDatabase(context: Context): AppDatabase {
            return database ?: synchronized(this) {
                val instance = buildDatabase(context)
                database = instance
                instance
            }
        }
    }
}
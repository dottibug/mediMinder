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

/**
 * Local Room database for this app.
 */
@Database(entities = [Medication::class, Dosage::class, MedReminders::class, Schedules::class, MedicationLogs::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun dosageDao(): DosageDao
    abstract fun remindersDao(): MedRemindersDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun medicationLogDao(): MedicationLogDao

    companion object {
        @Volatile // Make sure the database is always up-to-date and from the main memory (not from cache)
        private var database: AppDatabase? = null

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "medication_database")
                .fallbackToDestructiveMigration() // This will delete the old database
                .build()
        }

        fun getDatabase(context: Context): AppDatabase {
            return database ?: synchronized(this) {
                val instance = buildDatabase(context)
                database = instance
                instance
            }
        }
    }
}
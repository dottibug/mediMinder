package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediminder.data.local.classes.MedReminders

@Dao
interface MedRemindersDao {
    // Insert a reminder entity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: MedReminders): Long

    // Update a reminder entity
    @Update
    suspend fun update(reminder: MedReminders)

    // Get reminders by medication ID
    @Query("SELECT * FROM reminders WHERE medication_id = :medicationId")
    suspend fun getReminderByMedicationId(medicationId: Long): MedReminders?

    // Delete all reminders
    @Query("DELETE FROM reminders")
    suspend fun deleteAll()

    // Delete a reminder entity
    @Delete
    suspend fun delete(reminder: MedReminders)

    // Reset the sequence for the reminders table
    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'reminders'")
    suspend fun resetSequence()
}
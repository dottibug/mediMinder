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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: MedReminders): Long

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()

    @Query("SELECT * FROM reminders WHERE medication_id = :medicationId")
    suspend fun getReminderByMedicationId(medicationId: Long): MedReminders?

    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'reminders'")
    suspend fun resetSequence()

    @Query("SELECT * FROM reminders WHERE medication_id = :medicationId")
    suspend fun getRemindersByMedicationId(medicationId: Long): MedReminders?

    // Update a reminder entity
    @Update
    suspend fun update(reminder: MedReminders)

    // Delete a reminder entity
    @Delete
    suspend fun delete(reminder: MedReminders)

}
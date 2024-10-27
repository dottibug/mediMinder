package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediminder.data.local.classes.MedReminders

@Dao
interface MedRemindersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: MedReminders): Long

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()

}
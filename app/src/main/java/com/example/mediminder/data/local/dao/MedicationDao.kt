package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediminder.data.local.classes.Medication

// Abstraction layer for the medication table in the database
// https://developer.android.com/training/data-storage/room/accessing-data
@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications")
    suspend fun getAll(): List<Medication>

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun getMedicationCount(): Int

    @Query("DELETE FROM medications")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)
}
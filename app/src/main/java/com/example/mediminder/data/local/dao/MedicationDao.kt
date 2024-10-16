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
    fun getAll(): List<Medication>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(medication: Medication)

    @Update
    fun update(medication: Medication)

    @Delete
    fun delete(medication: Medication)
}
package com.example.mediminder.data.local.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mediminder.models.MedicationIcon

// Represents a medication entity (row) in the database medications table
// https://developer.android.com/training/data-storage/room/defining-data
@Entity(tableName = "medications")
data class Medication (
    @PrimaryKey (autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String, // not null
    @ColumnInfo(name= "prescribing_doctor") val prescribingDoctor: String?,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "icon") val icon : MedicationIcon,
    @ColumnInfo(name = "reminder_enabled") val reminderEnabled: Boolean,

    // todo: column for medication information from an API if time to implement that as a feature
    // todo: column for symptoms? Or put that in a separate table if you include symptom tracking as a feature
)
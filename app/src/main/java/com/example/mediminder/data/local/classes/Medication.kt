package com.example.mediminder.data.local.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mediminder.models.MedicationIcon

/**
 * Represents a medication row in the medications table.
 */
@Entity(tableName = "medications")
data class Medication (
    @PrimaryKey (autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name= "prescribing_doctor") val prescribingDoctor: String?,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "icon") val icon : MedicationIcon,
    @ColumnInfo(name = "reminder_enabled") val reminderEnabled: Boolean,
    @ColumnInfo(name = "as_needed") val asNeeded: Boolean = false,
)
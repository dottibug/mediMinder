package com.example.mediminder.data.local.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


// Represents a dosage row in the dosages table (associated with a medication)

@Entity(tableName = "dosages",
    foreignKeys = [ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.CASCADE
        )])
data class Dosage (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "medication_id") val medicationId: Long,
    @ColumnInfo(name = "amount") val amount: String,
    @ColumnInfo(name= "units") val units: String
)
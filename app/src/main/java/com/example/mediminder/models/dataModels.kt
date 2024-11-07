package com.example.mediminder.models

import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.MedicationStatus
import com.example.mediminder.data.local.classes.Schedules
import java.time.LocalDate
import java.time.LocalTime

// TODO add your other models

data class MedicationItem(
    val medication: Medication,
    val dosage: Dosage?,
    val time: LocalTime,
    val status: MedicationStatus,
    val logId: Long
)

data class MedicationWithDetails(
    val medication: Medication,
    val dosage: Dosage?,
    val reminders: MedReminders?,
    val schedule: Schedules?
)

data class MedicationHistory(
    val logs: List<MedicationLogWithDetails>
)

data class MedicationLogWithDetails(
    val id: Long,
    val name: String,
    val dosageAmount: String?,
    val dosageUnits: String?,
    val log: MedicationLogs
)

data class DayLogs(
    val date: LocalDate,
    val logs: List<MedicationLogWithDetails>
)
package com.example.mediminder.utils

import com.example.mediminder.R
import com.example.mediminder.data.local.classes.MedicationStatus

object StatusIconUtil {
    fun getStatusIcon(status: MedicationStatus): Int {
        return when (status) {
            MedicationStatus.PENDING -> R.drawable.pending
            MedicationStatus.TAKEN -> R.drawable.taken
            MedicationStatus.SKIPPED -> R.drawable.skipped
            MedicationStatus.MISSED -> R.drawable.missed
            MedicationStatus.UNSCHEDULED -> R.drawable.unscheduled
        }
    }
}
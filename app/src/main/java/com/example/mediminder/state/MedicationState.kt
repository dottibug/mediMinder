package com.example.mediminder.state

import com.example.mediminder.models.MedicationWithDetails
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Medication state class for holding medication details
 */
class MedicationState {
    val current: MutableStateFlow<MedicationWithDetails?> = MutableStateFlow(null)
    val id: MutableStateFlow<Long> = MutableStateFlow(-1L)
    val name: MutableStateFlow<String> = MutableStateFlow("")
    val asScheduled: MutableStateFlow<Boolean> = MutableStateFlow(true)

    fun setMedicationDetails(medicationDetails: MedicationWithDetails) {
        current.value = medicationDetails
        id.value = medicationDetails.medication.id
        name.value = medicationDetails.medication.name
        asScheduled.value = medicationDetails.medication.asNeeded == false
    }

    fun getCurrent(): MedicationWithDetails? { return current.value }
    fun getId(): Long { return id.value }
    fun getName(): String { return name.value }
    fun getAsScheduled(): Boolean { return asScheduled.value }
}
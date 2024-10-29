package com.example.mediminder.data.local.classes

enum class MedicationStatus {
    PENDING,        // Initial state when created
    TAKEN,          // User marked as taken
    SKIPPED,        // User skipped medication
    MISSED,         // Time passed without action
    UNSCHEDULED     // Unscheduled (as needed) medications
}
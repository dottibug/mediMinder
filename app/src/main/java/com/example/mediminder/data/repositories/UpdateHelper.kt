package com.example.mediminder.data.repositories

import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationData
import com.example.mediminder.models.MedicationIcon
import com.example.mediminder.models.ReminderData
import com.example.mediminder.models.ScheduleData
import com.example.mediminder.utils.AppUtils.getLocalTimeFromPair
import java.time.LocalDate

class UpdateHelper (
    private val medicationDao: MedicationDao,
    private val dosageDao: DosageDao,
    private val remindersDao: MedRemindersDao,
    private val scheduleDao: ScheduleDao,
    private val medicationLogDao: MedicationLogDao
) {

    suspend fun updateMedicationData(
        medicationId: Long,
        medicationData: MedicationData,
        dosageData: DosageData,
        reminderData: ReminderData,
        scheduleData: ScheduleData
    ) {
        try {
            updateMedicationDetails(medicationId, medicationData, reminderData)
            updateDosage(medicationId, dosageData)
            updateReminder(medicationId, reminderData)
            updateSchedule(medicationId, scheduleData)
        } catch (e: Exception) {
            throw Exception("Failed to update medication: ${e.message}", e)
        }
    }

    private suspend fun updateMedicationDetails(
        medicationId: Long, medicationData: MedicationData, reminderData: ReminderData
    ) {
        try {
            medicationDao.update(
                Medication(
                    id = medicationId,
                    name = medicationData.name,
                    prescribingDoctor = medicationData.doctor,
                    notes = medicationData.notes,
                    icon = medicationData.icon ?: MedicationIcon.TABLET,
                    reminderEnabled = reminderData.reminderEnabled
                )
            )
        } catch (e: Exception) {
            throw Exception("Failed to update medication details: ${e.message}", e)
        }
    }


    private suspend fun updateDosage(medicationId: Long, dosageData: DosageData) {
        try {
            // Get the current dosage id
            val currentDosage = dosageDao.getDosageByMedicationId(medicationId)

            dosageDao.update(
                Dosage(
                    id = currentDosage?.id ?: throw(Exception("Error updating dosage: Dosage not found for medication with id $medicationId")),
                    medicationId = medicationId,
                    amount = dosageData.dosageAmount,
                    units = dosageData.dosageUnits
                )
            )
        } catch (e: Exception) {
            throw Exception("Failed to update dosage: ${e.message}", e)
        }
    }

    private suspend fun updateReminder(medicationId: Long, reminderData: ReminderData) {
        try {
            val currentReminder = remindersDao.getReminderByMedicationId(medicationId)

            // Delete existing reminders and return early if reminder is disabled
            if (!reminderData.reminderEnabled) {
                deleteReminders(currentReminder, medicationId)
                return
            }

            if (currentReminder == null) { createNewReminder(medicationId, reminderData) }
            else { updateExistingReminder(currentReminder, medicationId, reminderData) }

        } catch (e: Exception) {
            throw Exception("Failed to update reminder: ${e.message}", e)
        }
    }

    // If reminders are disabled, delete any existing reminders and medication logs
    private suspend fun deleteReminders(currentReminder: MedReminders?, medicationId: Long) {
        currentReminder?.let {
            remindersDao.delete(it)
            medicationLogDao.deleteAllLogsForMedication(medicationId)
        }
    }

    // Create new reminder if none exists
    private suspend fun createNewReminder(medicationId: Long, reminderData: ReminderData) {
        remindersDao.insert(
            MedReminders(
                medicationId = medicationId,
                reminderFrequency = reminderData.reminderFrequency,
                hourlyReminderInterval = reminderData.hourlyReminderInterval,
                hourlyReminderStartTime = getLocalTimeFromPair(
                    reminderData.hourlyReminderStartTime
                ),
                hourlyReminderEndTime = getLocalTimeFromPair(
                    reminderData.hourlyReminderEndTime
                ),
                dailyReminderTimes = reminderData.dailyReminderTimes
                    .map { getLocalTimeFromPair(it)
                        ?: throw Exception("Invalid time pair when trying to create reminder: $it")
                    }
            )
        )
    }

    // Update existing reminder
    private suspend fun updateExistingReminder(
        currentReminder: MedReminders?,
        medicationId: Long,
        reminderData: ReminderData
    ) {
        if (currentReminder != null) {
            remindersDao.update(
                MedReminders(
                    id = currentReminder.id,
                    medicationId = medicationId,
                    reminderFrequency = reminderData.reminderFrequency,
                    hourlyReminderInterval = reminderData.hourlyReminderInterval,
                    hourlyReminderStartTime = getLocalTimeFromPair(
                        reminderData.hourlyReminderStartTime
                    ),
                    hourlyReminderEndTime = getLocalTimeFromPair(
                        reminderData.hourlyReminderEndTime
                    ),
                    dailyReminderTimes = reminderData.dailyReminderTimes
                        .map { getLocalTimeFromPair(it)
                            ?: throw Exception("Invalid time pair when trying to update reminder: $it")
                        }
                )
            )
        }
    }

    private suspend fun updateSchedule(medicationId: Long, scheduleData: ScheduleData) {
        try {
            // Get the current schedule id
            val currentSchedule = scheduleDao.getScheduleByMedicationId(medicationId)

            scheduleDao.update(
                Schedules(
                    id = currentSchedule?.id ?: throw(Exception("Error updating schedule: Schedule not found for medication with id $medicationId")),
                    medicationId = medicationId,
                    startDate = scheduleData.startDate ?: LocalDate.now(),
                    durationType = scheduleData.durationType,
                    numDays = scheduleData.numDays,
                    scheduleType = scheduleData.scheduleType,
                    selectedDays = scheduleData.selectedDays,
                    daysInterval = scheduleData.daysInterval
                )
            )
        } catch (e: Exception) {
            throw Exception("Failed to update schedule: ${e.message}", e)
        }
    }

}
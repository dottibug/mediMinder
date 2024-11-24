package com.example.mediminder.data.repositories

import android.util.Log
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
import com.example.mediminder.utils.Constants.ERR_INVALID_TIME_PAIR
import com.example.mediminder.utils.Constants.ERR_SCHEDULE_NOT_FOUND
import com.example.mediminder.utils.Constants.ERR_UPDATING_DOSAGE
import com.example.mediminder.utils.Constants.ERR_UPDATING_MED
import com.example.mediminder.utils.Constants.ERR_UPDATING_MED_INFO
import com.example.mediminder.utils.Constants.ERR_UPDATING_REMINDER
import com.example.mediminder.utils.Constants.ERR_UPDATING_SCHEDULE
import java.time.LocalDate

/**
 * Helper class for updating medication data in the database.
 */
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
        dosageData: DosageData?,
        reminderData: ReminderData?,
        scheduleData: ScheduleData?
    ) {
        try {
            updateMedicationDetails(medicationId, medicationData)
            if (dosageData != null) { updateDosage(medicationId, dosageData) }
            if (reminderData != null) { updateReminder(medicationId, reminderData) }
            if (scheduleData != null) { updateSchedule(medicationId, scheduleData) }
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_UPDATING_MED, e)
            throw Exception(ERR_UPDATING_MED, e)
        }
    }

    private suspend fun updateMedicationDetails(
        medicationId: Long,
        medicationData: MedicationData,
    ) {
        try {
            val isScheduled = !medicationData.asNeeded
            medicationDao.update(
                Medication(
                    id = medicationId,
                    name = medicationData.name,
                    prescribingDoctor = medicationData.doctor,
                    notes = medicationData.notes,
                    icon = medicationData.icon ?: MedicationIcon.TABLET,
                    reminderEnabled = isScheduled,  // Always true for scheduled meds
                    asNeeded = medicationData.asNeeded
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_UPDATING_MED_INFO, e)
            throw Exception(ERR_UPDATING_MED_INFO, e)
        }
    }

    private suspend fun updateDosage(medicationId: Long, dosageData: DosageData) {
        try {
            val currentDosage = dosageDao.getDosageByMedicationId(medicationId)

            if (currentDosage == null) {
                // Create new dosage if none exists
                dosageDao.insert(
                    Dosage(
                        medicationId = medicationId,
                        amount = dosageData.dosageAmount,
                        units = dosageData.dosageUnits
                    )
                )
            } else {
                // Update existing dosage
                dosageDao.update(
                    currentDosage.copy(
                        amount = dosageData.dosageAmount,
                        units = dosageData.dosageUnits
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_UPDATING_DOSAGE, e)
            throw Exception(ERR_UPDATING_DOSAGE, e)
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
            Log.e(TAG, e.message ?: ERR_UPDATING_REMINDER, e)
            throw Exception(ERR_UPDATING_REMINDER, e)
        }
    }

    private suspend fun deleteReminders(currentReminder: MedReminders?, medicationId: Long) {
        currentReminder?.let {
            remindersDao.delete(it)
            medicationLogDao.deleteAllLogsForMedication(medicationId)
        }
    }

    /**
     * Creates a new reminder with the given medication ID and reminder data.
     */
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
                    .map { getLocalTimeFromPair(it) ?: throw Exception(ERR_INVALID_TIME_PAIR) }
            )
        )
    }

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
                        .map { getLocalTimeFromPair(it) ?: throw Exception(ERR_INVALID_TIME_PAIR) }
                )
            )
        }
    }

    private suspend fun updateSchedule(medicationId: Long, scheduleData: ScheduleData) {
        try {
            val currentSchedule = scheduleDao.getScheduleByMedicationId(medicationId)

            scheduleDao.update(
                Schedules(
                    id = currentSchedule?.id ?: throw(Exception(ERR_SCHEDULE_NOT_FOUND)),
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
            Log.e(TAG, e.message ?: ERR_UPDATING_SCHEDULE, e)
            throw Exception(ERR_UPDATING_SCHEDULE, e)
        }
    }

    companion object {
        private const val TAG = "UpdateHelper"
    }
}
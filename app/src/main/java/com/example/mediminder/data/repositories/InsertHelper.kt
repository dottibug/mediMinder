package com.example.mediminder.data.repositories

import android.util.Log
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationData
import com.example.mediminder.models.MedicationIcon
import com.example.mediminder.models.ReminderData
import com.example.mediminder.models.ScheduleData
import com.example.mediminder.utils.AppUtils.getLocalTimeFromPair
import com.example.mediminder.utils.Constants.ERR_ADDING_MED
import com.example.mediminder.utils.Constants.ERR_INSERTING_DOSAGE
import com.example.mediminder.utils.Constants.ERR_INSERTING_MED
import com.example.mediminder.utils.Constants.ERR_INSERTING_REMINDER
import com.example.mediminder.utils.Constants.ERR_INSERTING_SCHEDULE
import com.example.mediminder.utils.Constants.ERR_INVALID_TIME_PAIR
import java.time.LocalDate

/**
 * Helper class to insert medication data into the database.
 */
class InsertHelper (
    private val medicationDao: MedicationDao,
    private val dosageDao: DosageDao,
    private val remindersDao: MedRemindersDao,
    private val scheduleDao: ScheduleDao,
) {

    suspend fun addMedicationData(
        medicationData: MedicationData,
        dosageData: DosageData?,
        reminderData: ReminderData?,
        scheduleData: ScheduleData?
    ): Long {
        try {
            val medicationId = insertMedication(medicationData)
            if (dosageData != null) { insertDosage(medicationId, dosageData) }
            if (reminderData != null) { insertReminder(medicationId, reminderData) }
            if (scheduleData != null) { insertSchedule(medicationId, scheduleData) }
            return medicationId
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_ADDING_MED, e)
            throw Exception(ERR_ADDING_MED, e)
        }
    }

    private suspend fun insertMedication(
        medicationData: MedicationData,
    ): Long {
        try {
            val isScheduled = !medicationData.asNeeded
            val medicationId = medicationDao.insert(
                Medication(
                    name = medicationData.name,
                    prescribingDoctor = medicationData.doctor,
                    notes = medicationData.notes,
                    icon = medicationData.icon ?: MedicationIcon.TABLET,
                    reminderEnabled = isScheduled,  // Always true for scheduled meds
                    asNeeded = medicationData.asNeeded
                )
            )
            return medicationId
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_INSERTING_MED, e)
            throw Exception(ERR_INSERTING_MED, e)
        }
    }

    private suspend fun insertDosage(medicationId: Long, dosageData: DosageData) {
        try {
            dosageDao.insert(
                Dosage(
                    medicationId = medicationId,
                    amount = dosageData.dosageAmount,
                    units = dosageData.dosageUnits
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_INSERTING_DOSAGE, e)
            throw Exception(ERR_INSERTING_DOSAGE, e)
        }
    }

    private suspend fun insertReminder(medicationId: Long, reminderData: ReminderData) {
        try {
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
                    dailyReminderTimes = reminderData.dailyReminderTimes.map {
                        getLocalTimeFromPair(it) ?: throw Exception(ERR_INVALID_TIME_PAIR)
                    }
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_INSERTING_REMINDER, e)
            throw Exception(ERR_INSERTING_REMINDER, e)
        }
    }

    private suspend fun insertSchedule(medicationId: Long, scheduleData: ScheduleData) {
        try {
            scheduleDao.insert(
                Schedules(
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
            Log.e(TAG, e.message ?: ERR_INSERTING_SCHEDULE, e)
            throw Exception(ERR_INSERTING_SCHEDULE, e)
        }
    }

    companion object {
        private const val TAG = "InsertHelper"
    }
}
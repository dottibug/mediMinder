package com.example.mediminder.data.repositories
import android.util.Log
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationData
import com.example.mediminder.models.MedicationHistory
import com.example.mediminder.models.MedicationItem
import com.example.mediminder.models.MedicationLogWithDetails
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.models.ReminderData
import com.example.mediminder.models.ScheduleData
import com.example.mediminder.models.ValidatedAsNeededData
import com.example.mediminder.utils.Constants.EMPTY_STRING
import com.example.mediminder.utils.Constants.ERR_ADDING_MED
import com.example.mediminder.utils.Constants.ERR_ADDING_MED_USER
import com.example.mediminder.utils.Constants.ERR_DELETING_MED
import com.example.mediminder.utils.Constants.ERR_UPDATING_MED
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

/**
 * Medication repository class that interacts with the database access objects to perform CRUD operations.
 */
class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val dosageDao: DosageDao,
    private val remindersDao: MedRemindersDao,
    private val scheduleDao: ScheduleDao,
    private val medicationLogDao: MedicationLogDao,
) {
    private val insertHelper = InsertHelper(medicationDao, dosageDao, remindersDao, scheduleDao)
    private val updateHelper = UpdateHelper(medicationDao, dosageDao, remindersDao, scheduleDao, medicationLogDao)

    /**
     * Get a simple list of all medications (no dosage, reminders, or schedules).
     */
    suspend fun getAllMedicationsSimple(): List<Medication> {
        return medicationDao.getAll()
    }

    /**
     * Get a medication by its ID.
     */
    suspend fun getMedicationById(medicationId: Long): Medication {
        return medicationDao.getMedicationById(medicationId)
    }

    /**
     * Get a detailed list of all medications, including dosage, reminders, and schedules.
     */
    suspend fun getAllMedicationsDetailed(): List<MedicationWithDetails> {
        return medicationDao.getAll().map { medication ->
            MedicationWithDetails(
                medication = medication,
                dosage = dosageDao.getDosageByMedicationId(medication.id),
                reminders = remindersDao.getReminderByMedicationId(medication.id),
                schedule = scheduleDao.getScheduleByMedicationId(medication.id)
            )
        }
    }

    /**
     * Get detailed medication data by ID, including dosage, reminders, and schedules.
     */
    suspend fun getMedicationDetailsById(medicationId: Long): MedicationWithDetails {
        return MedicationWithDetails(
            medication = medicationDao.getMedicationById(medicationId),
            dosage = dosageDao.getDosageByMedicationId(medicationId),
            reminders = remindersDao.getReminderByMedicationId(medicationId),
            schedule = scheduleDao.getScheduleByMedicationId(medicationId)
        )
    }

    suspend fun getMedicationLogStatus(logId: Long): MedicationStatus {
        return medicationLogDao.getMedicationLogStatus(logId)
    }

    suspend fun getAsNeededMedications(): List<Medication> {
        return medicationDao.getAsNeededMedications()
    }

    /**
     * Add medication to the database.
     */
    suspend fun addMedication(
        medicationData: MedicationData,
        dosageData: DosageData?,
        reminderData: ReminderData?,
        scheduleData: ScheduleData?
    ): Long {
        try {
            return insertHelper.addMedicationData(medicationData, dosageData, reminderData, scheduleData)
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_ADDING_MED_USER, e)
            throw e
        }
    }

    /**
     * Update medication data in the database.
     */
    suspend fun updateMedication(
        medicationId: Long,
        medicationData: MedicationData,
        dosageData: DosageData?,
        reminderData: ReminderData?,
        scheduleData: ScheduleData?,
    ) {
        try {
            updateHelper.updateMedicationData(medicationId, medicationData, dosageData, reminderData, scheduleData)
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_UPDATING_MED, e)
            throw e
        }
    }

    suspend fun deleteMedication(medicationId: Long) {
        medicationDao.deleteById(medicationId)
    }

    /**
     * Delete future medication logs of a specific medication.
     */
    suspend fun deleteFutureLogs(medicationId: Long) {
        val currentDateTime = LocalDateTime.now()
        medicationLogDao.deleteFutureLogs(medicationId, currentDateTime)
    }

    /**
     * Update the status of a medication log.
     */
    suspend fun updateMedicationLogStatus(logId: Long, newStatus: MedicationStatus) {
        medicationLogDao.updateStatus(logId, newStatus)
    }

    /**
     * Get medication history for a specific medication (or all medications if medicationId is null)
     */
    suspend fun getMedicationHistory(medicationId: Long?, selectedYearMonth: YearMonth): MedicationHistory {
        // Convert LocalDate to LocalDateTime for proper comparison to database
        val startDate = selectedYearMonth.atDay(1).atStartOfDay()
        // End date is exclusive, so add 1 day
        val endDate = selectedYearMonth.atEndOfMonth().plusDays(1).atStartOfDay()

        val logs = when (medicationId) {
            null -> medicationLogDao.getLogsInRange(
                startDate = startDate,
                endDate = endDate
            )
            else -> medicationLogDao.getLogsForMedicationInRange(
                medicationId = medicationId,
                startDate = startDate,
                endDate = endDate
            )
        }

        val logsWithDetails = getLogsWithDetails(logs)
        return MedicationHistory(logs = logsWithDetails)
    }

    /**
     * Get detailed medication log data for a list of logs.
     */
    private suspend fun getLogsWithDetails(logs: List<MedicationLogs>): List<MedicationLogWithDetails> {
        return logs.map { log ->
            val medication = medicationDao.getMedicationById(log.medicationId)
            val dosage = dosageDao.getDosageByMedicationId(log.medicationId)

            MedicationLogWithDetails(
                id = log.id,
                name = medication.name,
                dosageAmount = dosage?.amount,
                dosageUnits = dosage?.units,
                log = log
            )
        }
    }

    /**
     * Get medication logs for a specific date.
     */
    suspend fun getLogsForDate(date: LocalDate): List<MedicationItem> {
        val startOfDay = LocalDateTime.of(date, LocalTime.MIN)
        val endOfDay = LocalDateTime.of(date, LocalTime.MAX)

        val logs = medicationLogDao.getLogsInRange(startOfDay, endOfDay)

        return logs.map { log ->
            val medication = medicationDao.getMedicationById(log.medicationId)
            val dosage = getDosage(log)

            MedicationItem(
                medication = medication,
                dosage = dosage,
                time = log.plannedDatetime.toLocalTime(),
                status = log.status,
                logId = log.id
            )
        }
    }

    /**
     * Get dosage data for a medication log.
     */
    private suspend fun getDosage(log: MedicationLogs): Dosage? {
        if (log.asNeededDosageAmount != null) {
            return Dosage(
                medicationId = log.medicationId,
                amount = log.asNeededDosageAmount,
                units = log.asNeededDosageUnit ?: EMPTY_STRING
            )
        } else {
            return dosageDao.getDosageByMedicationId(log.medicationId)
        }
    }

    /**
     * Add an as-needed (unscheduled) medication log.
     */
    suspend fun addAsNeededLog(validatedData: ValidatedAsNeededData) {
        try {
            medicationLogDao.insert(
                MedicationLogs(
                    medicationId = validatedData.medicationId,
                    scheduleId = null, // No schedule for as-needed medications
                    plannedDatetime = validatedData.plannedDatetime,
                    takenDatetime = validatedData.takenDatetime,
                    status = validatedData.status,  // As-needed medications can not be "missed" or "skipped"
                    asNeededDosageAmount = validatedData.asNeededDosageAmount,
                    asNeededDosageUnit = validatedData.asNeededDosageUnit
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_ADDING_MED, e)
            throw e
        }
    }

    /**
     * Delete an as-needed (unscheduled) medication log by log id
     */
    suspend fun deleteAsNeededMedication(logId: Long) {
        try {
            medicationLogDao.deleteById(logId)
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: ERR_DELETING_MED, e)
            throw e
        }
    }

    companion object {
        private const val TAG = "MedicationRepository"
    }
}

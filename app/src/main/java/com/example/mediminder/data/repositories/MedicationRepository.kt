package com.example.mediminder.data.repositories
import android.content.Context
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationIcon
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.MedicationStatus
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.viewmodels.DosageData
import com.example.mediminder.viewmodels.MedicationData
import com.example.mediminder.viewmodels.ReminderData
import com.example.mediminder.viewmodels.ScheduleData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val dosageDao: DosageDao,
    private val remindersDao: MedRemindersDao,
    private val scheduleDao: ScheduleDao,
    private val medicationLogDao: MedicationLogDao,
    private val context: Context
) {

    suspend fun addMedication(
        medicationData: MedicationData,
        dosageData: DosageData,
        reminderData: ReminderData,
        scheduleData: ScheduleData
    ) {
        try {
            val medicationId = insertMedication(medicationData, reminderData)
            insertDosage(medicationId, dosageData)
            insertReminder(medicationId, reminderData)
            insertSchedule(medicationId, scheduleData)
        } catch (e: Exception) {
            throw Exception("Failed to add medication: ${e.message}", e)
        }
    }

    private suspend fun insertMedication(medicationData: MedicationData, reminderData: ReminderData): Long {
        try {
            val medicationId = medicationDao.insert(
                Medication(
                    name = medicationData.name,
                    prescribingDoctor = medicationData.doctor,
                    notes = medicationData.notes,
                    icon = medicationData.icon ?: MedicationIcon.TABLET,
                    reminderEnabled = reminderData.reminderEnabled
                )
            )
            return medicationId
        } catch (e: Exception) {
            throw Exception("Failed to insert medication: ${e.message}", e)
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
            throw Exception("Failed to insert dosage: ${e.message}", e)
        }
    }

    private suspend fun insertReminder(medicationId: Long, reminderData: ReminderData) {
        try {
            remindersDao.insert(
                MedReminders(
                    medicationId = medicationId,
                    reminderFrequency = reminderData.reminderFrequency,
                    hourlyReminderInterval = reminderData.hourlyReminderInterval,
                    hourlyReminderStartTime = getLocalTimeFromPair(reminderData.hourlyReminderStartTime),
                    hourlyReminderEndTime = getLocalTimeFromPair(reminderData.hourlyReminderEndTime),
                    dailyReminderTimes = reminderData.dailyReminderTimes.map {
                        getLocalTimeFromPair(it) ?: throw Exception("Invalid time pair when trying to insert reminder: $it")
                    }
                )
            )
        } catch (e: Exception) {
            throw Exception("Failed to insert reminder: ${e.message}", e)
        }
    }

    private fun getLocalTimeFromPair(pair: Pair<Int, Int>?): LocalTime? {
        return pair?.let { LocalTime.of(it.first, it.second) }
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
            throw Exception("Failed to insert schedule: ${e.message}", e)
        }
    }

    suspend fun getMedicationLogsForDate(date: LocalDate): List<MedicationItem> {
        val startOfDay = LocalDateTime.of(date, LocalTime.MIN)
        val endOfDay = LocalDateTime.of(date.plusDays(1), LocalTime.MIN)
        val logs = medicationLogDao.getLogsForDate(startOfDay, endOfDay)

        val result = logs.mapNotNull { log ->
            val medication = medicationDao.getMedicationById(log.medicationId)
            // Skip this log if medication doesn't exist
            if (medication == null) { return@mapNotNull null }

            val dosage = dosageDao.getDosageByMedicationId(log.medicationId)

            MedicationItem(
                medication = medication,
                dosage = dosage,
                time = log.plannedDatetime.toLocalTime(),
                status = log.status,
                logId = log.id
            )
        }.distinctBy { it.logId }.sortedBy { it.time }

        return result
    }

    suspend fun updateMedicationLogStatus(logId: Long, newStatus: MedicationStatus) {
        medicationLogDao.updateStatus(logId, newStatus)
    }

    suspend fun getMedicationAdherenceData(
        medicationId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MedicationLogs> {
        return medicationLogDao.getLogsForMedicationInRange(
            medicationId = medicationId,
            startDate = startDate,
            endDate = endDate
        )
    }

    suspend fun getAllMedications(): List<MedicationWithDetails> {
        return medicationDao.getAll().map { medication ->
            MedicationWithDetails(
                medication = medication,
                dosage = dosageDao.getDosageByMedicationId(medication.id),
                reminders = remindersDao.getRemindersByMedicationId(medication.id),
                schedule = scheduleDao.getScheduleByMedicationId(medication.id)
            )
        }
    }

    suspend fun getMedicationDetailsById(medicationId: Long): MedicationWithDetails {
        return MedicationWithDetails(
            medication = medicationDao.getMedicationById(medicationId),
            dosage = dosageDao.getDosageByMedicationId(medicationId),
            reminders = remindersDao.getRemindersByMedicationId(medicationId),
            schedule = scheduleDao.getScheduleByMedicationId(medicationId)
        )
    }
}

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

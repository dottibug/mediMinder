package com.example.mediminder.data.repositories
import android.util.Log
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.MedicationStatus
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.utils.MedScheduledForDateUtil
import com.example.mediminder.utils.ReminderTimesUtil.getHourlyReminderTimes
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
    private val medicationLogDao: MedicationLogDao
) {

    suspend fun addMedication(
        medicationData: MedicationData,
        dosageData: DosageData,
        reminderData: ReminderData,
        scheduleData: ScheduleData,
    ) {

        try {
            val medicationId = medicationDao.insert(
                Medication(
                    name = medicationData.name,
                    prescribingDoctor = medicationData.doctor,
                    notes = medicationData.notes,
                    reminderEnabled = reminderData.reminderEnabled,
                )
            )

            dosageDao.insert(
                Dosage(
                    medicationId = medicationId,
                    amount = dosageData.dosageAmount,
                    units = dosageData.dosageUnits
                )
            )

            val scheduleId = scheduleDao.insert(
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

            if (reminderData.reminderEnabled) {
                remindersDao.insert(
                    MedReminders(
                        medicationId = medicationId,
                        reminderFrequency = reminderData.reminderFrequency,
                        hourlyReminderInterval = reminderData.hourlyReminderInterval,
                        hourlyReminderStartTime = reminderData.hourlyReminderStartTime?.let {
                            LocalTime.of(
                                it.first, it.second
                            )
                        },
                        hourlyReminderEndTime = reminderData.hourlyReminderEndTime?.let {
                            LocalTime.of(
                                it.first, it.second
                            )
                        },
                        dailyReminderTimes = reminderData.dailyReminderTimes.map {
                            LocalTime.of(
                                it.first, it.second
                            )
                        },
                    )
                )

                createInitialMedicationLogs(
                    medicationId = medicationId,
                    scheduleId = scheduleId,
                    startDate = scheduleData.startDate ?: LocalDate.now(),
                    reminderData = reminderData
                )
            }

        } catch (e: Exception) {
            Log.e("testcat MedicationRepository", "Error adding medication: ${e.message}")
            // todo: throw e    to have calling functions handle errors
        }
    }

    suspend fun getMedicationsForDate(date: LocalDate): List<Triple<Medication, Dosage?, LocalTime>> {
        val allMeds = medicationDao.getAllWithRemindersEnabled()
        Log.d("testcat", "All meds with reminders enabled: ${allMeds.map { it.name }}")


        val result = mutableListOf<Triple<Medication, Dosage?, LocalTime>>()

        allMeds.forEach { medication ->
            val schedule = scheduleDao.getScheduleByMedicationId(medication.id)
            Log.d("testcat", "Checking schedule for ${medication.name} on $date")
            Log.d("testcat", "Is scheduled: ${MedScheduledForDateUtil.isScheduledForDate(schedule, date)}")
            if (MedScheduledForDateUtil.isScheduledForDate(schedule, date)) {
                val dosage = dosageDao.getDosageByMedicationId(medication.id)
                val reminder = remindersDao.getReminderByMedicationId(medication.id)
                Log.d("testcat", "Reminder frequency: ${reminder?.reminderFrequency}")
                val reminderTimes = when (reminder?.reminderFrequency) {
                    "daily" -> reminder.dailyReminderTimes
                    "hourly" -> getHourlyReminderTimes(
                        reminder.hourlyReminderInterval,
                        reminder.hourlyReminderStartTime?.let { Pair(it.hour, it.minute) },
                        reminder.hourlyReminderEndTime?.let { Pair(it.hour, it.minute) }
                    )
                    else -> emptyList()
                }

                reminderTimes.forEach { time -> result.add(Triple(medication, dosage, time)) }
            }
        }

        return result.sortedBy { it.third }
    }

    suspend fun updateMedicationStatus(medicationId: Long, newStatus: MedicationStatus) {
        medicationLogDao.updateStatus(medicationId, newStatus)
    }

    private suspend fun createInitialMedicationLogs(
        medicationId: Long,
        scheduleId: Long,
        startDate: LocalDate,
        reminderData: ReminderData
    ) {
        val reminderTimes = when {
            reminderData.reminderFrequency == "daily" ->
                // Create local time from pair of ints
                reminderData.dailyReminderTimes.map { (hour, minute) ->
                    LocalTime.of(hour, minute)
                }
            reminderData.reminderFrequency == "hourly" ->
                getHourlyReminderTimes(
                    reminderData.hourlyReminderInterval,
                    reminderData.hourlyReminderStartTime,
                    reminderData.hourlyReminderEndTime
                )
            else -> emptyList()
        }

        reminderTimes.forEach { time ->
            val plannedDateTime = LocalDateTime.of(startDate, time)
            medicationLogDao.insert(
                MedicationLogs(
                    medicationId = medicationId,
                    scheduleId = scheduleId,
                    plannedDatetime = plannedDateTime,
                    takenDatetime = null,
                    status = MedicationStatus.PENDING
                )
            )
        }
    }

//    private fun getHourlyReminderTimes(reminderData: ReminderData): List<LocalTime> {
//        val startTime = reminderData.hourlyReminderStartTime ?: return emptyList()
//        val endTime = reminderData.hourlyReminderEndTime ?: return emptyList()
//        val interval = reminderData.hourlyReminderInterval ?: return emptyList()
//
//        Log.d("testcat", "Starting hourly time generation")
//
//        // Parse interval to hours
//        val intervalHours = interval.toIntOrNull()?.toLong() ?: return emptyList()
//
//        val startLocalTime = LocalTime.of(startTime.first, startTime.second)
//        val endLocalTime = LocalTime.of(endTime.first, endTime.second)
//
//        val times = mutableListOf<LocalTime>()
//
//        // Calculate how many intervals fit between start and end time
//        val startMinutes = startLocalTime.hour * 60 + startLocalTime.minute
//        val endMinutes = endLocalTime.hour * 60 + endLocalTime.minute
//        val intervalMinutes = intervalHours * 60
//        val numberOfIntervals = ((endMinutes - startMinutes) / intervalMinutes).toInt()
//
//        // Generate times
//        for (i in 0..numberOfIntervals) {
//            val time = startLocalTime.plusHours(intervalHours * i)
//            if (time.isAfter(endLocalTime)) break
//            times.add(time)
//            Log.d("testcat", "Added time: $time")
//        }
//
//        Log.d("testcat", "Generated ${times.size} times")
//        return times
//    }
}


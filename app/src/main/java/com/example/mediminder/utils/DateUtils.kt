package com.example.mediminder.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    fun formatLocalTimeTo12Hour(localTime: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        return localTime.format(formatter).lowercase()
    }

    fun formatToLongDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
        return localDate.format(formatter)
    }
}
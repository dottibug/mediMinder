package com.example.mediminder.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

// Room does not persist DATETIME data types, so we need to define type converters
// https://developer.android.com/training/data-storage/room/referencing-data
class Converters {
    // -------------------------------------------------------------
    // Converters for LocalDate (epochDay: days since epoch)
    // -------------------------------------------------------------
    @TypeConverter
    fun fromLongToLocalDate(dbValue: Long?): LocalDate? {
        return dbValue?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun fromLocalDateToLong(localDate: LocalDate?): Long? {
        return localDate?.toEpochDay()
    }

    // -------------------------------------------------------------
    // Converters for LocalTime
    // -------------------------------------------------------------
    @TypeConverter
    fun fromStringToLocalTime(dbValue: String?): LocalTime? {
        return dbValue?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun fromLocalTimeToString(localTime: LocalTime?): String? {
        return localTime?.toString()
    }

    // ----------------------------------------------------------------------
    // Converters for LocalDateTime (epochSecond: seconds since epoch)
    // ----------------------------------------------------------------------
    @TypeConverter
    fun fromLongToLocalDateTime(dbValue: Long?): LocalDateTime? {
        return dbValue?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun fromLocalDateTimeToLong(localDateTime: LocalDateTime?): Long? {
        return localDateTime?.toEpochSecond(ZoneOffset.UTC)
    }
}
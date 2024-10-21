package com.example.mediminder.utils

class AppUtils {

    // Convert 24-hour digit to 12-hour digit (ex. 13 -> 1)
    fun convert24HourTo12Hour(hour: Int): Int {
        return if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    }

}
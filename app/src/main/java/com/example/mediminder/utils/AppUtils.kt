package com.example.mediminder.utils

class AppUtils {

    // Convert 24-hour digit to 12-hour digit (ex. 13 -> 1)
    fun convert24HourTo12Hour(hour: Int): Int {
        return if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    }

    // Convert comma-separated list of integers to days of the week
    fun convertDaysIntToDaysStringList(daysInt: String): List<String> {
        val daysStringList = mutableListOf<String>()
        val daysIntList = daysInt.split(",")

        for (dayInt in daysIntList) {
            when (dayInt) {
                "0" -> daysStringList.add("Sunday")
                "1" -> daysStringList.add("Monday")
                "2" -> daysStringList.add("Tuesday")
                "3" -> daysStringList.add("Wednesday")
                "4" -> daysStringList.add("Thursday")
                "5" -> daysStringList.add("Friday")
                "6" -> daysStringList.add("Saturday")
            }
        }
        return daysStringList
    }
}
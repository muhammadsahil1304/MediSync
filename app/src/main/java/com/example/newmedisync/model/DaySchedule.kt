package com.example.newmedisync.model

data class DaySchedule(
    val dayOfWeek: String = "",
    val enabled: Boolean = false,
    val startTime: String = "09:00 AM",
    val endTime: String = "05:00 PM"
)

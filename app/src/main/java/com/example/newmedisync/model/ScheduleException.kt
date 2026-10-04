package com.example.newmedisync.model

data class ScheduleException(
    val id: String = "",
    val doctorId: String = "",
    val locationId: String = "",
    val locationName: String = "",
    val date: String = "",
    val type: String = "FULL_DAY", // "FULL_DAY" or "BLOCK_TIME"
    val startTime: String = "",
    val endTime: String = "",
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

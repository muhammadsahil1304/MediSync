package com.example.newmedisync.model

data class DoctorAvailability(
    val id: String = "",
    val doctorId: String = "",
    val locationId: String = "",
    val locationName: String = "",
    val schedules: Map<String, DaySchedule> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)

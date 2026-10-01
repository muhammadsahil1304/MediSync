package com.example.newmedisync.model

data class PracticeLocation(
    val id: String = "",
    val doctorId: String = "",
    val name: String = "",
    val address: String = "",
    val type: String = "Hospital", // "Hospital" or "Clinic"
    val consultationFee: Double = 0.0,
    val availableDays: List<String> = emptyList(), // e.g. ["Mon", "Wed", "Fri"]
    val timing: String = "" // e.g. "05:00 PM - 08:00 PM"
)

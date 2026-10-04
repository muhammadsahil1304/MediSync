package com.example.newmedisync.model

data class Appointment(
    val appointmentId: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorSpecialization: String = "",
    val doctorProfileImageUrl: String = "",
    val practiceLocationId: String = "",
    val locationName: String = "",
    val locationAddress: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val timestamp: Long = 0L,
    val status: String = "Upcoming", // "Upcoming", "Completed", "Cancelled"
    val consultationFee: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val cancelledBy: String = "", // "PATIENT" or "DOCTOR"
    val cancelledAt: Long = 0L,
    val cancelReason: String = ""
)

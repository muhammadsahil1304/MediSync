package com.example.newmedisync.model

data class FollowUpItem(
    val followUpId: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorSpecialization: String = "",
    val sourceAppointmentId: String = "",
    val sourceVisitId: String = "",
    val recommendedDate: String = "", // format: dd/MM/yyyy
    val recommendedTimestamp: Long = 0L,
    val reason: String = "",
    val instructions: String = "",
    val status: String = "RECOMMENDED", // "RECOMMENDED", "BOOKED", "COMPLETED", "CANCELLED", "CLOSED"
    val bookedAppointmentId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

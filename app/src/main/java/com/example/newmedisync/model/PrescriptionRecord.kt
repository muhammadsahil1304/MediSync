package com.example.newmedisync.model

data class PrescriptionRecord(
    val id: String = "",
    val appointmentId: String = "",
    val patientUid: String = "",
    val patientName: String = "",
    val doctorUid: String = "",
    val doctorName: String = "",
    val doctorSpecialization: String = "",
    val date: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val diagnosis: List<String> = emptyList(),
    val medicines: List<Medicine> = emptyList(),
    val instructions: String = "",
    val followUpDate: String = "",
    val status: String = "ISSUED"
)

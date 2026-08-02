package com.example.newmedisync.model

data class PrescriptionRecord(
    val id: String = "",
    val patientUid: String = "",
    val doctorUid: String = "",
    val doctorName: String = "",
    val date: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L
)

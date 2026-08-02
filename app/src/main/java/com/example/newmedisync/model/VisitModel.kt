package com.example.newmedisync.model

data class VisitModel(
    val id: String = "",
    val patientUid: String = "",
    val doctorUid: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val date: String = "", // format: dd/MM/yyyy
    val time: String = "",
    val purpose: String = "",
    val timestamp: Long = 0L
)

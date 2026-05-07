package com.example.medisync.model

data class Patient(
    val initials: String,
    val name: String,
    val patientId: String,
    val phone: String,
    val lastVisit: String,
    val status: String
)
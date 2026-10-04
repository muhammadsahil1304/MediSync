package com.example.newmedisync.model

data class DoctorPatientItem(
    val patientUid: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val age: Int = 0,
    val gender: String = "",
    val bloodGroup: String = "",
    val profileImageUrl: String = "",
    val lastVisitDate: String = "",
    val lastVisitTimestamp: Long = 0L,
    val nextAppointmentDate: String = "",
    val appointmentCount: Int = 0
)

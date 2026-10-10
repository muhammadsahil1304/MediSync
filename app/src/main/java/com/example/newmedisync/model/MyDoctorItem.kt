package com.example.newmedisync.model

data class MyDoctorItem(
    val doctorId: String = "",
    val doctorName: String = "",
    val specialization: String = "",
    val profileImageUrl: String = "",
    val lastLocationName: String = "",
    val lastVisitDate: String = "",
    val lastVisitTimestamp: Long = 0L,
    val appointmentCount: Int = 0,
    val hasUpcoming: Boolean = false
)

package com.example.newmedisync.model

enum class HealthRecordType {
    ALL, REPORT, PRESCRIPTION, VISIT
}

data class HealthRecordItem(
    val id: String = "",
    val type: HealthRecordType,
    val title: String = "",
    val subtitle: String = "",
    val dateStr: String = "",
    val timestamp: Long = 0L,
    val badgeText: String = "",
    val rawReport: MedicalReport? = null,
    val rawPrescription: PrescriptionRecord? = null,
    val rawVisit: VisitModel? = null,
    val rawAppointment: Appointment? = null
)

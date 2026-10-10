package com.example.newmedisync.model

data class VisitModel(
    val id: String = "",
    val patientUid: String = "",
    val doctorUid: String = "",
    val appointmentId: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val doctorSpecialization: String = "",
    val date: String = "", // format: dd/MM/yyyy
    val time: String = "",
    val purpose: String = "",
    val chiefComplaint: String = "",
    val symptoms: String = "",
    val examinationFindings: String = "",
    val diagnosis: String = "",
    val treatmentPlan: String = "",
    val recommendations: String = "",
    val followUpDate: String = "",
    val bpSystolic: Int = 0,
    val bpDiastolic: Int = 0,
    val pulseRate: Int = 0,
    val temperature: Double = 0.0,
    val spO2: Int = 0,
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val respiratoryRate: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.newmedisync.model

data class MedicalReport(
    val reportId: String = "",
    val patientUid: String = "",
    val reportName: String = "",
    val createdAt: Long = 0L,
    val extractedText: String = "",
    val analysis: ReportAnalysis? = null,
    val fileUrl: String = ""
)

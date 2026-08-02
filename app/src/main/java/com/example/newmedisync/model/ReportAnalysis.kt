package com.example.newmedisync.model

data class ReportAnalysis(
    val summary: String = "",
    val abnormalFindings: List<String> = emptyList(),
    val normalFindings: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val severity: String = "",
    val followUp: String = ""
)

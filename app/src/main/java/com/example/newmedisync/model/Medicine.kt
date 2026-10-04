package com.example.newmedisync.model

data class Medicine(
    val medicineId: String = "",
    val name: String = "",
    val strength: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val timing: String = "",
    val durationValue: Int = 5,
    val durationUnit: String = "Days",
    val instructions: String = "",
    val order: Int = 0
)

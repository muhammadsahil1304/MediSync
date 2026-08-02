package com.example.newmedisync.model

    data class PatientModel(
        val uid: String = "",

        val age: Int = 0,

        val gender: String = "",

        val bloodGroup: String = "",

        val height: Double = 0.0,

        val weight: Double = 0.0,

        val allergies: List<String> = emptyList(),

        val diseases: List<String> = emptyList(),

        val medications: List<String> = emptyList(),

        val emergencyName: String = "",

        val emergencyPhone: String = "",

        val profileImageUrl: String = ""
    )

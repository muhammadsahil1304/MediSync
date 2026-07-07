package com.example.newmedisync.model

data class Doctor(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",

    val specialization: String = "",
    val qualification: String = "",
    val experience: String = "",
    val registrationNumber: String = "",

    val clinicName: String = "",
    val consultationFee: String = "",
    val clinicAddress: String = "",

    val bio: String = "",

    val profileImageUrl: String = "",
    val licenseUrl: String = "",
    val degreeUrl: String = "",

    val verificationStatus: String = "PENDING"
)
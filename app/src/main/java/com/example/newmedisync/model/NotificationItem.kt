package com.example.newmedisync.model

data class NotificationItem(
    val notificationId: String = "",
    val recipientUid: String = "",
    val recipientRole: String = "patient", // "patient" or "doctor"
    val title: String = "",
    val message: String = "",
    val type: String = "GENERAL", // "APPOINTMENT_BOOKED", "APPOINTMENT_COMPLETED", "APPOINTMENT_CANCELLED", "PRESCRIPTION_ISSUED", "GENERAL"
    val relatedAppointmentId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

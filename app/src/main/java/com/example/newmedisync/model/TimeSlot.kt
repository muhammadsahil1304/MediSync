package com.example.newmedisync.model

data class TimeSlot(
    val time: String,
    val isAvailable: Boolean = true,
    var isSelected: Boolean = false
)

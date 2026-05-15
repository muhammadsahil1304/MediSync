package com.example.newmedisync.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visits")
data class VisitEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val patientName: String,

    val patientPhone: String,

    val visitDate: String,

    val visitTime: String,

    val purpose: String
)
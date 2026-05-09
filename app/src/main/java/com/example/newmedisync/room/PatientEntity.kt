package com.example.newmedisync.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val phone: String,
    val age: String,
    val gender: String,
    val bloodGroup: String,
    val address: String,
    val medicalNotes: String
)
package com.example.newmedisync.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.auth.FirebaseAuth

@Entity(tableName = "visits")
data class VisitEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String = FirebaseAuth.getInstance().currentUser!!.uid,
    val patientName: String,

    val patientPhone: String,

    val visitDate: String,

    val visitTime: String,

    val purpose: String
)
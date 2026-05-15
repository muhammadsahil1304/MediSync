package com.example.newmedisync.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PrescriptionDao {

    @Insert
    suspend fun insertPrescription(
        prescription: PrescriptionEntity
    )

    @Query(
        "SELECT * FROM prescriptions WHERE patientName = :patientName ORDER BY id DESC"
    )
    fun getPrescriptionsByPatient(
        patientName: String
    ): LiveData<List<PrescriptionEntity>>

    @Query(
        "SELECT * FROM prescriptions WHERE patientName = :patientName ORDER BY id DESC LIMIT 1"
    )
    fun getLatestPrescription(
        patientName: String
    ): LiveData<PrescriptionEntity?>
}
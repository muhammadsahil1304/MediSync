package com.example.newmedisync.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PatientDao {

    @Insert
    suspend fun insertPatient(patient: PatientEntity)


    @Query("SELECT * FROM patients ORDER BY id DESC")
    fun getAllPatients(): LiveData<List<PatientEntity>>
}
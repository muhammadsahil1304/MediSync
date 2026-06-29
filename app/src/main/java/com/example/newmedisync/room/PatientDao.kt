package com.example.newmedisync.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PatientDao {

    @Insert
    suspend fun insertPatient(patient: PatientEntity)


//    @Query("SELECT * FROM patients ORDER BY id DESC")
//    fun getAllPatients(): LiveData<List<PatientEntity>>
//
@Query("SELECT * FROM patients WHERE userId = :uid")
fun getAllPatients(uid: String): LiveData<List<PatientEntity>>
    @Query("""
    SELECT * FROM patients
    WHERE name LIKE '%' || :query || '%'
    OR phone LIKE '%' || :query || '%'
    ORDER BY id DESC
""")
    fun searchPatients(query: String): LiveData<List<PatientEntity>>
    @Query("SELECT COUNT(*) FROM patients WHERE userId = :uid")
    fun getPatientsCount(uid: String): LiveData<Int>
}
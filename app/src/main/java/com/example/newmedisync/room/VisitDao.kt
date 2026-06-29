package com.example.newmedisync.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VisitDao {

    @Insert
    suspend fun insertVisit(
        visit: VisitEntity
    )

    @Query(
        "SELECT * FROM visits WHERE patientName = :patientName ORDER BY id DESC"
    )
    fun getVisitsByPatient(
        patientName: String
    ): LiveData<List<VisitEntity>>

    @Query(
        "SELECT * FROM visits ORDER BY id DESC"
    )
    fun getAllVisits(): LiveData<List<VisitEntity>>

    @Query("SELECT COUNT(*) FROM visits WHERE userId = :uid")
    fun getVisitsCount(uid: String): LiveData<Int>

    @Query("SELECT * FROM visits WHERE userId = :uid ORDER BY id DESC LIMIT 1")
    fun getLatestVisit(uid: String): LiveData<VisitEntity?>
}
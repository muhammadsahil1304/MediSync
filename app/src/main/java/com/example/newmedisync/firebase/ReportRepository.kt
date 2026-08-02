package com.example.newmedisync.firebase

import com.example.newmedisync.model.MedicalReport
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReportRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveReport(report: MedicalReport) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = firestore.collection("reports").document()
        val reportWithId = report.copy(
            reportId = docRef.id,
            patientUid = uid,
            createdAt = System.currentTimeMillis()
        )
        docRef.set(reportWithId).await()
    }

    suspend fun getPatientReports(): List<MedicalReport> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("reports")
                .whereEqualTo("patientUid", uid)
                .get()
                .await()
            snapshot.toObjects(MedicalReport::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

package com.example.newmedisync.firebase

import com.example.newmedisync.model.FollowUpItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FollowUpRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveFollowUp(followUp: FollowUpItem): String {
        if (followUp.patientId.isBlank() || followUp.doctorId.isBlank()) return ""

        val docId = if (followUp.followUpId.isNotBlank()) {
            followUp.followUpId
        } else if (followUp.sourceAppointmentId.isNotBlank()) {
            "fu_${followUp.sourceAppointmentId}"
        } else {
            firestore.collection("follow_ups").document().id
        }

        val finalFollowUp = followUp.copy(
            followUpId = docId,
            updatedAt = System.currentTimeMillis()
        )

        firestore.collection("follow_ups")
            .document(docId)
            .set(finalFollowUp)
            .await()

        return docId
    }

    suspend fun getPatientFollowUps(patientId: String): List<FollowUpItem> {
        if (patientId.isBlank()) return emptyList()
        return try {
            val snapshot = firestore.collection("follow_ups")
                .whereEqualTo("patientId", patientId)
                .get()
                .await()
            snapshot.toObjects(FollowUpItem::class.java)
                .sortedBy { it.recommendedTimestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDoctorFollowUps(doctorId: String): List<FollowUpItem> {
        if (doctorId.isBlank()) return emptyList()
        return try {
            val snapshot = firestore.collection("follow_ups")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .await()
            snapshot.toObjects(FollowUpItem::class.java)
                .sortedBy { it.recommendedTimestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateFollowUpStatus(followUpId: String, newStatus: String, bookedAppointmentId: String = "") {
        if (followUpId.isBlank()) return
        try {
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            )
            if (bookedAppointmentId.isNotBlank()) {
                updates["bookedAppointmentId"] = bookedAppointmentId
            } else if (newStatus == "RECOMMENDED") {
                updates["bookedAppointmentId"] = ""
            }

            firestore.collection("follow_ups")
                .document(followUpId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncFollowUpOnAppointmentStatusChange(appointmentId: String, newAppointmentStatus: String) {
        if (appointmentId.isBlank()) return
        try {
            val bookedSnapshot = firestore.collection("follow_ups")
                .whereEqualTo("bookedAppointmentId", appointmentId)
                .get()
                .await()

            for (doc in bookedSnapshot.documents) {
                val fu = doc.toObject(FollowUpItem::class.java) ?: continue
                when (newAppointmentStatus.uppercase()) {
                    "COMPLETED" -> {
                        updateFollowUpStatus(fu.followUpId, "COMPLETED", appointmentId)
                    }
                    "CANCELLED" -> {
                        updateFollowUpStatus(fu.followUpId, "RECOMMENDED", "")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.example.newmedisync.firebase

import android.util.Log
import com.example.newmedisync.model.PatientModel
import com.example.newmedisync.model.PrescriptionRecord
import com.example.newmedisync.model.User
import com.example.newmedisync.model.VisitModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class PatientRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentPatient(
        onSuccess: (PatientModel) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("patients")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val patient = it.toObject(PatientModel::class.java)
                if (patient != null) onSuccess(patient)
            }
            .addOnFailureListener { onFailure(it) }
    }

    suspend fun getPatientVisits(patientUid: String): List<VisitModel> {
        return try {
            val snapshot = firestore.collection("visits")
                .whereEqualTo("patientUid", patientUid)
                .get()
                .await()
            snapshot.toObjects(VisitModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPatientPrescriptions(patientUid: String): List<PrescriptionRecord> {
        return try {
            val snapshot = firestore.collection("prescriptions")
                .whereEqualTo("patientUid", patientUid)
                .get()
                .await()
            snapshot.toObjects(PrescriptionRecord::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveVisit(visit: VisitModel) {
        val docRef = firestore.collection("visits").document()
        docRef.set(visit.copy(id = docRef.id)).await()
    }

    suspend fun getPrescriptionByAppointmentId(appointmentId: String): PrescriptionRecord? {
        if (appointmentId.isBlank()) return null
        return try {
            val snapshot = firestore.collection("prescriptions")
                .whereEqualTo("appointmentId", appointmentId)
                .get()
                .await()
            snapshot.toObjects(PrescriptionRecord::class.java).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun savePrescription(prescription: PrescriptionRecord) {
        if (prescription.appointmentId.isNotBlank()) {
            val existingSnapshot = firestore.collection("prescriptions")
                .whereEqualTo("appointmentId", prescription.appointmentId)
                .get()
                .await()
            val existingDoc = existingSnapshot.documents.firstOrNull()
            if (existingDoc != null) {
                val finalPrescription = prescription.copy(id = existingDoc.id)
                firestore.collection("prescriptions").document(existingDoc.id).set(finalPrescription).await()
                return
            }
        }
        val docRef = if (prescription.id.isNotBlank()) {
            firestore.collection("prescriptions").document(prescription.id)
        } else {
            firestore.collection("prescriptions").document()
        }
        docRef.set(prescription.copy(id = docRef.id)).await()
    }

    suspend fun getUidByPhone(phone: String): String? {
        val snapshot = firestore.collection("users")
            .whereEqualTo("phone", phone)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.id
    }
    suspend fun getCurrentUser(): User {

        val uid = auth.currentUser?.uid
            ?: throw Exception("User not logged in")

        val snapshot = firestore.collection("users")
            .document(uid)
            .get()
            .await()

        return snapshot.toObject(User::class.java)
            ?: throw Exception("User not found")
    }
    suspend fun savePatientProfile(patient: PatientModel) {

        firestore.collection("patients")
            .document(patient.uid)
            .set(patient)
            .await()
    }

    suspend fun updateFullPatientProfile(user: User, patient: PatientModel) {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val userUpdates = mapOf(
            "name" to user.name,
            "phone" to user.phone
        )
        firestore.collection("users").document(uid).update(userUpdates).await()

        firestore.collection("patients").document(uid).set(patient).await()
    }

    suspend fun getPatientFullProfile(): Pair<User, PatientModel?> {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val userDoc = firestore.collection("users").document(uid).get().await()
        val patientDoc = firestore.collection("patients").document(uid).get().await()

        val user = userDoc.toObject(User::class.java) ?: User(uid = uid)
        val patientModel = if (patientDoc.exists()) patientDoc.toObject(PatientModel::class.java) else null

        return Pair(user, patientModel)
    }
    suspend fun patientExists(): Boolean {

        val uid = auth.currentUser?.uid
            ?: throw Exception("User not logged in")

        val document = firestore
            .collection("patients")
            .document(uid)
            .get()
            .await()

        return document.exists()
    }

}
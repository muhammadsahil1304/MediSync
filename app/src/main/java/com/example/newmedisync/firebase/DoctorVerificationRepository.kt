package com.example.newmedisync.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import com.example.newmedisync.model.DoctorVerification
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.example.newmedisync.model.User

class DoctorVerificationRepository {

    private val auth = FirebaseAuth.getInstance()

    private val firestore = FirebaseFirestore.getInstance()

    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadFile(
        uri: Uri,
        folder: String
    ): String {

        val uid = auth.currentUser!!.uid

        val fileName = UUID.randomUUID().toString()

        val reference = storage.reference
            .child(folder)
            .child(uid)
            .child(fileName)

        reference.putFile(uri).await()

        return reference.downloadUrl.await().toString()
    }
    suspend fun submitVerification(
        verification: DoctorVerification
    ) {

        firestore.collection("doctor_verifications")
            .document(verification.uid)
            .set(verification)
            .await()
    }
    suspend fun getCurrentUser(): User {

        val uid = auth.currentUser?.uid
            ?: throw Exception("User not logged in")

        val snapshot = firestore.collection("users")
            .document(uid)
            .get()
            .await()

        return snapshot.toObject(User::class.java)
            ?: throw Exception("User data not found")
    }
    suspend fun getPendingDoctors(): List<DoctorVerification> {

        val snapshot = firestore
            .collection("doctor_verifications")
            .whereEqualTo("status", "PENDING")
            .get()
            .await()

        return snapshot.toObjects(DoctorVerification::class.java)
    }
    suspend fun getDoctorVerification(uid: String): DoctorVerification {

        val document = firestore
            .collection("doctor_verifications")
            .document(uid)
            .get()
            .await()

        return document.toObject(DoctorVerification::class.java)
            ?: throw Exception("Doctor not found")
    }
    suspend fun approveDoctor(uid: String) {

        firestore.collection("doctor_verifications")
            .document(uid)
            .update("status", "APPROVED")
            .await()
    }
    suspend fun rejectDoctor(uid: String) {

        firestore.collection("doctor_verifications")
            .document(uid)
            .update("status", "REJECTED")
            .await()
    }
}
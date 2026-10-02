package com.example.newmedisync.firebase

import com.example.newmedisync.model.Appointment
import com.example.newmedisync.model.DoctorVerification
import com.example.newmedisync.model.PracticeLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AppointmentRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getRegisteredDoctors(): List<DoctorVerification> {
        return try {
            val approvedSnapshot = firestore.collection("doctor_verifications")
                .whereEqualTo("status", "APPROVED")
                .get()
                .await()
            val approvedDoctors = approvedSnapshot.toObjects(DoctorVerification::class.java)

            if (approvedDoctors.isNotEmpty()) {
                approvedDoctors
            } else {
                // Fallback for testing if no doctors are APPROVED yet
                val allSnapshot = firestore.collection("doctor_verifications")
                    .get()
                    .await()
                allSnapshot.toObjects(DoctorVerification::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDoctorById(doctorId: String): DoctorVerification? {
        return try {
            val doc = firestore.collection("doctor_verifications")
                .document(doctorId)
                .get()
                .await()
            doc.toObject(DoctorVerification::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPracticeLocations(doctor: DoctorVerification): List<PracticeLocation> {
        return try {
            val snapshot = firestore.collection("doctor_verifications")
                .document(doctor.uid)
                .collection("practice_locations")
                .get()
                .await()
            val locations = snapshot.toObjects(PracticeLocation::class.java)
            if (locations.isNotEmpty()) {
                locations
            } else {
                // Default location constructed from DoctorVerification details
                val feeDouble = doctor.consultationFee.toDoubleOrNull() ?: 500.0
                listOf(
                    PracticeLocation(
                        id = "default_${doctor.uid}",
                        doctorId = doctor.uid,
                        name = if (doctor.clinicName.isNotBlank()) doctor.clinicName else "Main Clinic",
                        address = if (doctor.clinicAddress.isNotBlank()) doctor.clinicAddress else "Clinic Address",
                        type = "Clinic",
                        consultationFee = feeDouble,
                        availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
                        timing = "09:00 AM - 08:00 PM"
                    )
                )
            }
        } catch (e: Exception) {
            val feeDouble = doctor.consultationFee.toDoubleOrNull() ?: 500.0
            listOf(
                PracticeLocation(
                    id = "default_${doctor.uid}",
                    doctorId = doctor.uid,
                    name = if (doctor.clinicName.isNotBlank()) doctor.clinicName else "Main Clinic",
                    address = if (doctor.clinicAddress.isNotBlank()) doctor.clinicAddress else "Clinic Address",
                    type = "Clinic",
                    consultationFee = feeDouble,
                    availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
                    timing = "09:00 AM - 08:00 PM"
                )
            )
        }
    }

    suspend fun getBookedTimeSlots(doctorId: String, date: String): Set<String> {
        return try {
            val snapshot = firestore.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", date)
                .whereEqualTo("status", "Upcoming")
                .get()
                .await()
            snapshot.documents.mapNotNull { it.getString("timeSlot") }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun bookAppointment(appointment: Appointment): String {
        val docRef = firestore.collection("appointments").document()
        val finalAppointment = appointment.copy(appointmentId = docRef.id)
        docRef.set(finalAppointment).await()
        return docRef.id
    }

    suspend fun getPatientAppointments(patientId: String): List<Appointment> {
        return try {
            val snapshot = firestore.collection("appointments")
                .whereEqualTo("patientId", patientId)
                .get()
                .await()
            snapshot.toObjects(Appointment::class.java).sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUpcomingAppointmentForPatient(patientId: String): Appointment? {
        return try {
            val appointments = getPatientAppointments(patientId)
            appointments.firstOrNull { it.status == "Upcoming" }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAppointmentById(appointmentId: String): Appointment? {
        return try {
            val doc = firestore.collection("appointments")
                .document(appointmentId)
                .get()
                .await()
            doc.toObject(Appointment::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDoctorAppointments(doctorId: String): List<Appointment> {
        return try {
            val snapshot = firestore.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .await()
            snapshot.toObjects(Appointment::class.java).sortedBy { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun listenDoctorAppointments(doctorId: String, onUpdate: (List<Appointment>) -> Unit, onError: (Exception) -> Unit) {
        firestore.collection("appointments")
            .whereEqualTo("doctorId", doctorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Appointment::class.java).sortedBy { it.timestamp }
                    onUpdate(list)
                }
            }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String) {
        firestore.collection("appointments")
            .document(appointmentId)
            .update("status", status)
            .await()
    }

    fun getCurrentDoctorUid(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentPatientUid(): String? {
        return auth.currentUser?.uid
    }
}

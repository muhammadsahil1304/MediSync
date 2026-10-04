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

    suspend fun cancelAppointment(appointmentId: String, cancelledBy: String, cancelReason: String = "") {
        val docRef = firestore.collection("appointments").document(appointmentId)
        val snapshot = docRef.get().await()
        val currentStatus = snapshot.getString("status") ?: "Upcoming"

        if (currentStatus.equals("Completed", ignoreCase = true) || currentStatus.equals("Cancelled", ignoreCase = true)) {
            throw IllegalStateException("Appointment is already $currentStatus and cannot be cancelled.")
        }

        val updates = mapOf(
            "status" to "Cancelled",
            "cancelledBy" to cancelledBy,
            "cancelledAt" to System.currentTimeMillis(),
            "cancelReason" to cancelReason
        )

        docRef.update(updates).await()
    }

    suspend fun saveDoctorAvailability(availability: com.example.newmedisync.model.DoctorAvailability) {
        val docId = availability.locationId.ifBlank { "default_${availability.doctorId}" }
        firestore.collection("doctor_verifications")
            .document(availability.doctorId)
            .collection("availabilities")
            .document(docId)
            .set(availability)
            .await()
    }

    suspend fun getDoctorAvailability(doctorId: String, locationId: String): com.example.newmedisync.model.DoctorAvailability? {
        return try {
            val docId = locationId.ifBlank { "default_${doctorId}" }
            val doc = firestore.collection("doctor_verifications")
                .document(doctorId)
                .collection("availabilities")
                .document(docId)
                .get()
                .await()
            doc.toObject(com.example.newmedisync.model.DoctorAvailability::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveDoctorException(exception: com.example.newmedisync.model.ScheduleException): String {
        val collectionRef = firestore.collection("doctor_verifications")
            .document(exception.doctorId)
            .collection("exceptions")

        val docRef = if (exception.id.isNotBlank()) {
            collectionRef.document(exception.id)
        } else {
            collectionRef.document()
        }

        val finalException = exception.copy(id = docRef.id)
        docRef.set(finalException).await()
        return docRef.id
    }

    suspend fun getDoctorExceptions(doctorId: String, locationId: String): List<com.example.newmedisync.model.ScheduleException> {
        return try {
            val snapshot = firestore.collection("doctor_verifications")
                .document(doctorId)
                .collection("exceptions")
                .get()
                .await()
            val targetLoc = locationId.ifBlank { "default_${doctorId}" }
            snapshot.toObjects(com.example.newmedisync.model.ScheduleException::class.java)
                .filter { exc ->
                    exc.locationId.isBlank() ||
                            exc.locationId == locationId ||
                            exc.locationId == targetLoc ||
                            locationId.isBlank() ||
                            locationId == "default_${doctorId}"
                }
                .sortedBy { it.date }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteDoctorException(doctorId: String, exceptionId: String) {
        firestore.collection("doctor_verifications")
            .document(doctorId)
            .collection("exceptions")
            .document(exceptionId)
            .delete()
            .await()
    }

    suspend fun getUpcomingAppointmentsForDate(doctorId: String, locationId: String, dateStr: String): List<Appointment> {
        return try {
            val snapshot = firestore.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("practiceLocationId", locationId)
                .whereEqualTo("date", dateStr)
                .whereEqualTo("status", "Upcoming")
                .get()
                .await()
            snapshot.toObjects(Appointment::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDoctorPatients(doctorUid: String): List<com.example.newmedisync.model.DoctorPatientItem> {
        return try {
            val apptSnapshot = firestore.collection("appointments")
                .whereEqualTo("doctorId", doctorUid)
                .get()
                .await()
            val appointments = apptSnapshot.toObjects(Appointment::class.java)

            val prescSnapshot = firestore.collection("prescriptions")
                .whereEqualTo("doctorUid", doctorUid)
                .get()
                .await()
            val prescriptions = prescSnapshot.toObjects(com.example.newmedisync.model.PrescriptionRecord::class.java)

            // Gather all unique patient UIDs
            val patientUids = mutableSetOf<String>()
            appointments.forEach { if (it.patientId.isNotBlank()) patientUids.add(it.patientId) }
            prescriptions.forEach { if (it.patientUid.isNotBlank()) patientUids.add(it.patientUid) }

            val now = System.currentTimeMillis()
            val resultList = mutableListOf<com.example.newmedisync.model.DoctorPatientItem>()

            for (pUid in patientUids) {
                val userDoc = firestore.collection("users").document(pUid).get().await()
                val patientDoc = firestore.collection("patients").document(pUid).get().await()

                val userName = userDoc.getString("name") ?: ""
                val userPhone = userDoc.getString("phone") ?: ""
                val userEmail = userDoc.getString("email") ?: ""

                val patientModel = patientDoc.toObject(com.example.newmedisync.model.PatientModel::class.java)

                val patientAppts = appointments.filter { it.patientId == pUid }
                val patientPrescs = prescriptions.filter { it.patientUid == pUid }

                val lastAppt = patientAppts.filter { it.timestamp <= now }.maxByOrNull { it.timestamp }
                val nextAppt = patientAppts.filter { it.timestamp > now && it.status.equals("Upcoming", ignoreCase = true) }
                    .minByOrNull { it.timestamp }

                val lastPresc = patientPrescs.maxByOrNull { it.timestamp }

                val lastVisitTs = maxOf(
                    lastAppt?.timestamp ?: 0L,
                    lastPresc?.timestamp ?: 0L
                )

                val lastVisitDateStr = when {
                    lastAppt != null -> "${lastAppt.date} • ${lastAppt.timeSlot}"
                    lastPresc != null -> lastPresc.date
                    else -> "No past visits"
                }

                val nextApptStr = if (nextAppt != null) "${nextAppt.date} • ${nextAppt.timeSlot}" else ""

                val displayName = if (userName.isNotBlank()) userName else {
                    patientAppts.firstOrNull()?.patientName ?: patientPrescs.firstOrNull()?.patientName ?: "Patient"
                }

                resultList.add(
                    com.example.newmedisync.model.DoctorPatientItem(
                        patientUid = pUid,
                        name = displayName,
                        phone = userPhone,
                        email = userEmail,
                        age = patientModel?.age ?: 0,
                        gender = patientModel?.gender ?: "",
                        bloodGroup = patientModel?.bloodGroup ?: "",
                        profileImageUrl = patientModel?.profileImageUrl ?: "",
                        lastVisitDate = lastVisitDateStr,
                        lastVisitTimestamp = lastVisitTs,
                        nextAppointmentDate = nextApptStr,
                        appointmentCount = patientAppts.size
                    )
                )
            }

            resultList.sortedByDescending { it.lastVisitTimestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCurrentDoctorUid(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentPatientUid(): String? {
        return auth.currentUser?.uid
    }
}

package com.example.newmedisync.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.newmedisync.MainActivity
import com.example.newmedisync.R
import com.example.newmedisync.firebase.NotificationRepository
import com.example.newmedisync.model.Appointment
import com.example.newmedisync.model.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {

    private const val CHANNEL_ID = "medisync_appointments_channel"
    private const val CHANNEL_NAME = "MediSync Appointments"
    private const val CHANNEL_DESC = "Notifications for MediSync appointment updates"
    private const val PERMISSION_REQUEST_CODE = 1001

    private var listenerRegistration: ListenerRegistration? = null
    private val notificationRepository = NotificationRepository()

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.medisynclogo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startAppointmentNotificationListener(context: Context) {
        stopAppointmentNotificationListener()

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val role = userDoc.getString("role") ?: "patient"
                if (role == "doctor") {
                    listenForDoctorAppointments(context, uid)
                } else {
                    listenForPatientAppointments(context, uid)
                }
            }
    }

    private fun listenForDoctorAppointments(context: Context, doctorUid: String) {
        val db = FirebaseFirestore.getInstance()
        var isInitial = true

        listenerRegistration = db.collection("appointments")
            .whereEqualTo("doctorId", doctorUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                if (isInitial) {
                    isInitial = false
                    return@addSnapshotListener
                }

                for (change in snapshot.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        val appt = change.document.toObject(Appointment::class.java)
                        val patientName = if (appt.patientName.isNotBlank()) appt.patientName else "A patient"
                        val title = "New Appointment Received! 📅"
                        val msg = "$patientName has booked an appointment for ${appt.date} at ${appt.timeSlot}."
                        
                        showNotification(context, title, msg)
                        persistNotif(doctorUid, "doctor", title, msg, "APPOINTMENT_BOOKED", appt.appointmentId)

                    } else if (change.type == DocumentChange.Type.MODIFIED) {
                        val appt = change.document.toObject(Appointment::class.java)
                        if (appt.status.equals("Cancelled", ignoreCase = true) && appt.cancelledBy.equals("PATIENT", ignoreCase = true)) {
                            val patientName = if (appt.patientName.isNotBlank()) appt.patientName else "A patient"
                            val title = "Appointment Cancelled ❌"
                            val msg = "Your appointment with $patientName on ${appt.date} at ${appt.timeSlot} has been cancelled."
                            
                            showNotification(context, title, msg)
                            persistNotif(doctorUid, "doctor", title, msg, "APPOINTMENT_CANCELLED", appt.appointmentId)
                        }
                    }
                }
            }
    }

    private fun listenForPatientAppointments(context: Context, patientUid: String) {
        val db = FirebaseFirestore.getInstance()
        var isInitial = true

        listenerRegistration = db.collection("appointments")
            .whereEqualTo("patientId", patientUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                if (isInitial) {
                    isInitial = false
                    return@addSnapshotListener
                }

                for (change in snapshot.documentChanges) {
                    if (change.type == DocumentChange.Type.MODIFIED) {
                        val appt = change.document.toObject(Appointment::class.java)
                        if (appt.status.equals("Completed", ignoreCase = true)) {
                            val doctorName = if (appt.doctorName.isNotBlank()) appt.doctorName else "Doctor"
                            val title = "Appointment Completed! ✓"
                            val msg = "Your appointment with $doctorName on ${appt.date} at ${appt.timeSlot} has been marked as completed."
                            
                            showNotification(context, title, msg)
                            persistNotif(patientUid, "patient", title, msg, "APPOINTMENT_COMPLETED", appt.appointmentId)

                        } else if (appt.status.equals("Cancelled", ignoreCase = true) && appt.cancelledBy.equals("DOCTOR", ignoreCase = true)) {
                            val doctorName = if (appt.doctorName.isNotBlank()) appt.doctorName else "Doctor"
                            val title = "Appointment Cancelled ❌"
                            val msg = "Dr. $doctorName has cancelled your appointment on ${appt.date} at ${appt.timeSlot}."
                            
                            showNotification(context, title, msg)
                            persistNotif(patientUid, "patient", title, msg, "APPOINTMENT_CANCELLED", appt.appointmentId)
                        }
                    }
                }
            }
    }

    private fun persistNotif(
        recipientUid: String,
        role: String,
        title: String,
        msg: String,
        type: String,
        appointmentId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notif = NotificationItem(
                    recipientUid = recipientUid,
                    recipientRole = role,
                    title = title,
                    message = msg,
                    type = type,
                    relatedAppointmentId = appointmentId,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
                notificationRepository.saveNotification(notif)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopAppointmentNotificationListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}

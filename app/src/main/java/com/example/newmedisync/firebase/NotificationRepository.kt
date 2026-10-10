package com.example.newmedisync.firebase

import com.example.newmedisync.model.NotificationItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveNotification(notification: NotificationItem) {
        if (notification.recipientUid.isBlank()) return
        
        val notifId = if (notification.notificationId.isNotBlank()) {
            notification.notificationId
        } else {
            val apptRef = if (notification.relatedAppointmentId.isNotBlank()) notification.relatedAppointmentId else "gen_${notification.timestamp}"
            "${notification.type}_${apptRef}_${notification.recipientUid}"
        }

        val finalNotif = notification.copy(notificationId = notifId)

        firestore.collection("notifications")
            .document(notification.recipientUid)
            .collection("items")
            .document(notifId)
            .set(finalNotif)
            .await()
    }

    fun listenToNotifications(
        recipientUid: String,
        onUpdate: (List<NotificationItem>) -> Unit
    ): ListenerRegistration? {
        if (recipientUid.isBlank()) return null

        return firestore.collection("notifications")
            .document(recipientUid)
            .collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.toObjects(NotificationItem::class.java)
                onUpdate(list)
            }
    }

    suspend fun markAsRead(recipientUid: String, notificationId: String) {
        if (recipientUid.isBlank() || notificationId.isBlank()) return
        try {
            firestore.collection("notifications")
                .document(recipientUid)
                .collection("items")
                .document(notificationId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun markAllAsRead(recipientUid: String) {
        if (recipientUid.isBlank()) return
        try {
            val snapshot = firestore.collection("notifications")
                .document(recipientUid)
                .collection("items")
                .whereEqualTo("isRead", false)
                .get()
                .await()

            if (snapshot.documents.isEmpty()) return

            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

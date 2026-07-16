package com.example.newmedisync.firebase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DoctorRepository {

    private val storage =
        FirebaseStorage.getInstance()

    suspend fun uploadFile(
        uri: Uri,
        folder: String
    ): String {

        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid

        val fileName =
            UUID.randomUUID().toString()

        val ref =
            storage.reference
                .child(folder)
                .child(uid)
                .child(fileName)

        ref.putFile(uri).await()

        return ref.downloadUrl.await().toString()
    }

}
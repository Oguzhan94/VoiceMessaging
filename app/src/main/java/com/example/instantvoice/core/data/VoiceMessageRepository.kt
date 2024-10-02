package com.example.instantvoice.core.data

import android.net.Uri
import android.util.Log
import com.example.instantvoice.core.data.model.MessageDTO
import com.example.instantvoice.core.data.model.RoomDTO
import com.example.instantvoice.core.data.repository.UserRepositoryImpl
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class VoiceMessageRepository @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val roomRepository: RoomRepository,
    private val userRepositoryImpl: UserRepositoryImpl
) {

    suspend fun uploadVoiceMessageToFirebase(filePath: String, userId: String): String? =
        withContext(Dispatchers.IO) {
            val fileUri = Uri.fromFile(File(filePath))
            val storageRef =
                firebaseStorage.reference.child("voice_messages/${userId}/${fileUri.lastPathSegment}")

            return@withContext try {
                storageRef.putFile(fileUri).await()
                storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun saveVoiceMessageToRoom(
        roomId: String,
        voiceMessageUrl: String,
        userId: String,
        userName: String
    ) {
        val newMessageDTO = MessageDTO(
            userId = userId,
            userName = userName,
            voiceMessageUrl = voiceMessageUrl,
            timestamp = Timestamp.now()
        )

        val roomRef = firestore.collection("rooms").document(roomId)

        firestore.runTransaction { transaction ->
            val roomSnapshot = transaction.get(roomRef)
            val roomDTO = roomSnapshot.toObject(RoomDTO::class.java)

            if (roomDTO != null) {
                val updatedMessages = roomDTO.messages.toMutableList().apply {
                    add(newMessageDTO)
                }

                transaction.update(roomRef, "messages", updatedMessages)
            }
        }.addOnSuccessListener {
            Log.d("Firestore", "Mesaj başarıyla eklendi.")

        }.addOnFailureListener { e ->
            Log.e("Firestore", "Mesaj eklenirken hata oluştu", e)
        }
    }
}


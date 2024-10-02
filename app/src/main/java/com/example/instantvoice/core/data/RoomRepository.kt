package com.example.instantvoice.core.data

import android.util.Log
import com.example.instantvoice.core.data.model.MessageDTO
import com.example.instantvoice.core.data.model.RoomDTO
import com.example.instantvoice.core.domain.repository.AuthRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firestoreStorage: FirebaseFirestore,
    private val authRepository: AuthRepository
) {

    suspend fun addRoom(name: String) {
        val roomId = UUID.randomUUID().toString()
        val roomData = hashMapOf(
            "roomId" to roomId,
            "name" to name,
            "created_at" to FieldValue.serverTimestamp(),
            "createdBy" to authRepository.getCurrentUserUid(),
            "members" to listOf(authRepository.getCurrentUserUid()),
            "messages" to listOf<MessageDTO>()

        )
        firestore.collection("rooms").document(roomId).set(roomData).await()
    }

    fun getUserRoomsFlow(userUid: String): Flow<List<RoomDTO>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = firestore.collection("rooms")
            .whereArrayContains("members", userUid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val roomDTOS = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(RoomDTO::class.java)?.copy(roomID = document.id)
                } ?: emptyList()

                trySend(roomDTOS).isSuccess
            }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getSingleRoom(roomUid: String): RoomDTO? {
        return try {
            val documentSnapshot = firestore.collection("rooms").document(roomUid).get().await()
            documentSnapshot.toObject(RoomDTO::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting room", e)
            null
        }
    }


    suspend fun addMember(roomId: String, userId: String) {
        firestore.collection("rooms").document(roomId)
            .update("members", FieldValue.arrayUnion(userId))
            .await()
    }

    suspend fun removeMember(roomId: String, userId: String) {
        firestore.collection("rooms").document(roomId)
            .update("members", FieldValue.arrayRemove(userId))
            .await()
    }

    fun getMessages(roomId: String, isUserAdmin: Boolean): Flow<List<MessageDTO>> = callbackFlow {
        val documentRef = firestore.collection("rooms").document(roomId)

        val listenerRegistration = documentRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val messages = snapshot?.get("messages") as? List<Map<String, Any>> ?: emptyList()

            val messageDTOObjects = messages.map { map ->
                MessageDTO(
                    userId = map["userId"] as? String ?: "",
                    userName = map["userName"] as? String ?: "",
                    voiceMessageUrl = map["voiceMessageUrl"] as? String ?: "",
                    timestamp = map["timestamp"] as? Timestamp
                )
            }

            val resultMessages = if (isUserAdmin) {
                messageDTOObjects
            } else {
                messageDTOObjects.takeLast(5)
            }

            trySend(resultMessages)
        }

        awaitClose { listenerRegistration.remove() }
    }


}

package com.example.instantvoice.core.data.repository

import com.example.instantvoice.core.data.model.UserDTO
import com.example.instantvoice.core.domain.repository.AuthRepository
import com.example.instantvoice.core.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
    private val authRepository: AuthRepository
) : UserRepository {
    override suspend fun createUser(userDTO: UserDTO) {
        val uid = authRepository.getCurrentUserUid()
        if (uid != null) {
            firestore.collection("users")
                .document(uid)
                .set(userDTO)
                .await()
        }
        updateUserFcmToken()
    }

    override suspend fun getAllUsers(): List<UserDTO> {
        return firestore.collection("users")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(UserDTO::class.java) }
    }

    override suspend fun getCurrentUser(): UserDTO? {
        val uid = authRepository.getCurrentUserUid() ?: return null
        val documentSnapshot = firestore.collection("users").document(uid).get().await()
        return documentSnapshot.toObject(UserDTO::class.java)
    }

    override suspend fun updateUserFcmToken() {
        val token = firebaseMessaging.token.await()
        val uid = authRepository.getCurrentUserUid() ?: return
        firestore.collection("users")
            .document(uid)
            .update("fcmToken", token)
            .await()
    }

    override suspend fun getUserFcmToken(uid: String): String? {
        val documentSnapshot = firestore.collection("users").document(uid).get().await()
        return documentSnapshot.getString("fcmToken")
    }
    
    override suspend fun updateUserMuteStatus(isMute: Boolean) {
        val userUid = authRepository.getCurrentUserUid() ?: return
        firestore.collection("users").document(userUid)
            .update("ismute", isMute)
            .await()
    }

}

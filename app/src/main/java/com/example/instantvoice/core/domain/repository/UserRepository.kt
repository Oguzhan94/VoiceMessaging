package com.example.instantvoice.core.domain.repository

import com.example.instantvoice.core.data.model.UserDTO

interface UserRepository {
    suspend fun createUser(userDTO: UserDTO)
    suspend fun getAllUsers(): List<UserDTO>
    suspend fun getCurrentUser(): UserDTO?
    suspend fun updateUserFcmToken()
    suspend fun getUserFcmToken(uid: String): String?
    suspend fun updateUserMuteStatus(isMute: Boolean)
}
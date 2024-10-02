package com.example.instantvoice.core.domain.usecase.auth

import com.example.instantvoice.core.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepositoryImpl: AuthRepositoryImpl
) {
    suspend fun execute(email: String, password: String): Result<FirebaseUser?> {
        val loginResult = authRepositoryImpl.login(email, password)
        return if (loginResult.isSuccess) {
            Result.success(loginResult.getOrNull())
        } else {
            Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
        }
    }
}
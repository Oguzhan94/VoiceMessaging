package com.example.instantvoice.core.domain.usecase.auth

import com.example.instantvoice.core.data.repository.AuthRepositoryImpl
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepositoryImpl: AuthRepositoryImpl
) {
    fun execute() {
        authRepositoryImpl.signOut()
    }
}
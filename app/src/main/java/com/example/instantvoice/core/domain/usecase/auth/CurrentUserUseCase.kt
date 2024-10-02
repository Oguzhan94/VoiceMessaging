package com.example.instantvoice.core.domain.usecase.auth

import com.example.instantvoice.core.data.repository.AuthRepositoryImpl
import javax.inject.Inject

class CurrentUserUseCase @Inject constructor(private val authRepositoryImpl: AuthRepositoryImpl) {
    operator fun invoke(): String? {
        return authRepositoryImpl.getCurrentUserUid()
    }

    fun execute(): String? {
        return authRepositoryImpl.getCurrentUserUid()
    }

}
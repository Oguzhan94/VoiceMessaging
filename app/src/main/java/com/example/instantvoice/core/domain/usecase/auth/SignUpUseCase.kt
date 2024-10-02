package com.example.instantvoice.core.domain.usecase.auth

import com.example.instantvoice.core.data.model.UserDTO
import com.example.instantvoice.core.data.repository.AuthRepositoryImpl
import com.example.instantvoice.core.data.repository.UserRepositoryImpl
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepositoryImpl: AuthRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl
) {
    suspend fun execute(userDTO: UserDTO, password: String): Result<Unit> {
        val signUpResult = authRepositoryImpl.signUp(userDTO.email, password)
        return if (signUpResult.isSuccess) {
            userDTO.uid = authRepositoryImpl.getCurrentUserUid()!!
            userRepositoryImpl.createUser(userDTO)
            Result.success(Unit)
        } else {
            Result.failure(signUpResult.exceptionOrNull() ?: Exception("Sign up failed"))
        }
    }
}
package com.example.instantvoice.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instantvoice.core.data.model.UserDTO
import com.example.instantvoice.core.domain.usecase.auth.LoginUseCase
import com.example.instantvoice.core.domain.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _registrationStatus = MutableLiveData<RegistrationStatus>()
    val registrationStatus: LiveData<RegistrationStatus> = _registrationStatus

    private val _loginStatus = MutableLiveData<LoginStatus>()
    val loginStatus: LiveData<LoginStatus> = _loginStatus

    fun validateInput(
        name: String = "",
        surname: String = "",
        email: String = "",
        password: String = ""
    ): Boolean {
        return name.isNotBlank() && surname.isNotBlank() && email.isNotBlank() && password.isNotBlank()
    }

    fun validateLogin(email: String = "", password: String = ""): Boolean {
        return email.isNotBlank() && password.isNotBlank()
    }

    fun signUp(userDTO: UserDTO, password: String) {
        viewModelScope.launch {
            val result = signUpUseCase.execute(userDTO, password)
            if (result.isSuccess) {
                _registrationStatus.value = RegistrationStatus.Success
            } else {
                _registrationStatus.value = RegistrationStatus.Error(
                    result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = loginUseCase.execute(email, password)
            if (result.isSuccess) {
                _loginStatus.value = LoginStatus.Success
            } else {
                _loginStatus.value =
                    LoginStatus.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    sealed class RegistrationStatus {
        object Success : RegistrationStatus()
        data class Error(val message: String) : RegistrationStatus()
    }

    sealed class LoginStatus {
        object Success : LoginStatus()
        data class Error(val message: String) : LoginStatus()
    }
}
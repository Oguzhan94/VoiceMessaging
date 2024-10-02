package com.example.instantvoice.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instantvoice.core.data.RoomRepository
import com.example.instantvoice.core.data.model.RoomDTO
import com.example.instantvoice.core.data.model.UserDTO
import com.example.instantvoice.core.data.repository.UserRepositoryImpl
import com.example.instantvoice.core.domain.usecase.auth.CurrentUserUseCase
import com.example.instantvoice.core.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val roomRepository: RoomRepository,
    private val currentUserUseCase: CurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _getRoomStatus = MutableLiveData<GetRoomStatus>()
    val getRoomStatus: LiveData<GetRoomStatus> = _getRoomStatus

    private val _currentUserDTO = MutableLiveData<UserDTO?>()
    val currentUserDTO: LiveData<UserDTO?> = _currentUserDTO

    private val _rooms = MutableLiveData<List<RoomDTO>>()
    val rooms: LiveData<List<RoomDTO>> get() = _rooms

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin


    fun getCurrentUser() {
        viewModelScope.launch {
            _currentUserDTO.value = userRepositoryImpl.getCurrentUser()
        }
    }

    fun startListeningToUserRooms() {
        viewModelScope.launch {
            try {
                val userUid = currentUserUseCase.invoke()
                roomRepository.getUserRoomsFlow(userUid!!).collect { userRooms ->
                    _rooms.value = userRooms
                    _getRoomStatus.value = GetRoomStatus.Success
                }
            } catch (e: Exception) {
                _getRoomStatus.value = GetRoomStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    private val _addRoomStatus = MutableLiveData<AddRoomStatus>()
    val addRoomStatus: LiveData<AddRoomStatus> = _addRoomStatus

    fun addRoom(name: String) {
        viewModelScope.launch {
            try {
                roomRepository.addRoom(name)
                _addRoomStatus.value = AddRoomStatus.Success
            } catch (e: Exception) {
                _addRoomStatus.value = AddRoomStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        signOutUseCase.execute()
    }

    fun setUserToken() {
        viewModelScope.launch {
            userRepositoryImpl.updateUserFcmToken()
        }
    }

    fun mute() {
        viewModelScope.launch {
            val currentUser = _currentUserDTO.value
            if (currentUser != null) {
                val newMuteStatus = !currentUser.ismute
                userRepositoryImpl.updateUserMuteStatus(newMuteStatus)

                _currentUserDTO.value = currentUser.copy(ismute = newMuteStatus)
            }
        }
    }


    sealed class GetRoomStatus {
        object Success : GetRoomStatus()
        data class Error(val message: String) : GetRoomStatus()
    }

    sealed class AddRoomStatus {
        object Success : AddRoomStatus()
        data class Error(val message: String) : AddRoomStatus()
    }

}

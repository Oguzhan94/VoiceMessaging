package com.example.instantvoice.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instantvoice.core.data.RoomRepository
import com.example.instantvoice.core.data.VoiceMessageRepository
import com.example.instantvoice.core.data.model.MessageDTO
import com.example.instantvoice.core.data.model.UserDTO
import com.example.instantvoice.core.data.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val voiceMessageRepository: VoiceMessageRepository,
) : ViewModel() {

    private val _users = MutableLiveData<List<UserDTO>>()
    val users: LiveData<List<UserDTO>> get() = _users

    private val _currentUserDTO = MutableLiveData<UserDTO>()
    val currentUserDTO: LiveData<UserDTO> get() = _currentUserDTO

    private val _messages = MutableLiveData<List<MessageDTO>>()
    val messages: LiveData<List<MessageDTO>> get() = _messages


    fun loadUsersInRoom(roomId: String) {
        viewModelScope.launch {
            try {

                val allUsers = userRepositoryImpl.getAllUsers()
                val room = roomRepository.getSingleRoom(roomId)

                if (room == null) {
                    Log.e("RoomViewModel", "Room with ID $roomId not found")
                    _users.postValue(emptyList())
                    return@launch
                }

                val roomMemberIds = room.members.toSet()
                val updatedUsers = allUsers.map { user ->
                    user.copy(inRoom = roomMemberIds.contains(user.uid))
                }
                Log.d("RoomViewModel", "Updated users: $updatedUsers")
                _users.postValue(updatedUsers)
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Error loading users", e)
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepositoryImpl.getCurrentUser()
                if (user != null) {
                    _currentUserDTO.postValue(user!!)
                }
            } catch (e: Exception) {
                Log.e("Current user", "Error loading users", e)
            }
        }
    }


    fun addMemberToRoom(roomId: String, userId: String) {
        viewModelScope.launch {
            try {
                roomRepository.addMember(roomId, userId)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading users", e)
            }
        }

    }

    fun removeMemberToRoom(roomId: String, userId: String) {
        viewModelScope.launch {
            try {
                roomRepository.removeMember(roomId, userId)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading users", e)
            }
        }
    }

    fun uploadVoiceMessage(roomId: String, filePath: String, userId: String) {
        viewModelScope.launch {
            val voiceMessageUrl =
                voiceMessageRepository.uploadVoiceMessageToFirebase(filePath, userId)
            if (voiceMessageUrl != null) {
                saveVoiceMessageToFireStore(roomId, voiceMessageUrl, userId)
            }
        }
    }

    fun loadMessages(roomId: String, isUserAdmin: Boolean) {
        viewModelScope.launch {
            roomRepository.getMessages(roomId, isUserAdmin).collect { messages ->
                _messages.value = messages
            }
        }
    }

    private suspend fun saveVoiceMessageToFireStore(
        roomId: String,
        voiceMessageUrl: String,
        userId: String,
        userName: String = _currentUserDTO.value!!.name + " " + _currentUserDTO.value!!.surname
    ) {
        voiceMessageRepository.saveVoiceMessageToRoom(roomId, voiceMessageUrl, userId, userName)
    }


}


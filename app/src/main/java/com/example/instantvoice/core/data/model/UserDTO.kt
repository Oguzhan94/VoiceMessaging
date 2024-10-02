package com.example.instantvoice.core.data.model

data class UserDTO(
    var uid: String = "",
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val admin: Boolean = false,
    val inRoom: Boolean = false,
    val fcmToken: String? = null,
    val ismute: Boolean = false
)
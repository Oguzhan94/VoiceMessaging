package com.example.instantvoice.core.data.model

import com.google.firebase.Timestamp

data class MessageDTO(
    val userId: String = "",
    val userName: String = "",
    val voiceMessageUrl: String = "",
    val timestamp: Timestamp? = null
)
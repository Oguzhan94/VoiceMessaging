package com.example.instantvoice.core.data.model

import java.util.Date

data class RoomDTO(
    val roomID: String = "",
    val name: String = "",
    val members: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Date? = null,
    val messages: List<MessageDTO> = emptyList()
)

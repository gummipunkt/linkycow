package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TagDto(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val createdAt: String,
    val updatedAt: String
) 
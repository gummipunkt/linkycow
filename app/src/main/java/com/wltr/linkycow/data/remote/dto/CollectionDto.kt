package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val iconWeight: String? = null,
    val color: String? = null,
    val parentId: Int? = null,
    val isPublic: Boolean,
    val ownerId: Int,
    val createdById: Int,
    val createdAt: String,
    val updatedAt: String
) 
package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LinkDetailResponse(
    val response: LinkDetailData
)

@Serializable
data class LinkDetailData(
    val id: Int,
    val name: String,
    val type: String,
    val description: String?,
    val createdById: Int,
    val collectionId: Int?,
    val icon: String?,
    val iconWeight: String?,
    val color: String?,
    val url: String,
    val textContent: String?,
    val preview: String?,
    val image: String?,
    val pdf: String?,
    val readable: String?,
    val monolith: String?,
    val lastPreserved: String?,
    val importDate: String?,
    val createdAt: String,
    val updatedAt: String,
    val tags: List<Tag> = emptyList(),
    val collection: Collection? = null
)

@Serializable
data class Tag(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class Collection(
    val id: Int,
    val name: String,
    val description: String?,
    val icon: String?,
    val iconWeight: String?,
    val color: String?,
    val parentId: Int?,
    val isPublic: Boolean,
    val ownerId: Int,
    val createdById: Int,
    val createdAt: String,
    val updatedAt: String,
    val pinnedBy: List<Int>? = null
) 
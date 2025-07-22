package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LinkResponse(
    val response: FullLinkData
)

@Serializable
data class FullLinkData(
    val id: Int,
    val name: String,
    val type: String,
    val description: String?,
    val createdById: Int,
    val collectionId: Int?,
    val icon: String?,
    val iconWeight: String?,
    val color: String? = null,
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
    val tags: List<FullLinkTag> = emptyList(),
    val collection: FullLinkCollection? = null
)

@Serializable
data class FullLinkTag(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class FullLinkCollection(
    val id: Int,
    val name: String,
    val description: String,
    val icon: String?,
    val iconWeight: String?,
    val color: String? = null,
    val parentId: Int?,
    val isPublic: Boolean,
    val ownerId: Int,
    val createdById: Int,
    val createdAt: String,
    val updatedAt: String,
    val pinnedBy: List<Int>? = null
) 
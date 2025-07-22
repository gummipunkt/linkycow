package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkRequest(
    val id: Int? = null, // für Update-Requests
    val url: String,
    val name: String? = null,
    val type: String? = "url", // url, pdf, image
    val description: String? = null,
    val tags: List<CreateLinkTag>? = null,
    val collection: CreateLinkCollection? = null
)

@Serializable
data class CreateLinkTag(
    val id: Int? = null,
    val name: String
)

@Serializable
data class CreateLinkCollection(
    val id: Int? = null,
    val name: String? = null,
    val ownerId: Int? = null
) 
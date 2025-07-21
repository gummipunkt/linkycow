package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class Link(
    val id: Int,
    val name: String,
    val url: String,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val type: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val tags: List<SimpleTag>? = null,
    val collection: SimpleCollection? = null
)

@Serializable
data class SimpleTag(
    val id: Int,
    val name: String
)

@Serializable
data class SimpleCollection(
    val id: Int,
    val name: String,
    val color: String? = null
) 
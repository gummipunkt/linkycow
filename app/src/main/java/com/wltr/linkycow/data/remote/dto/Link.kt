package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class Link(
    val id: Int,
    val name: String,
    val url: String,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null
) 
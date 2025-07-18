package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkRequest(
    val url: String,
    val name: String? = null,
    val description: String? = null
) 
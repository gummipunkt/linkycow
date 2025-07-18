package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val response: TokenPayload
)

@Serializable
data class TokenPayload(
    val token: String
) 
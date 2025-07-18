package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val sessionName: String = "LinkyCow Android Client" // Default session name
) 
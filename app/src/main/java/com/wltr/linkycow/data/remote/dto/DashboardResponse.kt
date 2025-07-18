package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
    val data: DashboardData
)

@Serializable
data class DashboardData(
    val links: List<Link>
) 
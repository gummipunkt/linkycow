package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val message: String,
    val data: SearchData
)

@Serializable
data class SearchData(
    val nextCursor: Int?,
    val links: List<Link>
) 
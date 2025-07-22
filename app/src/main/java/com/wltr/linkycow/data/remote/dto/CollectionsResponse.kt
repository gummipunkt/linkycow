package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionsResponse(
    val response: List<CollectionDto>
) 
package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TagsResponse(
    val response: List<TagDto>
) 
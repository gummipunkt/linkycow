package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FilteredLinksResponse(
    val response: List<Link>
) 
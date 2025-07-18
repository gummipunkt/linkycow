package com.wltr.linkycow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeleteLinkResponse(
    val response: LinkDetailData
) 
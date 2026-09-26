package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommandResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "mode") val mode: String? = null,
    @Json(name = "message") val message: String? = null
)

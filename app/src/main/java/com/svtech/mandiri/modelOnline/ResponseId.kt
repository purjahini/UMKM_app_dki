package com.svtech.mandiri.modelOnline

data class ResponseId(
    val api_message: String,
    val api_status: Int,
    val `data`: Data
) {
    data class Data(
        val id: Int
    )
}
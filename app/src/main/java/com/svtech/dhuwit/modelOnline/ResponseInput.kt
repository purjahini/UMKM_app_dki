package com.svtech.dhuwit.modelOnline

data class ResponseInput(
    val api_message: String?,
    val api_status: Int?,
    val `data`: Data?
) {
    data class Data(
        val id: Int?
    )
}
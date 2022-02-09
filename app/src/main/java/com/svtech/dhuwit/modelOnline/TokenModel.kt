package com.svtech.dhuwit.modelOnline

data class TokenModel(
    val api_message: String?,
    val api_status: Int?,
    val `data`: Data?
) {
    data class Data(
        val access_token: String?,
        val expiry: Int?
    )
}
package com.svtech.mandiri.modelOnline

data class ResponseBilboardSaldo(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>,
    val saldo: Int
) {
    data class Data(
        val created_at: String,
        val foto: String,
        val id: Int,
        val link: String,
        val status: String,
        val updated_at: String
    )
}
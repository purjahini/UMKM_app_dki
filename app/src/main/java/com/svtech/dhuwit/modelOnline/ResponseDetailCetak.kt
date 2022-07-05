package com.svtech.dhuwit.modelOnline

data class ResponseDetailCetak(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>
) {
    data class Data(
        val invoice: String,
        val nominal: Int,
        val tanggal: String
    )
}
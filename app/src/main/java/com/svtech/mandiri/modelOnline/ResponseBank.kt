package com.svtech.mandiri.modelOnline

data class ResponseBank(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>
) {
    data class Data(
        val atas_nama: String,
        val id: Int,
        val nama_bank: String,
        val no_rek: String,
        val status: Int,
        val username: String
    )
}
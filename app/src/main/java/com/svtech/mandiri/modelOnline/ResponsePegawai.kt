package com.svtech.mandiri.modelOnline

data class ResponsePegawai(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>
) {
    data class Data(
        val foto: String,
        val id: Int,
        val kontak: String,
        val nama: String,
        val password: String,
        val role: String,
        val username: String
    )
}
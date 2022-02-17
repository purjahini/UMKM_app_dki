package com.svtech.dhuwit.modelOnline

data class ProfileOnline(
    val api_message: String?,
    val api_status: Int?,
    val `data`: Data?
) {
    data class Data(
        val alamat_toko: String?,
        val kode: String?,
        val logo_toko: Any?,
        val nama_toko: String?,
        val username: String?,
        val id: Int?
    )
}
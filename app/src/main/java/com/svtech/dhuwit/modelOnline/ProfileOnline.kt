package com.svtech.dhuwit.modelOnline

data class ProfileOnline(
    val api_message: String?,
    val api_status: Int?,
    val `data`: Data?
) {
    data class Data(
        val ALAMAT_TOKO: String?,
        val KODE: String?,
        val LOGO_TOKO: Any?,
        val NAMA_TOKO: String?,
        val USERNAME: String?,
        val id: Int?
    )
}
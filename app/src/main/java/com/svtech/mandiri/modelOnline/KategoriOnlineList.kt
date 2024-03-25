package com.svtech.mandiri.modelOnline

data class KategoriOnlineList(
    val api_message: String?,
    val api_status: Int?,
    val `data`: List<Data?>?
) {
    data class Data(
        var KATEGORI_GAMBAR: String?,
        var KATEGORI_NAMA: String?,
        val KATEGORI_USERNAME: String?,
        val id: Int?
    )
}
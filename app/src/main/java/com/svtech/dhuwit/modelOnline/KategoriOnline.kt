package com.svtech.dhuwit.modelOnline

data class KategoriOnline(
    val api_message: String?,
    val api_status: Int?,
    val `data`: MutableList<Data?>?
) {
    data class Data(
        var KATEGORI_GAMBAR: String?,
        var KATEGORI_NAMA: String?,
        val KATEGORI_USERNAME: String?,
        val id: Int?
    )
}
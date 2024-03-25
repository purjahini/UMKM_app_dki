package com.svtech.mandiri.modelOnline

data class KategoriOnline(
    val api_message: String?,
    val api_status: Int?,
    val `data`: MutableList<Data?>?
) {
    data class Data(
        var kategori_gambar: String?,
        var kategori_nama: String?,
        val username: String?,
        val id: Int?
    )

}

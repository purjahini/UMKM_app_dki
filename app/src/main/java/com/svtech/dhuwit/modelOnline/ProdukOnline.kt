package com.svtech.dhuwit.modelOnline

data class ProdukOnline(
    val api_message: String?,
    val api_status: Int?,
    val `data`: MutableList<Data?>?
) {
    data class Data(
        val DISKON: Int?,
        val KATEGORI_GAMBAR: String?,
        val KATEGORI_NAMA: String?,
        val FOTO: String?,
        val HARGA: Int?,
        var KATEGORI: Int?,
        val MINIMAL_PEMBELIAN: Int?,
        val NAMA: String?,
        val SATUAN: String?,
        val STOK: Int?,
        val USERNAME: String?,
        val id: Int?
    )
}
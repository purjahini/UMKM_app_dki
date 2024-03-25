package com.svtech.mandiri.modelOnline

data class ProdukOnline(
    val api_message: String?,
    val api_status: Int?,
    val `data`: MutableList<Data?>?
) {
    data class Data(
        var diskon: Int?,
        val kategori_gambar: String?,
        val kategori_nama: String?,
        var foto: String?,
        var harga: Int?,
        var kategori: Int?,
        var minimal_pembelian: Int?,
        var nama: String?,
        var satuan: String?,
        var stok: Int?,
        val username: String?,
        val id: Int?
    )
}
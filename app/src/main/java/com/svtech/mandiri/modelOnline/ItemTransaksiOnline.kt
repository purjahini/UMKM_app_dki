package com.svtech.mandiri.modelOnline

data class ItemTransaksiOnline(
    val api_message: String?,
    val api_status: Int?,
    val `data`: MutableList<Data?>
) {
    data class Data(
        val diskon_produk: Int?,
        val foto_produk: String?,
        val harga_produk: Int?,
        val id: Int?,
        val id_transaksi: Int?,
        val jumlah: Int?,
        val kategori: Int?,
        val minimal_pembelian: Int?,
        val nama_produk: String?,
        val produk_id: Int?,
        val satuan: String?,
        val stok_produk: Int?,
        val username: String?
    )
}
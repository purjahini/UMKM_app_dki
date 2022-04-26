package com.svtech.dhuwit.modelOnline

data class ResponeItemProdukIdTransaksi(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Any>,
    val item_produk: List<ItemProduk>
) {
    data class ItemProduk(
        val harga_produk: Int,
        val jumlah: Int,
        val nama_produk: String
    )
}
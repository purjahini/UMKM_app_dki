package com.svtech.mandiri.modelOnline

data class item_transaksi_get_id_transaksi(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>
) {
    data class Data(
        val id: Int,
        val id_transaksi: Int,
        val jumlah: Int,
        val produk_id: Int,
        val username: String
    )
}
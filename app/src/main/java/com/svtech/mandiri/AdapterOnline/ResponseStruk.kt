package com.svtech.mandiri.AdapterOnline

data class ResponseStruk(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>,
    val item_produk: List<ItemProduk>
) {
    data class Data(
        val alamat_toko: String,
        val bank: String,
        val invoice: String,
        val nama_toko: String,
        val nokartu: String,
        val saldoakhir: String,
        val saldoawal: String,
        val tanggal_transaksi: String,
        val tid: String,
        val created_at : String,
        val total_pembayaran: Int,
        val casier : String
    )

    data class ItemProduk(
        val jumlah: Int,
        val nama_produk: String,
        val harga_produk: Int

    )
}
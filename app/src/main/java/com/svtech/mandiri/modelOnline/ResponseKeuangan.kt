package com.svtech.mandiri.modelOnline

data class ResponseKeuangan(
    val api_message: String,
    val api_status: String,
    val `data`: List<Data>,
    val filter: String,
    val total_pembayaran: Int,
    val total_pembayaran_hari_ini: Int,
    val transaksi_suskes_hari_ini: Int,
    val transaksi_pending: Int,
    val transaksi_pending_hari_ini: Int,
    val transaksi_sukses: Int
) {
    data class Data(
        val tanggal_transaksi: String,
        val total_pembayaran: Int,
        val transaksi_pending: Int,
        val transaksi_sukses: Int
    )
}
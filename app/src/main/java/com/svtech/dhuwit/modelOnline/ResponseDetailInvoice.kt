package com.svtech.dhuwit.modelOnline

data class ResponseDetailInvoice(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>
) {
    data class Data(
        val id: Int,
        val invoice: String,
        val nama_pembeli: String,
        val status: Int,
        val tanggal_transaksi: String,
        val total_pembayaran: Int,
        val username: String
    )
}
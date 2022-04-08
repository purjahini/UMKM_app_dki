package com.svtech.dhuwit.modelOnline

data class ResponseTransaksiTodays(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>
) {
    data class Data(
        val tanggal: String,
        val total: String,
        val jumlah_trx:String
    )
}
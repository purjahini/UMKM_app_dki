package com.svtech.mandiri.modelOnline

data class ResponseMenuNeraca(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>,
    val debet_current : String,
    val kredit_current : String,
    val debet_prev : String,
    val kredit_prev : String,
    val history_neraca: List<HistoryNeraca>
) {
    data class Data(
        val debet: String,
        val kredit: String,
        val tanggal: String
    )

    data class HistoryNeraca(
        val created_at: String,
        val debet: Int,
        val id: Int,
        val kredit: Int,
        val saldo: Int,
        val tanggal: String,
        val updated_at: Any,
        val uraian: String,
        val username: String
    )
}
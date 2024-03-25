package com.svtech.mandiri.modelOnline

data class ResponseConfig(
    val code: String,
    val data_bank: List<DataBank>,
    val data_config: DataConfig,
    val info_umum: InfoUmum,
    val message: String,
    val status: Boolean
) {
    data class DataBank(
        val bank: String,
        val device_id: String,
        val ip_sam: String,
        val married_code: Any,
        val mid: String,
        val port: String,
        val processing_code: String,
        val tid: String
    )

    data class DataConfig(
        val device_id: String,
        val ip_sam: String,
        val port: String
    )

    data class InfoUmum(
        val alamatlokasi: String,
        val idhost: String,
        val kodelokasi: String,
        val lokasi: String,
        val pesankeluar: String,
        val pesanmasuk: String,
        val shift: String
    )
}
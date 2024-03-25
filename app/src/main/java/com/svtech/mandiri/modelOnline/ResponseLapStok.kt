package com.svtech.mandiri.modelOnline

data class ResponseLapStok(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>,
    val data_toko: List<DataToko>
) {
    data class Data(
        val kategori_id: Int,
        val kategori_nama: String,
        val nama_produk: String,
        val stok: Int
    )

    data class DataToko(
        val alamat_toko: String,
        val nama_toko: String
    )
}
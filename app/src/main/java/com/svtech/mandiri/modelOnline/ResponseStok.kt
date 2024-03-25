package com.svtech.mandiri.modelOnline

data class ResponseStok(
    val api_message: String,
    val api_status: Int,
    val `data`: List<Data>
) {
    data class Data(
        val foto: String,
        val id_stok: Int,
        val id_tambah:Int,
        val jumlah: Int,
        val tanggal:String,
        val nama_produk: String,
        val satuan: String
    )
}
package com.svtech.dhuwit.Models

import com.orm.SugarRecord

data class ItemTransaksi(
    var jumlah: Int? = 0,
    var namaProduk: String? = null,
    var hargaProduk: Double? = 0.0,
    var kategori: String? = null,
    var fotoProduk: String? = null,
    var minimalPembelianProduk: Int? = 0,
    var diskonProduk: Double? = 0.0,
    var stokProduk: Int? = 0,
    var satuan: String? = null,
    var produkId: Long? = 0,
    val idTransaksi: Long? = 0
) : SugarRecord<ItemTransaksi>()

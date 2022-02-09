package com.svtech.dhuwit.Models

import com.orm.SugarRecord

data class Produk(
    var nama: String? = null,
    var kategori: Int? = null,
    var harga: Double? = null,
    var foto: String? = null,
    var diskon: Int? = null,
    var minimalPembelian: Int? = null,
    var stok: Int? = null,
    var satuan: String? = null
) : SugarRecord<Produk>()
package com.svtech.mandiri.Models

import com.orm.SugarRecord

data class Stok(
    var jumlah: Int? = null,
    var isTambah: Boolean? = null,
    var idProduk: Long? = null,
    var tanggal: String? = null
) : SugarRecord<Stok>()
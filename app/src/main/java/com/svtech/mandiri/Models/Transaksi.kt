package com.svtech.mandiri.Models

import com.orm.SugarRecord

data class Transaksi(
    var status: Boolean? = false,
    var tanggalTrasaksi: String? = null,
    var namaPembeli: String? = "n/a",
    var diskon: Double? = 0.0,
    var totalPembayaran: Double? = 0.0,
    var bayar: Double? = 0.0,
    var invoice : String? =""
): SugarRecord<Transaksi>()
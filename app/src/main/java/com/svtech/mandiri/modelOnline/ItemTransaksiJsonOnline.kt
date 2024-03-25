package com.svtech.mandiri.modelOnline


class ItemTransaksiJsonOnline(
        val diskon_produk: Int?,
        val foto_produk: String?,
        val harga_produk: Int?,
        val id_transaksi: Int?,
        val jumlah: Int?,
        val minimal_pembelian: Int?,
        val nama_produk: String?,
        val produk_id: Int?,
        val satuan: String?,
        val stok_produk: Int?,
        val username: String?
    )

class  ItemUpdateStok(
        val id: Int,
        val id_transaksi: Int,
        val jumlah: Int,
        val produk_id: Int,
        val username: String
)

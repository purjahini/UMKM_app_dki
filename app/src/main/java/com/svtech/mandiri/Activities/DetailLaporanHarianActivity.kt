package com.svtech.mandiri.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.orm.SugarRecord
import com.svtech.mandiri.Models.ItemTransaksi
import com.svtech.mandiri.Models.Transaksi
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.numberToCurrency
import com.svtech.mandiri.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_detail_laporan_penjualan.table
import kotlinx.android.synthetic.main.activity_detail_laporan_penjualan.tvTotalPendapatan
import kotlinx.android.synthetic.main.activity_detail_laporan_penjualan.tvTotalProduk
import kotlinx.android.synthetic.main.layout_table_row_detail_penjualan.view.*
import java.text.SimpleDateFormat

class DetailLaporanHarianActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_laporan_harian)
        setToolbar(this,"Laporan Penjualan Harian")

        val tanggal = intent.getStringExtra("tanggal")
        if(tanggal != null){
            val transaksi = SugarRecord.listAll(Transaksi::class.java).filter { l -> l.status == false && l.tanggalTrasaksi?.substring(0, l.tanggalTrasaksi!!.indexOf(" ")).equals(tanggal) }
            var totItemTransaksi = mutableListOf<ItemTransaksi>()
            var total = 0.0
            var totalProduk = 0
            if(transaksi.isNotEmpty()){
                for (item in transaksi){
                    val itemTransaksi = SugarRecord.find(ItemTransaksi::class.java,"id_transaksi = ?",item.id.toString())

                    if(itemTransaksi.isNotEmpty()){
                        for (it in itemTransaksi){
                            totItemTransaksi.add(it)
                            totalProduk += it.jumlah!!
                        }
                    }
                    total += item.totalPembayaran!!
                }
            }
            val itemTransaksiDistinc = totItemTransaksi.distinctBy { l -> l.produkId }
            for (it in itemTransaksiDistinc){
                val row = LayoutInflater.from(this).inflate(R.layout.layout_table_row_detail_penjualan, table, false)
                val count = totItemTransaksi.filter { i -> i.produkId == it.produkId }
                var jml = 0
                count.forEach { s -> jml += s.jumlah!! }
                row.tvNamaProduk.text = it.namaProduk
                row.tvJumlah.text = "$jml ${it.satuan}"
                table.addView(row)
            }

            val date = SimpleDateFormat("dd/MM/yyyy").parse(tanggal)
//            tvTanggal.text = ": " +SimpleDateFormat("dd MMMM yyyy").format(date)
            tvTotalPendapatan.text = ": " +numberToCurrency(total)
            tvTotalProduk.text = ": " +totalProduk.toString() + " Produk"
        }
    }
}
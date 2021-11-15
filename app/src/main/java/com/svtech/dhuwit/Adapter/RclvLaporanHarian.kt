package com.svtech.dhuwit.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.orm.SugarRecord
import com.svtech.dhuwit.Activities.DetailLaporanHarianActivity
import com.svtech.dhuwit.Activities.LaporanHarianActivity
import com.svtech.dhuwit.Models.Transaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.numberToCurrency
import kotlinx.android.synthetic.main.layout_item_laporan_harian.view.*
import kotlinx.android.synthetic.main.layout_item_laporan_harian.view.btnDetail
import kotlinx.android.synthetic.main.layout_item_laporan_harian.view.btnSavePdf
import kotlinx.android.synthetic.main.layout_item_laporan_harian.view.tvTotalPendapatan
import java.text.SimpleDateFormat

/*Adapter recycler view untuk menapilkan item laporan penjualan*/
class RclvLaporanHarian(val context: Context, var listTransaksi: MutableList<Transaksi>) :
    RecyclerView.Adapter<RclvLaporanHarian.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(transaksi: Transaksi) {
            val date = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(transaksi.tanggalTrasaksi)
            val iTransaksi = SugarRecord.listAll(Transaksi::class.java).filter { l ->
                l.status == false && l.tanggalTrasaksi?.substring(
                    0,
                    l.tanggalTrasaksi?.indexOf(" ")!!
                ).equals(
                    transaksi.tanggalTrasaksi?.substring(
                        0,
                        transaksi.tanggalTrasaksi?.indexOf(" ")!!
                    )!!
                )
            }
            var total = 0.0
            if (iTransaksi.isNotEmpty()) {
                for (item in iTransaksi) {
                    total += item.totalPembayaran!!
                }
            }
            itemView.tvTglPenjualan.text = SimpleDateFormat("dd MMMM yyyy").format(date).toString()
            itemView.tvTotalPendapatan.text = numberToCurrency(total)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_laporan_harian, parent, false)
        )
    }

    override fun getItemCount(): Int = listTransaksi.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaksi = listTransaksi[position]
        holder.bind(transaksi)
        holder.itemView.btnDetail.setOnClickListener {
            val inten = Intent(context, DetailLaporanHarianActivity::class.java)
            inten.putExtra(
                "tanggal",
                transaksi.tanggalTrasaksi?.substring(0, transaksi.tanggalTrasaksi?.indexOf(" ")!!)
            )
            context.startActivity(inten)
        }

        holder.itemView.btnSavePdf.setOnClickListener {
            (context as LaporanHarianActivity).savePDF(transaksi)
        }
    }
}
package com.svtech.dhuwit.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.orm.SugarRecord
import com.svtech.dhuwit.Activities.DetailLaporanPenjualanActivity
import com.svtech.dhuwit.Activities.LaporanPenjualanActivity
import com.svtech.dhuwit.Models.Transaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.numberToCurrency
import kotlinx.android.synthetic.main.layout_item_laporan_penjualan.view.*

/*Adapter recycler view untuk menapilkan item laporan penjualan*/
class RclvLaporanPenjualan(val context: Context, var listTransaksi : MutableList<Transaksi>): RecyclerView.Adapter<RclvLaporanPenjualan.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(transaksi: Transaksi) {
            val iTransaksi = SugarRecord.listAll(Transaksi::class.java).filter { l -> l.status == false && l.namaPembeli.equals(transaksi.namaPembeli)}
            var total = 0.0
            if(iTransaksi.isNotEmpty()){
                for (item in iTransaksi){
                    total += item.totalPembayaran!!
                }
            }
            itemView.tvNamaPembeli.text = transaksi.namaPembeli
            itemView.tvTotalPendapatan.text = numberToCurrency(total)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_item_laporan_penjualan, parent, false))
    }

    override fun getItemCount(): Int = listTransaksi.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaksi = listTransaksi[position]
        holder.bind(transaksi)
        holder.itemView.btnDetail.setOnClickListener {
            val intent = Intent(context, DetailLaporanPenjualanActivity::class.java)
            intent.putExtra("nama",transaksi.namaPembeli)
            context.startActivity(intent)
        }

        holder.itemView.btnSavePdf.setOnClickListener {
//            (context as LaporanPenjualanActivity).savePDF(transaksi)
        }
    }
}
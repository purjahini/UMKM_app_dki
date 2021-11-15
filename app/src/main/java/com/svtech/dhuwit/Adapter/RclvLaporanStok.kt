package com.svtech.dhuwit.Adapter

import android.content.Context
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.Produk
import com.svtech.dhuwit.Models.Stok
import com.svtech.dhuwit.R
import kotlinx.android.synthetic.main.layout_item_laporan_stok.view.*
import kotlinx.android.synthetic.main.layout_item_produk.view.tvNamaProduk
import java.text.SimpleDateFormat

class RclvLaporanStok(val context: Context, var listStok: MutableList<Stok>) :
    RecyclerView.Adapter<RclvLaporanStok.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(context: Context, stok: Stok) {
            //Find produk
            val produk =
                SugarRecord.find(Produk::class.java, "id = ?", stok.idProduk.toString())
                    .first()
            //Update UI
            Glide.with(context).load(Base64.decode(produk.foto, Base64.DEFAULT)).fitCenter()
                .into(itemView.imgProduk)
            itemView.tvNamaProduk.text = produk.nama
            val date = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(stok.tanggal)
            itemView.tvTanggal.text = SimpleDateFormat("dd MMMM yyyy").format(date).toString()
            itemView.tvSatuan.text = produk.satuan
            if (stok.isTambah == true) {
                itemView.tvJumlah.text = stok.jumlah.toString()
                itemView.tvJumlah.setTextColor(Color.GREEN)
            } else {
                itemView.tvJumlah.text = stok.jumlah.toString()
                itemView.tvJumlah.setTextColor(Color.RED)
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_laporan_stok, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stok = listStok[position]
        holder.bind(context, stok)
    }

    override fun getItemCount(): Int {
        return listStok.size
    }
}
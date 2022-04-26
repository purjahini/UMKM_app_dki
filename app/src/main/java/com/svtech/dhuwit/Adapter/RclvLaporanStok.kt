package com.svtech.dhuwit.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svtech.dhuwit.R
import com.svtech.dhuwit.modelOnline.ResponseLapStok
import kotlinx.android.synthetic.main.layout_item_laporan_stok.view.*

class RclvLaporanStok(val context: Context, var listStok: MutableList<ResponseLapStok.Data>) :
    RecyclerView.Adapter<RclvLaporanStok.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(context: Context, stok: ResponseLapStok.Data) {
            //Find produk

            itemView.tvNamaProduk.text = stok.nama_produk
            itemView.tvProdukKategori.text = stok.kategori_nama
            itemView.tvNO.text = adapterPosition.plus(1).toString()
            itemView.tvJumlah.text = stok.stok.toString()
//            val date = SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(stok.tanggal)
//            itemView.tvTanggal.text = SimpleDateFormat("dd MMMM yyyy").format(date).toString()

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
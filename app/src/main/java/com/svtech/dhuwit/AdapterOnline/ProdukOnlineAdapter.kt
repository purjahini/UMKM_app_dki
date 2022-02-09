package com.svtech.dhuwit.AdapterOnline

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svtech.dhuwit.R
import com.svtech.dhuwit.modelOnline.ProdukOnline
import kotlinx.android.synthetic.main.layout_item_produk.view.*

class ProdukOnlineAdapter(val listProduk: List<ProdukOnline.Data?>?) :
    RecyclerView.Adapter<MvpHolder>() {
    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MvpHolder {
        context = parent.context
        return MvpHolder(
            LayoutInflater.from(context).inflate(
                R.layout.layout_item_produk,
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int = listProduk!!.size

    override fun onBindViewHolder(holder: MvpHolder, position: Int) {
        val data = listProduk?.get(position)
        val namaProduk = holder.itemView.tvNamaProduk
        val namaKategori = holder.itemView.tvKategori
        val fotoProduk = holder.itemView.imgFoto
        val hargaProduk = holder.itemView.tvHargaProduk
        val stokProduk = holder.itemView.tvStok
        val iconStok = holder.itemView.imgStokHabis
        if (data != null) {
            if (data.STOK == 0) {
                iconStok.visibility = View.VISIBLE
            } else {
                iconStok.visibility = View.INVISIBLE
            }
            namaProduk.text = data.NAMA
            namaKategori.text = data.KATEGORI_NAMA
            hargaProduk.text = "Rp. ${data.HARGA}"
            stokProduk.text = "STOK = ${data.STOK}"
            Glide.with(context).load(data.FOTO).fitCenter()
                .into(fotoProduk)

        }

    }

}

package com.svtech.mandiri.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svtech.mandiri.Activities.*
import com.svtech.mandiri.Models.Menu
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.See
import kotlinx.android.synthetic.main.layout_item_menu.view.*

/*Adapter recycler view untuk menu dashboard*/
class RclvItemMenu(val context: Context, var listItemMenu: MutableList<Menu>) :
    RecyclerView.Adapter<RclvItemMenu.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(menu: Menu, context: Context) {
            itemView.iconMenu.setImageDrawable(context.getDrawable(menu.icon!!))
            itemView.namaMenu.text = menu.nama
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvItemMenu.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_menu, parent, false)
        )
    }

    override fun getItemCount(): Int = listItemMenu.size

    override fun onBindViewHolder(holder: RclvItemMenu.ViewHolder, position: Int) {
        val item = listItemMenu[position]
        holder.onBind(item, context)
        holder.itemView.setOnClickListener {
            when(item.nama){
                "Penjualan" ->{
                    context.startActivity(Intent(context,MenuPembelianActivity::class.java))
                }
                "Keranjang"  ->{
                    context.startActivity(Intent(context,MenuKeranjangActivity::class.java))
                }
                "Kategori"  ->{
                    context.startActivity(Intent(context,MenuTambahKategoriActivity::class.java))
                }
                "Produk"  ->{
                    context.startActivity(Intent(context,MenuTambahProdukActivity::class.java))
                }
                "Pegawai"  ->{
                    context.startActivity(Intent(context,MenuTambahPegawaiActivity::class.java))
                }
                "Laporan" ->{
                    context.startActivity(Intent(context,LaporanActivity::class.java))
                }
                "Neraca" -> {
                    context.startActivity(Intent(context,NeracaActivity::class.java))
                }
                "TOPUP" -> {
                    See.toast(context,context.getString(R.string.label_underdevelopment))
                }

            }
        }
    }

}
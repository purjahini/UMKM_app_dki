package com.svtech.dhuwit.AdapterOnline

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.svtech.dhuwit.Activities.AddKategoriActivity
import com.svtech.dhuwit.Models.Kategori
import com.svtech.dhuwit.R
import kotlinx.android.synthetic.main.layout_item_kategori.view.*

/*Adapter recycler view untuk menapilkan item kategori*/
class RclvKategoriOnline(val context: Context, var listKategori : MutableList<Kategori>): RecyclerView.Adapter<RclvKategoriOnline.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(kategori: Kategori, context: Context){
            Glide.with(context).load(Base64.decode(kategori.gambar, Base64.DEFAULT)).fitCenter().into(itemView.imgFoto)
            itemView.tvKategori.text = kategori.nama
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_item_kategori, parent, false))
    }

    override fun getItemCount(): Int = listKategori.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kategori = listKategori[position]
        holder.bind(kategori, context)
        holder.itemView.setOnClickListener {
            val popupMenu = PopupMenu(context, it)
            popupMenu.menuInflater.inflate(R.menu.menu_edit_item, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {item ->
                when(item.itemId){
                    R.id.menuEdit -> editItem(kategori)
                    R.id.menuHapus -> hapusItem(kategori)
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
    }

    private fun hapusItem(kategori: Kategori) {
        MaterialAlertDialogBuilder(context).setTitle("Hapus").setMessage("Apakah anda yakin ingin menghapus?")
            .setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                listKategori.remove(kategori)
                kategori.delete()
                notifyDataSetChanged()
            })
            .setNegativeButton("Batal", DialogInterface.OnClickListener { dialogInterface, i ->  dialogInterface.dismiss()})
            .show()
    }

    private fun editItem(kategori: Kategori) {
        val intent = Intent(context, AddKategoriActivity::class.java)
        intent.putExtra("kategori", kategori.id)
        intent.putExtra("update", true)
        context.startActivity(intent)
    }
}
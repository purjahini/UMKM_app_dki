package com.svtech.dhuwit.Adapter

import android.content.Context
import android.content.DialogInterface
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.svtech.dhuwit.Activities.MenuKeranjangActivity
import com.svtech.dhuwit.Models.ItemTransaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.numberToCurrency
import kotlinx.android.synthetic.main.layout_item_keranjang.view.*

/*Adapter recycler view untuk menampilkan item transaksi*/
class RclvItemTransaksi(val context: Context, var listItemTransaksi: MutableList<ItemTransaksi>) :
    RecyclerView.Adapter<RclvItemTransaksi.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(itemTransaksi: ItemTransaksi, context: Context) {
            val activity = context as MenuKeranjangActivity
            itemView.tvNamaProduk.text = itemTransaksi.namaProduk
            itemView.tvKategori.text = itemTransaksi.kategori
            itemView.tvHargaProduk.text = numberToCurrency(itemTransaksi.hargaProduk!!)
            itemView.tvStok.text = "Stok : " + itemTransaksi.stokProduk
            Glide.with(itemView).load(itemTransaksi.fotoProduk)
                .placeholder(R.drawable.logo)
                .fitCenter().into(itemView.imgFoto)
            itemView.tvJumlah.text = itemTransaksi.jumlah.toString()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvItemTransaksi.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_keranjang, parent, false)
        )
    }

    override fun getItemCount(): Int = listItemTransaksi.size

    override fun onBindViewHolder(holder: RclvItemTransaksi.ViewHolder, position: Int) {
        val item = listItemTransaksi[position]
        holder.onBind(item, context)
        /*Menghapus produk di keranjang*/
        holder.itemView.btnHapus.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle("Hapus")
                .setMessage("Apakah anda yakin ingin menghapus?")
                .setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                    item.delete()
                    dialogInterface.dismiss()
                    listItemTransaksi.removeAt(position)
                    notifyDataSetChanged()
                })
                .setNegativeButton(
                    "Batal",
                    DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .show()
        }

        /*Menambah jumlah item*/
        holder.itemView.btnPlus.setOnClickListener {
            if (item.jumlah!! < item.stokProduk!!) {
                holder.itemView.tvJumlah.text =
                    (holder.itemView.tvJumlah.text.toString().toInt() + 1).toString()
                item.jumlah = holder.itemView.tvJumlah.text.toString().toInt()
                item.save()
                notifyDataSetChanged()
            } else {
                Toast.makeText(context, "Pembelian telah mencapai batas maksimum !!!", Toast.LENGTH_LONG).show()
            }
        }
        /*Mengurangi jumlah item*/
        holder.itemView.btnMinus.setOnClickListener {
            if (holder.itemView.tvJumlah.text.toString().toInt() != 1) {
                holder.itemView.tvJumlah.text =
                    (holder.itemView.tvJumlah.text.toString().toInt() - 1).toString()
                item.jumlah = holder.itemView.tvJumlah.text.toString().toInt()
                item.save()
                notifyDataSetChanged()
            }
        }

    }

}
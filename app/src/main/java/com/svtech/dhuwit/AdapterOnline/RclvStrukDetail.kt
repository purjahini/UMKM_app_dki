package com.svtech.dhuwit.AdapterOnline


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svtech.dhuwit.Activities.*

import com.svtech.dhuwit.R

import com.svtech.dhuwit.Utils.numberToCurrency
import kotlinx.android.synthetic.main.layout_item_struk.view.*


class RclvStrukDetail(val context: Context, var listItemMenu: MutableList<ResponseStruk.ItemProduk>) :
    RecyclerView.Adapter<RclvStrukDetail.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(dataTodays: ResponseStruk.ItemProduk, context: Context) {
            itemView.tvNamaProduk.text = dataTodays.nama_produk
            itemView.tvDetailProduk.text = "${dataTodays.harga_produk}  X  ${dataTodays.jumlah}     ${numberToCurrency(dataTodays.harga_produk*dataTodays.jumlah)}"

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvStrukDetail.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_struk, parent, false)
        )
    }

    override fun getItemCount(): Int = listItemMenu.size

    override fun onBindViewHolder(holder: RclvStrukDetail.ViewHolder, position: Int) {
        val item = listItemMenu[position]
        holder.onBind(item, context)

    }

}
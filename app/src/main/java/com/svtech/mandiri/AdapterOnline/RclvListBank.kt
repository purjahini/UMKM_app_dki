package com.svtech.mandiri.AdapterOnline

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svtech.mandiri.R
import com.svtech.mandiri.modelOnline.ResponseBank
import kotlinx.android.synthetic.main.layout_item_bank.view.tvAtasNama
import kotlinx.android.synthetic.main.layout_item_bank.view.tvBank
import kotlinx.android.synthetic.main.layout_item_bank.view.tvNorek


/*Adapter recycler view untuk menu dashboard*/
class RclvListBank(val context: Context, var listItemMenu: List<ResponseBank.Data>) :
    RecyclerView.Adapter<RclvListBank.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(data: ResponseBank.Data, context: Context) {
            itemView.tvBank.text = data.nama_bank
            itemView.tvAtasNama.text = data.atas_nama
            itemView.tvNorek.text = data.no_rek
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvListBank.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_bank, parent, false)
        )
    }

    override fun getItemCount(): Int = listItemMenu.size

    override fun onBindViewHolder(holder: RclvListBank.ViewHolder, position: Int) {
        val item = listItemMenu[position]
        holder.onBind(item, context)
    }

}
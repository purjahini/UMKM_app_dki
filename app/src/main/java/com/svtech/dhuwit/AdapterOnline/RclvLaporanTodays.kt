package com.svtech.dhuwit.AdapterOnline

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svtech.dhuwit.Activities.*
import com.svtech.dhuwit.Models.Menu
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.numberToCurrency
import com.svtech.dhuwit.modelOnline.ResponseTransaksiTodays
import kotlinx.android.synthetic.main.layout_item_lap_todays.view.*


/*Adapter recycler view untuk menu dashboard*/
class RclvLaporanTodays(val context: Context, var listItemMenu: List<ResponseTransaksiTodays.Data>) :
    RecyclerView.Adapter<RclvLaporanTodays.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(dataTodays: ResponseTransaksiTodays.Data, context: Context) {
            itemView.tvTotal.text = numberToCurrency(dataTodays.total.toInt())
            itemView.tvTanggal.text = dataTodays.tanggal
            itemView.tvTrx.text = dataTodays.jumlah_trx+" Invoice"
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvLaporanTodays.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_lap_todays, parent, false)
        )
    }

    override fun getItemCount(): Int = listItemMenu.size

    override fun onBindViewHolder(holder: RclvLaporanTodays.ViewHolder, position: Int) {
        val item = listItemMenu[position]
        holder.onBind(item, context)
    }

}
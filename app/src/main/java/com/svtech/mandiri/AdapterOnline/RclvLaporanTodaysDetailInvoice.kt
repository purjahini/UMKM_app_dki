package com.svtech.mandiri.AdapterOnline

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svtech.mandiri.Activities.*

import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.numberToCurrency
import com.svtech.mandiri.modelOnline.ResponseDetailInvoice
import kotlinx.android.synthetic.main.layout_item_lap_detail_invoice.view.*


class RclvLaporanTodaysDetailInvoice(val context: Context, var listItemMenu: MutableList<ResponseDetailInvoice.Data>) :
    RecyclerView.Adapter<RclvLaporanTodaysDetailInvoice.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(dataTodays: ResponseDetailInvoice.Data, context: Context) {
            itemView.tvInvoice.text = dataTodays.invoice
            itemView.tvTotalBayar.text = numberToCurrency(dataTodays.total_pembayaran)

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvLaporanTodaysDetailInvoice.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_lap_detail_invoice, parent, false)
        )
    }

    override fun getItemCount(): Int = listItemMenu.size

    override fun onBindViewHolder(holder: RclvLaporanTodaysDetailInvoice.ViewHolder, position: Int) {
        val item = listItemMenu[position]
        holder.onBind(item, context)
        holder.itemView.setOnClickListener {
            See.log("on clik detail invoice to struck id transaksi ${item.id}")
            val intent = Intent(context, StrukActivity::class.java)
            intent.putExtra(MyConstant.ID_TRANSAKSI, item.id)
            context.startActivity(intent)

        }
    }

}
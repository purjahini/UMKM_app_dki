package com.svtech.mandiri.AdapterOnline

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.numberToCurrency
import com.svtech.mandiri.modelOnline.ResponseMenuNeraca
import kotlinx.android.synthetic.main.layout_item_lap_todays.view.*
import kotlinx.android.synthetic.main.layout_item_neraca.view.tvDebet
import kotlinx.android.synthetic.main.layout_item_neraca.view.tvInvoice
import kotlinx.android.synthetic.main.layout_item_neraca.view.tvKredit


/*Adapter recycler view untuk menu dashboard*/
class RclvNeraca(val context: Context, var listNeraca: List<ResponseMenuNeraca.HistoryNeraca>) :
    RecyclerView.Adapter<RclvNeraca.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(neraca:ResponseMenuNeraca.HistoryNeraca, context: Context) {
            itemView.tvDebet.text = "Debet : "+numberToCurrency(neraca.debet)
            itemView.tvKredit.text = "Kredit : "+numberToCurrency(neraca.kredit)
            itemView.tvTanggal.text = "Tanggal : "+neraca.created_at
            if (neraca.uraian.isNullOrEmpty()) {
                itemView.tvInvoice.setText("No. Invoice : -")
            } else {
                itemView.tvInvoice.setText("No. Invoice : "+neraca.uraian)
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvNeraca.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_neraca, parent, false)
        )
    }

    override fun getItemCount(): Int = listNeraca.size

    override fun onBindViewHolder(holder: RclvNeraca.ViewHolder, position: Int) {
        val item = listNeraca[position]
        holder.onBind(item, context)
    }

}
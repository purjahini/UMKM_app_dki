package com.svtech.dhuwit.AdapterOnline

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svtech.dhuwit.Activities.*

import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.numberToCurrency
import com.svtech.dhuwit.modelOnline.ResponseTransaksiTodays
import kotlinx.android.synthetic.main.layout_item_lap_detail.view.*


/*Adapter recycler view untuk menu dashboard*/
class RclvLaporanTodaysDetail(val context: Context, var listItemMenu: List<ResponseTransaksiTodays.Data>) :
    RecyclerView.Adapter<RclvLaporanTodaysDetail.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(dataTodays: ResponseTransaksiTodays.Data, context: Context) {
            itemView.tvTotal.text = numberToCurrency(dataTodays.total.toInt())
            itemView.tvTanggal.text = dataTodays.tanggal
            itemView.tvTrx.text = dataTodays.jumlah_trx+" Invoice"
            var nilai = 0
            itemView.TvDetailBtn.setOnClickListener {
                if (nilai.equals(0)) {

                    itemView.rclvLapTodaysDetail.visibility = View.VISIBLE
                    itemView.TvDetailBtn.animate().rotation(180F).start()
                    nilai = 1
                } else {
                    itemView.rclvLapTodaysDetail.visibility = View.GONE
                    itemView.TvDetailBtn.animate().rotation(0F).start()
                    nilai = 0
                }

            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvLaporanTodaysDetail.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_lap_detail, parent, false)
        )
    }

    override fun getItemCount(): Int = listItemMenu.size

    override fun onBindViewHolder(holder: RclvLaporanTodaysDetail.ViewHolder, position: Int) {
        val item = listItemMenu[position]
        holder.onBind(item, context)
    }

}
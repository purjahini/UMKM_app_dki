package com.svtech.dhuwit.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svtech.dhuwit.Models.DataBank
import com.svtech.dhuwit.R
import kotlinx.android.synthetic.main.layout_item_menu.view.*

/*Adapter recycler view untuk menu dashboard*/
class RclvItemBank(val context: Context, var listItemMenu: MutableList<DataBank>) :
    RecyclerView.Adapter<RclvItemBank.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(menu: DataBank, context: Context) {
            when (menu.bank) {
                "BNI" -> {
                    itemView.iconMenu.setImageDrawable(context.getDrawable(R.drawable.tapcash))
                }
                "DKI" -> {
                    itemView.iconMenu.setImageDrawable(context.getDrawable(R.drawable.dki))
                }

                "MANDIRI" -> {
                    itemView.iconMenu.setImageDrawable(context.getDrawable(R.drawable.mandiri))

                }
                "BRI" -> {
                    itemView.iconMenu.setImageDrawable(context.getDrawable(R.drawable.brizzi))

                }
                else -> {
                    itemView.iconMenu.setImageDrawable(context.getDrawable(R.drawable.logo))

                }
            }

            itemView.namaMenu.text = menu.bank
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvItemBank.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_menu, parent, false)
        )
    }

    override fun getItemCount(): Int = listItemMenu.size

    override fun onBindViewHolder(holder: RclvItemBank.ViewHolder, position: Int) {
        val item = listItemMenu[position]
        holder.onBind(item, context)
    }

}
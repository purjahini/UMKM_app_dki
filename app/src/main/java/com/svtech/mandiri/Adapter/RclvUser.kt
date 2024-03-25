package com.svtech.mandiri.Adapter

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
import com.svtech.mandiri.Activities.AddKaryawanActivity
import com.svtech.mandiri.Models.User
import com.svtech.mandiri.R
import kotlinx.android.synthetic.main.layout_item_pegawai.view.*

/*Adapter recycler view untuk menapilkan item user*/
class RclvUser(val context: Context, var listUser : MutableList<User>): RecyclerView.Adapter<RclvUser.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User){
            Glide.with(itemView).load(Base64.decode(user.foto,Base64.DEFAULT)).fitCenter().into(itemView.imgFotoPegawai)
            itemView.tvNama.text = user.nama
            itemView.tvKontak.text = user.kontak
            itemView.tvUsername.text = user.username
            itemView.tvPassword.text = user.password
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_item_pegawai, parent, false))
    }

    override fun getItemCount(): Int = listUser.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = listUser[position]
        holder.bind(user)
        holder.itemView.setOnClickListener {
            val popupMenu = PopupMenu(context, it)
            popupMenu.menuInflater.inflate(R.menu.menu_edit_item, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {item ->
                when(item.itemId){
                    R.id.menuEdit -> editItem(user)
                    R.id.menuHapus -> hapusItem(user)
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
    }

    private fun hapusItem(user: User) {
        MaterialAlertDialogBuilder(context).setTitle("Hapus").setMessage("Apakah anda yakin ingin menghapus?")
            .setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                listUser.removeAt(listUser.indexOf(user))
                user.delete()
                notifyDataSetChanged()
            })
            .setNegativeButton("Batal", DialogInterface.OnClickListener { dialogInterface, i ->  dialogInterface.dismiss()})
            .show()
    }

    private fun editItem(user: User) {
        val intent = Intent(context, AddKaryawanActivity::class.java)
        intent.putExtra("user", user.id)
        intent.putExtra("update", true)
        context.startActivity(intent)
    }
}
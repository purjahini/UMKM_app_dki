package com.svtech.dhuwit.AdapterOnline

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.svtech.dhuwit.Activities.AddKaryawanActivity
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.modelOnline.ResponsePegawai
import kotlinx.android.synthetic.main.layout_item_pegawai.view.*
import org.json.JSONObject

/*Adapter recycler view untuk menapilkan item user*/
class RclvUserOnline(val context: Context, var listUser : MutableList<ResponsePegawai.Data>): RecyclerView.Adapter<RclvUserOnline.ViewHolder>() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(user: ResponsePegawai.Data){

            Glide.with(itemView)
                .load(user.foto)
                .fitCenter()
                .placeholder(R.drawable.logo)
                .into(itemView.imgFotoPegawai)
            itemView.tvNama.text = "NAMA :\n"+user.nama
            itemView.tvKontak.text = "USERNAME :\n"+user.kontak
//            itemView.tvUsername.text = ""+user.username
            itemView.tvPassword.text = "PASSWORD Encrypt :\n"+user.password
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

     fun hapusItem(user: ResponsePegawai.Data) {

         progressDialog = ProgressDialog(context)
         progressDialog!!.setTitle("Proses")
         progressDialog!!.setMessage("Mohon Menunggu...")
         progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
         progressDialog!!.setCancelable(false)
         progressDialog!!.isIndeterminate = true
         token =
             com.svtech.dhuwit.Utils.getPreferences(context).getString(MyConstant.TOKEN, "").toString()
         username =
             com.svtech.dhuwit.Utils.getPreferences(context).getString(MyConstant.CURRENT_USER, "")
                 .toString()
         See.log("token addProduk : $token id hapus kt : ${user?.id}")
        MaterialAlertDialogBuilder(context).setTitle("Hapus").setMessage("Apakah anda yakin ingin menghapus?")
            .setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                progressDialog?.show()
                listUser.remove(user)
                AndroidNetworking.post(MyConstant.Urlpegawaihapus)
                    .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
                    .addBodyParameter(MyConstant.USERNAME, username)
                    .addBodyParameter(MyConstant.ID, user.id.toString())
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener{
                        override fun onResponse(response: JSONObject?) {
                            progressDialog?.dismiss()
                            See.toast(context, "Hapus pegawai ${user.nama} sukses")

                        }

                        override fun onError(anError: ANError?) {
                            progressDialog?.dismiss()
                            See.toast(context, "Hapus pegawai ${user.nama} gagal")

                        }

                    })
                notifyDataSetChanged()
//                listUser.removeAt(listUser.indexOf(user))
//                user.delete()

            })
            .setNegativeButton("Batal", DialogInterface.OnClickListener { dialogInterface, i ->  dialogInterface.dismiss()})
            .show()
    }

    private fun editItem(user: ResponsePegawai.Data) {
        val intent = Intent(context, AddKaryawanActivity::class.java)
        intent.putExtra(MyConstant.ID, user.id)
        intent.putExtra(MyConstant.NAMA, user.nama)
        intent.putExtra(MyConstant.KONTAK, user.kontak)
        intent.putExtra(MyConstant.FOTO, user.foto)
        intent.putExtra("update", true)
        context.startActivity(intent)
    }
}
package com.svtech.dhuwit.AdapterOnline

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.svtech.dhuwit.Activities.AddKategoriActivity
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.modelOnline.KategoriOnline
import kotlinx.android.synthetic.main.activity_add_kategori.*
import kotlinx.android.synthetic.main.layout_item_kategori.view.*
import org.json.JSONObject

/*Adapter recycler view untuk menapilkan item kategori*/
class RclvKategoriOnline : RecyclerView.Adapter<RclvKategoriOnline.ViewHolder> {
    val context: Context
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    val order: Boolean
    lateinit var saveListKategori: MutableList<KategoriOnline.Data?>
    lateinit var listKategori: MutableList<KategoriOnline.Data?>

    constructor(
        context: Context,
        listKategori: MutableList<KategoriOnline.Data?>?,
        sort: Boolean,
        order: Boolean

        ): super() {
        this.context = context
        if (listKategori != null) {
            this.listKategori =
                if (sort) listKategori.sortBy { it?.kategori_nama } as MutableList<KategoriOnline.Data?> else listKategori
        }
        this.order = order
        if (listKategori != null) {
            this.saveListKategori = listKategori
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(kategori: KategoriOnline.Data?, context: Context){
            if (kategori != null) {
                Glide.with(context).load(kategori?.kategori_gambar)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.logo).fitCenter().into(itemView.imgFoto)
                itemView.tvKategori.text = kategori?.kategori_nama
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_item_kategori, parent, false))
    }

    override fun getItemCount(): Int = this.listKategori!!.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kategori = this.listKategori?.get(position)
        holder.bind(kategori, context)
        holder.itemView.setOnClickListener {
            val popupMenu = PopupMenu(context, it)
            popupMenu.menuInflater.inflate(R.menu.menu_edit_item, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {item ->
                when(item.itemId){
                    R.id.menuEdit -> editItem(kategori)
                    R.id.menuHapus -> hapusItem(kategori)
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
    }

    private fun hapusItem(kategori: KategoriOnline.Data?) {
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
        See.log("token addProduk : $token id hapus kt : ${kategori?.id}")
        MaterialAlertDialogBuilder(context).setTitle("Hapus").setMessage("Apakah anda yakin ingin menghapus?")
            .setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                this.listKategori?.remove(kategori)
                AndroidNetworking.post(MyConstant.Urlkategorihapus)
                    .addHeaders("Authorization", "Bearer$token")
                    .addBodyParameter("id", kategori?.id.toString().trim())
                    .addBodyParameter("username", username.trim())
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            val respon = response?.toString()
                            See.log("respon insertKategori : $respon")
                            val json = JSONObject(respon)
                            val apiStatus = json.getInt(MyConstant.API_STATUS)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiStatus.equals(1)) {
                                progressDialog!!.dismiss()

                                See.toast(context, "Hapus Item Kategori to Server $apiMessage")

                            } else {
                                progressDialog!!.dismiss()
                                See.toast(context, "Hapus Item  Kategori to Server $apiMessage")

                            }

                        }

                        override fun onError(anError: ANError?) {
                            progressDialog?.dismiss()
                            See.log("onError errorCode insertKategori : ${anError?.errorCode}")
                            See.log("onError errorBody insertKategori: ${anError?.errorBody}")
                            See.log("onError errorDetail insertKategori: ${anError?.errorDetail}")
                        }

                    })

//                kategori.delete()
                notifyDataSetChanged()
            })
            .setNegativeButton("Batal", DialogInterface.OnClickListener { dialogInterface, i ->  dialogInterface.dismiss()})
            .show()
    }

    fun sortItem(sort: String) {
        when (sort) {
            "Asc" -> {
                listKategori.sortBy { it?.kategori_nama }
                notifyDataSetChanged()
            }
            "Dsc" -> {
                listKategori.sortBy { it?.kategori_nama }
                listKategori.reverse()
                notifyDataSetChanged()
            }
        }
    }

    fun searchItem(search: String) {
        if (search.isNotEmpty()) {
            val search = saveListKategori.filter { produk ->
                produk?.kategori_nama!!.trim().toLowerCase()
                    .toLowerCase().contains(search.trim().toLowerCase())
            }
            this.listKategori = search as MutableList<KategoriOnline.Data?>
            notifyDataSetChanged()
        } else {
            this.listKategori = saveListKategori
            notifyDataSetChanged()
        }
    }

    private fun editItem(kategori: KategoriOnline.Data?) {
        val intent = Intent(context, AddKategoriActivity::class.java)
        intent.putExtra("kategori_id", kategori?.id)
        intent.putExtra("kategori_nama", kategori?.kategori_nama)
        intent.putExtra("kategori_gambar", kategori?.kategori_gambar)
        intent.putExtra("update", true)
        context.startActivity(intent)
    }
}
package com.svtech.mandiri.Adapter

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.svtech.mandiri.Activities.LaporanActivity
import com.svtech.mandiri.Activities.MenuKeranjangActivity
import com.svtech.mandiri.Activities.MenuPembelianActivity
import com.svtech.mandiri.Activities.MenuTambahKategoriActivity
import com.svtech.mandiri.Activities.MenuTambahPegawaiActivity
import com.svtech.mandiri.Activities.MenuTambahProdukActivity
import com.svtech.mandiri.Activities.KeuanganActivity
import com.svtech.mandiri.Activities.KeunganNewActivity
import com.svtech.mandiri.Activities.WalletActivity
import com.svtech.mandiri.AdapterOnline.RclvKategoriOnline
import com.svtech.mandiri.Models.Menu
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.getTokenContext
import com.svtech.mandiri.modelOnline.KategoriOnline
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.rclvPenjualan
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.tvEmpty
import kotlinx.android.synthetic.main.layout_item_menu.view.iconMenu
import kotlinx.android.synthetic.main.layout_item_menu.view.namaMenu
import org.json.JSONObject

/*Adapter recycler view untuk menu dashboard*/
class RclvItemMenu(val context: Context, var listItemMenu: MutableList<Menu>) :
    RecyclerView.Adapter<RclvItemMenu.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(menu: Menu, context: Context) {
            Glide.with(context)
                .load(menu.icon!!)
                .into(itemView.iconMenu)
//            itemView.iconMenu.setImageDrawable(context.getDrawable(menu.icon!!))
            itemView.namaMenu.text = menu.nama
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvItemMenu.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_menu, parent, false)
        )
    }

    override fun getItemCount(): Int = listItemMenu.size

    override fun onBindViewHolder(holder: RclvItemMenu.ViewHolder, position: Int) {
        val item = listItemMenu[position]
        holder.onBind(item, context)
        holder.itemView.setOnClickListener {
            when (item.nama) {
                "Penjualan" -> {
                    context.startActivity(Intent(context, MenuPembelianActivity::class.java))
                }

                "Keranjang" -> {
                    context.startActivity(Intent(context, MenuKeranjangActivity::class.java))
                }

                "Kategori" -> {
                    context.startActivity(Intent(context, MenuTambahKategoriActivity::class.java))
                }

                "Produk" -> {
                    var progressDialog: ProgressDialog? = null

                   var token =
                        com.svtech.mandiri.Utils.getPreferences(context).getString(MyConstant.TOKEN, "").toString()

                   var username =
                        com.svtech.mandiri.Utils.getPreferences(context).getString(MyConstant.CURRENT_USER, "")
                            .toString()
                    progressDialog = ProgressDialog(context)
                    progressDialog!!.setTitle("Proses")
                    progressDialog!!.setMessage("Mohon Menunggu...")
                    progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    progressDialog!!.setCancelable(false)
                    progressDialog!!.isIndeterminate = true
                    //cek apakah ada kategori produk atau tidak jika ada lanjutkan

                    progressDialog.show()
                    AndroidNetworking.post(MyConstant.UrlKategoriGetData)
                        .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
                        .addBodyParameter("username", username)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener{
                            override fun onResponse(response: JSONObject?) {
                                progressDialog?.dismiss()
                                val respon = response?.toString()
                                See.log("respon getKategori: $respon")
                                val json = JSONObject(respon)
                                val apiStatus = json.getInt(MyConstant.API_STATUS)
                                val apiMessage = json.getString(MyConstant.API_MESSAGE)
                                if (apiStatus.equals(1)) {
                                    val data = Gson().fromJson(respon, KategoriOnline::class.java)
                                    if (data.data.isNullOrEmpty()){
                                        See.toast(context, "Silahkan Tambahkan Kategori Dahulu")
                                        context.startActivity(Intent(context, MenuTambahKategoriActivity::class.java))

                                    }else {
                                        context.startActivity(Intent(context, MenuTambahProdukActivity::class.java))
                                    }

                                } else {
                                    See.toast(context, "response api $apiMessage")
                                }


                            }

                            override fun onError(anError: ANError?) {

                                progressDialog?.dismiss()
                                val json = JSONObject(anError?.errorBody)
                                val apiMessage = json.getString(MyConstant.API_MESSAGE)
                                if (apiMessage != null) {
                                    if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                        getTokenContext(context)

                                    }
                                }

                                See.log("onError getProduk errorCode : ${anError?.errorCode}")
                                See.log("onError getProduk errorBody : ${anError?.errorBody}")
                                See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                            }

                        })

                }

                "Pegawai" -> {
                    context.startActivity(Intent(context, MenuTambahPegawaiActivity::class.java))
                }

                "Laporan" -> {
                    context.startActivity(Intent(context, LaporanActivity::class.java))
                }

                "Keuangan" -> {
                    context.startActivity(Intent(context, KeunganNewActivity::class.java))
                }

                "Wallet" -> {
                    context.startActivity(Intent(context, WalletActivity::class.java))
                }

            }
        }
    }

}
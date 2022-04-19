package com.svtech.dhuwit.AdapterOnline

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.orm.SugarRecord
import com.svtech.dhuwit.Activities.AddProdukActivity
import com.svtech.dhuwit.Activities.MenuPembelianActivity
import com.svtech.dhuwit.Models.ItemTransaksi
import com.svtech.dhuwit.Models.Transaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOnline.ItemTransaksiOnline
import com.svtech.dhuwit.modelOnline.ProdukOnline
import com.svtech.dhuwit.modelOnline.ResponseId
import kotlinx.android.synthetic.main.activity_menu_pembelian.*
import kotlinx.android.synthetic.main.layout_item_produk.view.*
import kotlinx.android.synthetic.main.layout_total_order.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/*Adapter recycler view untuk menapilkan item produk*/
class RclvProdukOnline :
    RecyclerView.Adapter<RclvProdukOnline.ViewHolder> {
    val context: Context
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    var TransaksiId = ""
    var ProdukId = ""
    var transaksiIds = 0
    lateinit var listProduk: MutableList<ProdukOnline.Data?>

    lateinit var itemTransaksi: MutableList<ItemTransaksiOnline.Data?>
    val order: Boolean
    lateinit var saveListProduk: MutableList<ProdukOnline.Data?>


    constructor(
        context: Context,
        listProduk: MutableList<ProdukOnline.Data?>?,
        sort: Boolean,
        order: Boolean
    ) : super() {
        this.context = context
        if (listProduk != null) {
            this.listProduk =
                if (sort) listProduk.sortedBy { it?.nama } as MutableList<ProdukOnline.Data?> else listProduk
        }
        this.order = order
        if (listProduk != null) {
            this.saveListProduk = listProduk
        }

        token = getPreferences(context).getString(MyConstant.TOKEN, "").toString()
        username = getPreferences(context).getString(MyConstant.CURRENT_USER, "").toString()
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(produk: ProdukOnline.Data?, context: Context) {
            if (produk != null) {
                Glide.with(context).load(produk.foto).fitCenter()
                    .placeholder(R.drawable.logo)
                    .into(itemView.imgFoto)
                itemView.tvNamaProduk.text = produk.nama
                itemView.tvKategori.text = produk.kategori_nama
                itemView.tvHargaProduk.text = numberToCurrency(produk.harga!!)
                itemView.tvStok.text = "Stok : " + produk.stok.toString()
                /*Tampilkan label stok habis*/
                if (produk.stok == 0) {
                    itemView.imgStokHabis.visibility = View.VISIBLE
                } else {
                    itemView.imgStokHabis.visibility = View.INVISIBLE
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_produk, parent, false)
        )
    }

    override fun getItemCount(): Int = listProduk.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produk = listProduk[position]
        if (produk != null) {

            holder.bind(produk, context)
            holder.itemView.setOnClickListener {
                if (order) {

                    /*return jika stok habis*/
                    if (produk?.stok == 0) {

                        Toast.makeText(context, "Maaf.. Stok Produk Habis ${produk?.stok}", Toast.LENGTH_SHORT).show()

                        return@setOnClickListener
                    }

                    AndroidNetworking.post(MyConstant.Urltransaksigetstatus)
                        .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
                        .addBodyParameter(MyConstant.STATUSPARAM, "1")
                        .addBodyParameter(MyConstant.USERNAME, username)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject?) {
                                progressDialog?.dismiss()
                                if (response != null) {
                                    val respon = response?.toString()
                                    See.log("respon getDataTransaksiStatus: \n $respon")
                                    val json = JSONObject(respon)
                                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                                    if (apiStatus.equals(1)) {
                                        val data = Gson().fromJson(respon, ProdukOnline::class.java)
                                        val listProdukRespon = data.data
                                        if (listProdukRespon != null) {
                                            listProduk = listProdukRespon
                                            listProdukRespon?.map {
                                                TransaksiId = it?.id.toString()
                                            }
                                        }

                                    }

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
                            }

                        })

                    AndroidNetworking.post(MyConstant.Urlitemtransaksidataidproduk)
                        .addHeaders(MyConstant.AUTHORIZATION, "Bearer${token}")
                        .addBodyParameter(MyConstant.ID_TRANSAKSI, TransaksiId)
                        .addBodyParameter(MyConstant.ID, produk?.id.toString())
                        .addBodyParameter(MyConstant.USERNAME, username)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject?) {
                                progressDialog?.dismiss()
                                if (response != null) {
                                    val respon = response?.toString()
                                    See.log("respon getDataTransaksiStatus: \n $respon")
                                    val json = JSONObject(respon)
                                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                                    if (apiStatus.equals(1)) {
                                        val data =
                                            Gson().fromJson(respon, ItemTransaksiOnline::class.java)
                                        val listItemTransaksiOnlineRespon = data.data
                                        if (listItemTransaksiOnlineRespon != null) {
                                            listItemTransaksiOnlineRespon.forEach {
                                                itemTransaksi = it as MutableList<ItemTransaksiOnline.Data?>

                                            }



                                        }

                                    } else {
                                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {


                                        }
                                        See.toast(context, apiMessage)
                                    }

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
                            }

                        })


                    val activity = context as MenuPembelianActivity
                    val view =
                        LayoutInflater.from(context)
                            .inflate(R.layout.layout_total_order, null, false)
                    MaterialAlertDialogBuilder(
                        context
                    )
                        .setView(view)
                        .setNegativeButton("Batal") { dialogInterface, _ -> dialogInterface.dismiss() }
                        .setPositiveButton("Tambah") { _, _ ->

                            /*Cek apakah sudah mencapai stok maksimum*/
                            if (itemTransaksi != null) {
                                itemTransaksi.map {
                                if (  it?.jumlah!!.plus(view.tvJumlah.text.toString().toInt()) > produk.stok!! ) {
                                    Toast.makeText(
                                        context,
                                        "Pembelian telah mencapai batas maksimum !!!",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    return@setPositiveButton
                                }
                                }

                            }


                            if (listProduk == null) {
//                                val transaksi = Transaksi(
//                                    status = true,
//                                    tanggalTrasaksi = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
//                                        Date().time
//                                    )
//                                )
//                                transaksi.save()
                                progressDialog?.show()
                                AndroidNetworking.post(MyConstant.Urltransaksicreatestatus)
                                    .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
                                    .addBodyParameter(MyConstant.STATUS, "1")
                                    .addBodyParameter(MyConstant.USERNAME,username )
                                    .setPriority(Priority.MEDIUM)
                                    .build()
                                    .getAsJSONObject(object : JSONObjectRequestListener{
                                        override fun onResponse(response: JSONObject?) {
                                           progressDialog?.dismiss()
                                            val data = Gson().fromJson(response.toString(), ResponseId::class.java)
                                          transaksiIds = data.data.id

                                        }

                                        override fun onError(anError: ANError?) {
                                             progressDialog?.dismiss()
                                            val json = JSONObject(anError?.errorBody)
                                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                                            if (apiMessage != null) {
                                                if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                                    getToken(activity)
                                                }
                                            }

                                            See.log("onError getProduk errorCode : ${anError?.errorCode}")
                                        }

                                    })

                                AndroidNetworking.post(MyConstant.Urlitem_transaksi_insert_produk_id)
                                    .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
                                    .addBodyParameter(MyConstant.diskon_produk, produk.diskon.toString())
                                    .addBodyParameter(MyConstant.harga_produk, produk.harga.toString())
                                    .addBodyParameter(MyConstant.ID_TRANSAKSI, transaksiIds.toString())
                                    .addBodyParameter(MyConstant.JUMLAH, view.tvJumlah.text.toString().toInt().toString())
                                    .addBodyParameter(MyConstant.KATEGORI, produk.kategori.toString())
                                    .addBodyParameter(MyConstant.MINIMAL_PEMBELIAN, produk.minimal_pembelian.toString())
                                    .addBodyParameter(MyConstant.nama_produk, produk.nama)
                                    .addBodyParameter(MyConstant.SATUAN, produk.satuan)
                                    .addBodyParameter(MyConstant.stok_produk, produk.stok.toString())
                                    .addBodyParameter(MyConstant.produk_id, produk.id.toString())
                                    .addBodyParameter(MyConstant.USERNAME, username)
                                    .setPriority(Priority.MEDIUM)
                                    .build()
                                    .getAsJSONObject(object :JSONObjectRequestListener{
                                        override fun onResponse(response: JSONObject?) {
                                            val respon = response.toString()
                                            See.log("Respon insert item $respon")
                                            val json= JSONObject(respon)
                                            val apiStatusItem = json.getInt(MyConstant.API_STATUS)
                                            val apiMessageItem = json.getString(MyConstant.API_MESSAGE)
                                            if (apiStatusItem.equals(1)) {
                                                val data = Gson().fromJson(respon, ResponseId::class.java)
                                                val list = data.data.id
                                            }

                                        }

                                        override fun onError(anError: ANError?) {

                                            progressDialog?.dismiss()
                                            val json = JSONObject(anError?.errorBody)
                                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                                            if (apiMessage != null) {
                                                if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                                    getToken(activity)
                                                }
                                            }

                                            See.log("onError getProduk errorCode : ${anError?.errorCode}")
                                        }

                                    })


                            }
                            activity.setBadgeKeranjang()
                        }.show()

                    view.btnPlus.setOnClickListener {
                        itemTransaksi.forEach {
                            val jumlahDiKeranjang =
                                if (itemTransaksi == null) 0 else it?.jumlah
                            val jumlahTambahan = view.tvJumlah.text.toString().toInt()
                            val jumlahTotal = jumlahDiKeranjang?.plus(jumlahDiKeranjang)

                            Log.d("jumlah", jumlahTotal.toString())

                            /*Cek apakah sudah mencapai stok maksimum*/
                            if (jumlahTotal!! < produk.stok!!) {
                                view.tvJumlah.text =
                                    (view.tvJumlah.text.toString().toInt() + 1).toString()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Pembelian telah mencapai batas maksimum !!!",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        }


                    }
                    view.btnMinus.setOnClickListener {
                        if (view.tvJumlah.text.toString().toInt() != 1) {
                            view.tvJumlah.text =
                                (view.tvJumlah.text.toString().toInt() - 1).toString()
                        }
                    }
                } else {
                    val popupMenu = PopupMenu(context, it)
                    popupMenu.menuInflater.inflate(R.menu.menu_edit_item, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menuEdit -> editItem(produk)
                            R.id.menuHapus -> hapusItem(produk)
                        }
                        return@setOnMenuItemClickListener true
                    }
                    popupMenu.show()
                }
            }
        }
    }

    private fun hapusItem(produk: ProdukOnline.Data?) {
        progressDialog = ProgressDialog(context)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        See.log("token addProduk : $token")
        MaterialAlertDialogBuilder(context).setTitle("Hapus")
            .setMessage("Apakah anda yakin ingin menghapus?")
            .setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                listProduk.removeAt(listProduk.indexOf(produk))
                AndroidNetworking.post(MyConstant.Urlprodukhapusid)
                    .addHeaders("Authorization", "Bearer$token")
                    .addBodyParameter("id", produk?.id.toString().trim())
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

                                See.toast(context, "Hapus Item Produk to Server $apiMessage")

                            } else {
                                progressDialog!!.dismiss()
                                See.toast(context, "Hapus Item  Produk to Server $apiMessage")

                            }

                        }

                        override fun onError(anError: ANError?) {
                            progressDialog?.dismiss()
                            See.log("onError errorCode insertKategori : ${anError?.errorCode}")
                            See.log("onError errorBody insertKategori: ${anError?.errorBody}")
                            See.log("onError errorDetail insertKategori: ${anError?.errorDetail}")
                        }

                    })
                // produk.delete()
                notifyDataSetChanged()
            })
            .setNegativeButton(
                "Batal",
                DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
            .show()

    }

    private fun editItem(produk: ProdukOnline.Data?) {
        val intent = Intent(context, AddProdukActivity::class.java)
        if (produk != null) {
            intent.putExtra("id", produk.id)
            intent.putExtra("diskon", produk.diskon)
            intent.putExtra("foto", produk.foto)
            intent.putExtra("harga", produk.harga)
            intent.putExtra("kategori", produk.kategori)
            intent.putExtra("minimal_pembelian", produk.minimal_pembelian)
            intent.putExtra("nama", produk.nama)
            intent.putExtra("satuan", produk.satuan)
            intent.putExtra("stok", produk.stok)
        }
        intent.putExtra("update", true)
        context.startActivity(intent)
    }

    fun sortItem(sort: String) {
        when (sort) {
            "Asc" -> {
                listProduk.sortBy { it?.nama }
                notifyDataSetChanged()
            }
            "Dsc" -> {
                listProduk.sortBy { it?.nama }
                listProduk.reverse()
                notifyDataSetChanged()
            }
        }
    }

    fun searchItem(search: String) {
        if (search.isNotEmpty()) {
            val search = saveListProduk.filter { produk ->
                produk?.nama!!.trim().toLowerCase()
                    .toLowerCase().contains(search.trim().toLowerCase())
            }
            listProduk = search as MutableList<ProdukOnline.Data?>
            notifyDataSetChanged()
        } else {
            listProduk = saveListProduk
            notifyDataSetChanged()
        }
    }
}



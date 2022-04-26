package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.svtech.dhuwit.Adapter.RclvLaporanStok
import com.svtech.dhuwit.AdapterOnline.SpinnerAdapterCustom
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.Utils.getToken
import com.svtech.dhuwit.Utils.setToolbar
import com.svtech.dhuwit.modelOnline.ItemOption
import com.svtech.dhuwit.modelOnline.ResponseLapStok
import kotlinx.android.synthetic.main.activity_laporan_stok.*
import org.json.JSONObject

class LaporanStokActivity : AppCompatActivity() {
    var token = ""
    var username = ""
    var progressDialog: ProgressDialog? = null

    var kategoriId = ""


    var arrayList: ArrayList<ItemOption> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_stok)
        setToolbar(this, "Laporan Stok")

        token =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        See.log("token Kategori : $token")
        See.log("token addProduk : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        progressDialog?.show()
        arrayList.add(ItemOption("0","All Kategori"))

        AndroidNetworking.post(MyConstant.UrlKategoriGetData)
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER + token)
            .addBodyParameter("username", username)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    See.log("respon getKategori: $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        val jsonArray = response!!.getJSONArray("data")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val kategoriIdName = jsonObject.optString("id")
                            val kategoriName = jsonObject.optString("kategori_nama")

                            val aItmOpt = ItemOption(kategoriIdName, kategoriName)
                            arrayList.add(aItmOpt)




//                        KategoriList.add(kategoriName)
//                        KategoriListId.add(kategoriId)
//                        KategoriAdapter = ArrayAdapter(
//                            this@AddProdukActivity,
//                            android.R.layout.simple_spinner_item,
//                            KategoriList)


                        }

                    } else {
                        See.toast(
                            this@LaporanStokActivity,
                            "Check Koneksi Internet anda Code " + apiMessage
                        )
                    }

                    var adapter = SpinnerAdapterCustom(
                        this@LaporanStokActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        arrayList
                    )


                    spnKategoriProduk.adapter = adapter
                    spnKategoriProduk.setSelection(See.getIndex(arrayList,spnKategoriProduk,
                        kategoriId.toString()
                    ))




                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@LaporanStokActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

        spnKategoriProduk.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {


                val ItemOptionModel: ItemOption = parent.selectedItem as ItemOption
                See.log("itemOption id   ${ItemOptionModel.optId} ")
                See.log("itemOption label : ", ItemOptionModel.optLabel)
                kategoriId = ItemOptionModel.optId


                AndroidNetworking.post(MyConstant.Urlstokgetdata)
                    .addHeaders(MyConstant.AUTHORIZATION,MyConstant.BEARER+token)
                    .addBodyParameter(MyConstant.USERNAME, username)
                    .addBodyParameter(MyConstant.KATEGORI, if(kategoriId.equals("0")) "" else kategoriId)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener{
                        override fun onResponse(response: JSONObject?) {
                            progressDialog?.dismiss()
                            val respon = response.toString()
                            See.log("response data stok  $respon")
                            val json = JSONObject(respon)
                            val apiStatus = json.getInt(MyConstant.API_STATUS)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if(apiStatus.equals(1)) {
                                tvEmpty.visibility = View.GONE
                                val data = Gson().fromJson(respon, ResponseLapStok::class.java)
                                val dataToko = data.data_toko
                                dataToko.forEach {
                                    tvNamaToko.text = "Nama Toko : ${it.nama_toko}"
                                    tvAlamatToko.text = "Alamat Toko : ${it.alamat_toko}"

                                }
                                if (data.data.isEmpty()){
                                    tvEmpty.visibility = View.VISIBLE
                                    rclvPenjualan.visibility = View.GONE
                                } else {
                                    rclvPenjualan.visibility = View.VISIBLE
                                    val listData = data.data.reversed()
                                    rclvPenjualan.apply {
                                        adapter = RclvLaporanStok(this@LaporanStokActivity, listData as MutableList<ResponseLapStok.Data>)
                                        layoutManager = LinearLayoutManager(this@LaporanStokActivity)
                                        setHasFixedSize(true)
                                    }
                                }


                            }
                            else {
                                tvEmpty.visibility = View.VISIBLE
                            }
                        }

                        override fun onError(anError: ANError?) {
                            progressDialog?.dismiss()
                            See.log("response api server ${anError?.errorCode}")
                        }

                    })


            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        })







    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
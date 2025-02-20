package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.svtech.mandiri.AdapterOnline.RclvKategoriOnline
import com.svtech.mandiri.AdapterOnline.RclvLaporanTodays
import com.svtech.mandiri.AdapterOnline.RclvListBank
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.setToolbar
import com.svtech.mandiri.modelOnline.ResponseBank
import com.svtech.mandiri.modelOnline.ResponseTransaksiTodays
import kotlinx.android.synthetic.main.activity_laporan_harian.btnExport
import kotlinx.android.synthetic.main.activity_laporan_harian.rclvPenjualan
import kotlinx.android.synthetic.main.activity_laporan_harian.tvEmptyMesage
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.rclvPenjualan
import kotlinx.android.synthetic.main.activity_wallet.RvBank
import kotlinx.android.synthetic.main.activity_wallet.fab_add
import org.json.JSONObject

class WalletActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        setToolbar(this, "Wallet")
        token =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()


        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true


        fab_add.setOnClickListener {
            val intent = Intent(this, AddbankActivity::class.java)
            startActivity(intent)
        }
        Loaddatabank()



    }

    private fun Loaddatabank() {
        progressDialog?.show()

        AndroidNetworking.post(MyConstant.Urllistbank)
            .addHeaders(MyConstant.AUTHORIZATION, "Bearer$token")
            .addBodyParameter(MyConstant.STATUS, "1")
            .addBodyParameter(MyConstant.USERNAME, username.trim())
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog!!.dismiss()
                    val respon = response?.toString()
                    See.log("respon get bank Status : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        val data = Gson().fromJson(respon, ResponseBank::class.java)

                        if (data != null) {
                            val rclvadapter = RclvListBank(this@WalletActivity, data.data)
                            RvBank.apply {
                                adapter = rclvadapter
                                layoutManager = LinearLayoutManager(context)
                                setHasFixedSize(true)
                            }
                        } else {
                            See.toast(this@WalletActivity, "Maaf . .Data bank Belum Ada.")
                        }




                    }

                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@WalletActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

    }

    override fun onResume() {
        super.onResume()
        Loaddatabank()
    }
}
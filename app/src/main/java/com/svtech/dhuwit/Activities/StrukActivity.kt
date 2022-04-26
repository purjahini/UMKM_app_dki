package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.svtech.dhuwit.AdapterOnline.RclvStrukDetail
import com.svtech.dhuwit.AdapterOnline.ResponseStruk
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import kotlinx.android.synthetic.main.activity_struk.*
import org.json.JSONObject

class StrukActivity : AppCompatActivity() {
    var token = ""
    var username = ""
    var id_transaksi = 0
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_struk)
        /*Setting toolbar*/
        setToolbar(this, "Struk")

        id_transaksi = intent.getIntExtra(MyConstant.ID_TRANSAKSI, 0)
        token =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        val nama = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.NAMA, "").toString()
        tvNamaKasir.text = "Kasir : $nama"
        See.log("token struk : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        btnSelesai.setOnClickListener {
            onBackPressed()
        }

        setToRecyclerView()


    }

    private fun setToRecyclerView() {
        AndroidNetworking.post(MyConstant.Urlitem_transaksi_produk_transaksi)
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER + token)
            .addBodyParameter(MyConstant.ID_TRANSAKSI, id_transaksi.toString())
            .addBodyParameter(MyConstant.USERNAME, username)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    See.log("response struk $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        val data = Gson().fromJson(respon, ResponseStruk::class.java)


                        var listStrukToko = data.data
                            listStrukToko.forEach {
                                val TotalBayar = if (it.total_pembayaran.equals(null)) 0 else it.total_pembayaran
                                tvTotalBayar.text = numberToCurrency(TotalBayar)
                            tvNamaCafe.text = it.nama_toko
                                tvInvoice.text = it.invoice
                            tvAlamatCafe.text = it.alamat_toko
                            tvTanggal.text = it.created_at
                            tvBank.text = it.bank
                            tvNoBank.text = it.nokartu
                                val saldoAwal = if (it.saldoawal.isNullOrEmpty()) 0 else it.saldoawal
                            tvSaldoAwal.text = numberToCurrency(saldoAwal.toString().toInt())
                                val saldoAkhir = if (it.saldoakhir.isNullOrEmpty()) 0 else it.saldoakhir
                            tvSaldoAkhir.text = numberToCurrency(saldoAkhir.toString().toInt())
                            tvTid.text = it.tid

                        }

                        val itemTransaksiProduk = data.item_produk

                        val rclvadapter = RclvStrukDetail(
                            this@StrukActivity,
                            itemTransaksiProduk as MutableList<ResponseStruk.ItemProduk>
                        )
                        RvItemTrxProduk.apply {
                            adapter = rclvadapter
                            layoutManager = LinearLayoutManager(this@StrukActivity)
                            setHasFixedSize(true)
                        }


                    } else {

                    }
                }

                override fun onError(anError: ANError?) {


                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@StrukActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
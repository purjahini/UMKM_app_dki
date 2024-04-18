package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.orm.SugarRecord
import com.svtech.mandiri.Models.ItemTransaksi
import com.svtech.mandiri.Models.Transaksi
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.Cons
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.numberToCurrency
import com.svtech.mandiri.modelOnline.ItemTransaksiJsonOnline
import com.svtech.mandiri.modelOnline.ResponseId
import com.svtech.mandiri.modelOnline.ResponseQris
import com.svtech.mandiri.modelOnline.ResponseQrisKaltim
import kotlinx.android.synthetic.main.activity_qris_mock_up.TvNominalQr
import kotlinx.android.synthetic.main.activity_qris_mock_up.TvQrTanggalBerlaku
import kotlinx.android.synthetic.main.activity_qris_mock_up.idIVQrcode
import kotlinx.android.synthetic.main.toolbar_with_back.btnBack
import kotlinx.android.synthetic.main.toolbar_with_back.tvTitle
import org.json.JSONObject
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.UUID

class QrisMockUp : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    var amount = 0.0
    var total = 0
    var message = ""
    var bank = ""
    var nokartu = ""
    var saldoawal = 0
    var saldoakhir = 0
    var tid = ""
    var invoice = ""
    var nama = ""

    var id_transaksi = 0
    var now = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_qris_mock_up)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")       
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        token = com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username = com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "").toString()
        nama = com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.NAMA,"").toString()
        val currentYear: Int = now.get(Calendar.YEAR)
        val currentMonth: Int = now.get(Calendar.MONTH) + 1
        val currentDay: Int = now.get(Calendar.DAY_OF_MONTH)
        val currentMill:Int = now.get(Calendar.MILLISECOND)
        invoice = "INV$currentYear$currentMonth$currentDay$currentMill"
        amount = intent.getDoubleExtra("Total", 0.0)
        See.log("total amount ${amount.toInt()}")
        total = amount.toInt()

        tvTitle.setText("Pembayaran QRIS")
        btnBack.setOnClickListener {
            onBackPressed()
        }

        LoadCreateQr()



//  TvNominalQr.setText(MyUtil.numberToCurrency(nominal))
    }

    private fun LoadCreateQr() {
        val  amount = intent.getDoubleExtra("Total", 0.0)
        fun generateMerchantRef(): String {
            val uuid = UUID.randomUUID().toString().take(8).toUpperCase()
            val timestamp = System.currentTimeMillis().toString()
            return "MERCHANT-$timestamp-$uuid"
        }

        invoice = generateMerchantRef()
       progressDialog?.show()
        AndroidNetworking.post(MyConstant.Urlcreateqr)
            .addBodyParameter("merchant_id","604277662")
            .addBodyParameter("merchant_ref",invoice)
            .addBodyParameter("amount",amount.toString())
            .addBodyParameter("notif_url","http://202.150.155.88:61080/umkm_core/public/api/post-fcm-qr")
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response.toString()
                    val json = JSONObject(respon)
                    val apiMessage = json.getString("message")
                    val apiStatus = json.getBoolean("status")
                    if (apiStatus.equals(true)) {


                        val data = Gson().fromJson(respon, ResponseQrisKaltim::class.java)
                        val qrContent = data.data.qr_string
                        if (qrContent.isNotEmpty()) {
                            val writer = QRCodeWriter()
                            val bitMatrix =
                                writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512)
                            val width = bitMatrix.width
                            val height = bitMatrix.height
                            val bitmap =
                                Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                            for (x in 0 until width) {
                                for (y in 0 until height) {
                                    bitmap.setPixel(
                                        x,
                                        y,
                                        if (bitMatrix.get(
                                                x,
                                                y
                                            )
                                        ) Color.BLACK else Color.WHITE
                                    )
                                }
                            }


                            TvNominalQr.setText(numberToCurrency(amount))


                            TvQrTanggalBerlaku.setText("Bayar Sebelum  " + "${data.data.qr_expired}" + "  WIB")

                            idIVQrcode.setImageBitmap(bitmap)

                            UploadToServer(data.data.bill_number,data.data.qr_string,data.data.nmid,data.data.acquirer_name)

                        }




                    } else {
                        See.toast(this@QrisMockUp, apiMessage)
                    }




                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    See.toast(this@QrisMockUp,
                        "Mohon Check Koneksi Internet Anda.. \nCode Error :  ${anError?.errorCode}"
                    )
                    See.log("anError QrisMockUp errorCode : ${anError?.errorCode}")
                    See.log("anError QrisMockUp errorBody : ${anError?.errorBody}")
                    See.log("anError QrisMockUp errorDetail : ${anError?.errorDetail}")
                }

            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent  = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun UploadToServer(billNumber: String, qrString: String, nmid: String, bank :String) {

        val transaksi =
            SugarRecord.find(Transaksi::class.java, "status = ?", "1").firstOrNull()
        if (transaksi != null) {
            transaksi.status = false

            See.log("post Transaksi : diskon ${transaksi.diskon}")

            AndroidNetworking.post(MyConstant.Urltransaksiinput)
                .addHeaders("Authorization", "Bearer$token")
                .addBodyParameter("bayar", transaksi?.bayar?.toInt().toString().trim())
                .addBodyParameter("diskon",transaksi?.diskon?.toInt().toString().trim())
                .addBodyParameter("nama_pembeli",transaksi?.namaPembeli.toString().trim() )
                .addBodyParameter("status",1.toString().trim() )
                .addBodyParameter("total_pembayaran",transaksi?.totalPembayaran?.toInt().toString().trim() )
                .addBodyParameter("invoice",billNumber)
                .addBodyParameter("bank",bank)
                .addBodyParameter("nokartu", qrString)
                .addBodyParameter("saldoawal", saldoawal.toString())
                .addBodyParameter("saldoakhir", saldoakhir.toString())
                .addBodyParameter("tid", nmid)
                .addBodyParameter("username", username.trim())
                .addBodyParameter("casier", nama.trim())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        val respon = response?.toString()
                        See.log("respon transaksi : $respon")
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt(MyConstant.API_STATUS)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiStatus.equals(1)) {
                            progressDialog!!.dismiss()

                            val data = Gson().fromJson(respon, ResponseId::class.java).data


                            UploadItemTranksaksi(transaksi.id,data.id)
                            id_transaksi = data.id

                            See.toast(this@QrisMockUp, "Upload Trx to Server $apiMessage")
                        } else {
                            progressDialog!!.dismiss()
                            See.toast(this@QrisMockUp, "Upload Trx to Server $apiMessage")

                        }

                    }

                    override fun onError(anError: ANError?) {

                        progressDialog?.dismiss()
                        val json = JSONObject(anError?.errorBody)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiMessage != null) {
                            if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                getToken(this@QrisMockUp)
                            }
                        }

                        See.log("onError getProduk errorCode : ${anError?.errorCode}")
                        See.log("onError getProduk errorBody : ${anError?.errorBody}")
                        See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                    }

                })

            transaksi.save()
//                        kurangiStok(transaksi.id);

        }



    }

    private fun UploadItemTranksaksi(id: Long?,id_transaksi: Int?) {
        val itemTransaksi =
            SugarRecord.find(ItemTransaksi::class.java, "id_transaksi = ?", "${id}")
        var arrayList: ArrayList<ItemTransaksiJsonOnline> = ArrayList()
        itemTransaksi.forEach {
            arrayList.add(
                ItemTransaksiJsonOnline(it.diskonProduk?.toInt(),
                    it.fotoProduk,
                    it.hargaProduk?.toInt(),
                    id_transaksi,
                    it.jumlah,
                    it.minimalPembelianProduk,
                    it.namaProduk,
                    it.produkId?.toInt(),
                    it.satuan,
                    it.stokProduk,
                    username
                )
            )

        }

        val json = Gson().toJson(arrayList)
        See.log("data json $json")

        AndroidNetworking.post(MyConstant.url+"/item_transaksi_produk_transaksi_insert")
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
            .addBodyParameter("data_produk",json)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    val apiStatus = response?.getInt(MyConstant.API_STATUS)
                    val apiMessage = response?.getString(MyConstant.API_MESSAGE)
                    if (apiStatus!!.equals(0)) {
                        See.toast(this@QrisMockUp, apiMessage!!)

                        UpdateProdukStok(id_transaksi)


                    }
                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@QrisMockUp)
                        }
                    }

                    See.log("onError keranjang errorCode : ${anError?.errorCode}")
                }

            })



    }

    private fun UpdateProdukStok(id_transaksi: Int?) {
        progressDialog?.show()

        AndroidNetworking.post(MyConstant.url+"/item_transaksi_get_id_transaksi")
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
            .addBodyParameter(MyConstant.ID_TRANSAKSI, id_transaksi.toString())
            .addBodyParameter(MyConstant.USERNAME, username)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener{

                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response.toString()
                    See.log("respon item id $id_transaksi : respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        See.toast(this@QrisMockUp, apiMessage)
                    }
                    else {
                        See.toast(this@QrisMockUp, apiMessage)
                    }

                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@QrisMockUp)
                        }
                    }

                    See.log("onError keranjang errorCode : ${anError?.errorCode}")
                }

            })

    }

}
package com.svtech.mandiri.Activities

import android.app.ProgressDialog
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
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.Cons
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.numberToCurrency
import com.svtech.mandiri.modelOnline.ResponseQris
import kotlinx.android.synthetic.main.activity_qris_mock_up.TvNominalQr
import kotlinx.android.synthetic.main.activity_qris_mock_up.TvQrTanggalBerlaku
import kotlinx.android.synthetic.main.activity_qris_mock_up.idIVQrcode
import kotlinx.android.synthetic.main.toolbar_with_back.btnBack
import kotlinx.android.synthetic.main.toolbar_with_back.tvTitle
import org.json.JSONObject
import java.util.Calendar
import java.util.Date
import java.util.UUID

class QrisMockUp : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
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

        tvTitle.setText("Pembayaran QRIS")
        btnBack.setOnClickListener {
            onBackPressed()
            finish()
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
       progressDialog?.show()
        AndroidNetworking.post(MyConstant.Urlcreateqr)
            .addBodyParameter("merchant_id","100007727")
            .addBodyParameter("merchant_ref",generateMerchantRef())
            .addBodyParameter("amount",amount.toString())
            .addBodyParameter("notif_url","http://202.150.155.88:61080/umkm_core/public/admin/login")
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

                        val data = Gson().fromJson(respon, ResponseQris::class.java)
                        val qrContent = data.data.qrContent
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


                            TvQrTanggalBerlaku.setText("Bayar Sebelum  " + "${data.data.expired_at}" + "  WIB")

                            idIVQrcode.setImageBitmap(bitmap)

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
        finish()
    }
}
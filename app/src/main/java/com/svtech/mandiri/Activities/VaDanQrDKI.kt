package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.svtech.mandiri.R
import kotlinx.android.synthetic.main.activity_va_dki.TvTotalTagihan
import kotlinx.android.synthetic.main.toolbar_with_back.btnBack
import kotlinx.android.synthetic.main.toolbar_with_back.tvTitle

class VaDanQrDKI : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_va_dki)

      val  amount = intent.getDoubleExtra("Total", 0.0)

        TvTotalTagihan.setText("Rp. "+amount.toInt().toString())
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        tvTitle.setText("Pembayaran Va Bank Dki")
        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }
        progressDialog!!.show()
//        Handler().postDelayed({
//            progressDialog!!.dismiss()
//
//        },3000)

        AndroidNetworking.post("")
            .addBodyParameter("merchant_id","")
            .addBodyParameter("merchant_id","")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
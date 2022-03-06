package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.nfc.NfcManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.pattra.pattracardsdk.*
import com.pattra.pattracardsdk.Service.SyncService
import com.svtech.dhuwit.R
import com.pattra.pattracardsdk.UangElektronikSDK
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.See
import kotlinx.android.synthetic.main.activity_cash.*
import kotlinx.android.synthetic.main.dialogdone.*


class CashActivity : AppCompatActivity() {

    var uangElektronikSDK: UangElektronikSDK? = null
    var progressDialog: ProgressDialog? = null
    var amount = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cash)
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Loading...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        val myIntent = Intent(this, SyncService::class.java)
        startService(myIntent)

        amount = intent.getStringExtra("Total").toString()
        balance_pay_total.text = amount

        val manager = getSystemService(NFC_SERVICE) as NfcManager
        val nfcAdapter = manager.defaultAdapter

        if (nfcAdapter != null && nfcAdapter.isEnabled) {

            //Yes NFC available
            Toast.makeText(applicationContext, "nfc available", Toast.LENGTH_LONG).show()
        } else if (nfcAdapter != null && !nfcAdapter.isEnabled) {

            //NFC is not enabled.Need to enable by the user.
            Toast.makeText(applicationContext, "nfc tidak aktif", Toast.LENGTH_LONG).show()
        } else {
            //NFC is not supported
            Toast.makeText(applicationContext, "nfc tidak support", Toast.LENGTH_LONG).show()
        }
        val cardCallback: UangElektronikSDK.CardCallback =
            object : UangElektronikSDK.CardCallback {
                override fun cardResult(cardResult: CardResult?) {
                  runOnUiThread {
                      kotlin.run {
                          card_number.setText(cardResult?.cardNumber)
                          balance_value.setText(cardResult?.balance)

                          when (cardResult?.bank) {
                              "DKI" -> {
                                  card_background.setBackgroundResource(R.drawable.dki)
                              }
                              "BNI" -> {
                                  card_background.setBackgroundResource(R.drawable.tapcash)
                              }
                              "MANDIRI" -> {
                                  card_background.setBackgroundResource(R.drawable.mandiri)
                              }
                              "BRI" -> {
                                  card_background.setBackgroundResource(R.drawable.brizzi)
                              }
                              else -> {
                                  card_background.setBackgroundResource(R.drawable.logo)
                              }
                          }
                      }
                  }
                }

                override fun cardError(cardError: CardError?) {
                   runOnUiThread {
                       kotlin.run {
                           See.toast(this@CashActivity, "${cardError?.errorMsg}")
                           if (cardError?.errorCode.equals("PATTRACARD03")){
                               btn_pay.isEnabled = false
                               progressDialog?.dismiss()
                           }
                           else {
                               btn_pay.isEnabled= false
                               progressDialog?.dismiss()

                           }
                       }
                   }
                }

                override fun purchaseSuccess(purchaseData: PurchaseData?) {
                    runOnUiThread {
                        kotlin.run {
                            balance_value.setText(purchaseData?.lastBalance)
                            showAlertDialog(purchaseData?.cardNumber!!,
                                purchaseData?.debitAmount,
                            purchaseData?.prevBalance,
                            purchaseData?.lastBalance,
                            purchaseData?.bank)
                            btn_pay.isEnabled = false
                        }
                    }
                }

                override fun onServerConnect() {
                   runOnUiThread {
                       kotlin.run {
                           tv_status_server.setText(getString(R.string.serverOn))
                           iv_status_server.setImageResource(R.drawable.ic_baseline_wifi_24)
                       }
                   }
                }

                override fun onServerDisconnect() {
                   runOnUiThread {
                       kotlin.run {
                           btn_pay.isEnabled = false
                           tv_status_server.setText(getString(R.string.serverOff))
                           iv_status_server.setImageResource(R.drawable.ic_baseline_wifi_off_24)
                       progressDialog?.dismiss()
                       }
                   }
                }

                override fun onPingSuccess() {

                    btn_pay.isEnabled = true
                }

                override fun onLoading(loadingDialog: LoadingDialog?) {
                    if (loadingDialog!!.isLoading){
                        progressDialog?.show()
                    }
                    else {
                        progressDialog!!.dismiss()
                    }

                }
            }
        uangElektronikSDK = UangElektronikSDK(this, cardCallback, "203.210.87.98", "didik")
        btn_pay.setOnClickListener {
//            val amount: Int = balance_pay_total.getText().toString().toInt()
            uangElektronikSDK!!.startPurchase(amount.toInt(), "INV2021092802", "android didik", false)
        }

    }

    private fun showAlertDialog(nokartu:String,nominal:String,saldoawal:String,saldoakhir:String,bank:String)  {
        val alertDialog = AlertDialog.Builder(this)
        val customLayout = layoutInflater.inflate(R.layout.dialogdone, null)
        alertDialog.setView(customLayout)
      val  vNokartu = customLayout.findViewById<TextView>(R.id.TvDialogCardNumber)
        val vNominal = customLayout.findViewById<TextView>(R.id.TvDialogDebitAmount)
        val  vSaldoAwal = customLayout.findViewById<TextView>(R.id.TvDialogPrevBalance)
        val  vSaldoAkhir = customLayout.findViewById<TextView>(R.id.TvDialogLastBalance)
        val  vBank = customLayout.findViewById<TextView>(R.id.TvDialogBank)

        val  btnDialogClose = customLayout.findViewById<Button>(R.id.btnDialogClose)
        val   btnDialogCheckTrx = customLayout.findViewById<Button>(R.id.btnDialogCheckTrx)

        vNokartu.text = "No. Kartu : $nokartu"
        vNominal.text = "Nominal : $nominal"
        vSaldoAwal.text = "Saldo Awal : $saldoawal"
        vSaldoAkhir.text = "Saldo Akhir : $saldoakhir"
        vBank.text = "Bank : $bank"
        val alert = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
        btnDialogClose.setOnClickListener {
            See.toast(this, "btn close alert di klik")

        }
        btnDialogCheckTrx.setOnClickListener {
            See.toast(this, "btn close alert di Trx")
        }
    }
}
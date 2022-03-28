package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.nfc.NfcManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.pattra.pattracardsdk.*
import com.pattra.pattracardsdk.PattraCardConfig.ConfigCallback
import com.pattra.pattracardsdk.Service.SyncService
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.Utils.getDeviceId
import kotlinx.android.synthetic.main.activity_cash.*
import org.json.JSONObject


class CashActivity : AppCompatActivity() {
    var pattraCardConfig: PattraCardConfig? = null
    var pattraCardSDK: PattraCardSDK? = null


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

//        amount = intent.getStringExtra("Total").toString()
        balance_pay_total.text = "1"

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

        pattraCardConfig = PattraCardConfig(object : ConfigCallback {
            override fun ConfigError(s: String) {
                See.log("Config Error $s")

            }

            override fun ConfigSuccess(person: JSONObject) {
                val respon = person.toString()
                See.log("ConfigSuccess $respon")

            }

            override fun LogSuccess(jsonObject: JSONObject) {
                val respon = jsonObject.toString()
                See.log("ConfigSuccess $respon")

            }

            override fun LogError(jsonObject: JSONObject) {
                val respon = jsonObject.toString()
                See.log("LogError $respon")
            }
        })

        pattraCardConfig!!.getConfig("203.210.87.98", getDeviceId(this))


        val cardCallback: PattraCardSDK.CardCallback = object : PattraCardSDK.CardCallback {
            override fun cardResult(cardResult: CardResult) {
                runOnUiThread {

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

            override fun cardError(cardError: CardError) {
                runOnUiThread {
                    Toast.makeText(applicationContext, cardError.errorMsg, Toast.LENGTH_LONG).show()
                    See.toast(this@CashActivity, "${cardError?.errorMsg}")
                    if (cardError?.errorCode.equals("PATTRACARD03")) {
                        btn_pay.isEnabled = false
                        progressDialog?.dismiss()
                    } else {
                        btn_pay.isEnabled = false
                        progressDialog?.dismiss()

                    }
                }
            }

            override fun purchaseSuccess(purchaseData: PurchaseData) {
                runOnUiThread {

                    balance_value.setText(purchaseData?.lastBalance)
                    showAlertDialog(
                        purchaseData?.cardNumber!!,
                        purchaseData?.debitAmount,
                        purchaseData?.prevBalance,
                        purchaseData?.lastBalance,
                        purchaseData?.bank
                    )
                    btn_pay.isEnabled = false

                }
            }

            override fun onServerConnect(BrokerUri: String) {
                runOnUiThread {
                    tv_status_server.setText("Server Connected")

                    iv_status_server.setImageResource(R.drawable.ic_baseline_wifi_24)
                }
            }

            override fun onServerDisconnect() {
                runOnUiThread {
                    btn_pay.setEnabled(false)
                    tv_status_server.setText("Server Disconnected")

                    iv_status_server.setImageResource(R.drawable.ic_baseline_wifi_off_24)
                    progressDialog!!.dismiss()
                }
            }

            override fun onPingSuccess() {
                btn_pay.setEnabled(true)
            }

            override fun onLoading(loadingDialog: LoadingDialog) {
                if (loadingDialog.isLoading) {
                    progressDialog!!.show()
                } else {
                    progressDialog!!.dismiss()
                }
            }
        }
        pattraCardSDK = PattraCardSDK(this, cardCallback, "203.210.87.98", "didik")
        btn_pay.setOnClickListener {
            pattraCardSDK!!.startPurchase(1, "", "android test", false)
        }


    }

    private fun showAlertDialog(
        nokartu: String,
        nominal: String,
        saldoawal: String,
        saldoakhir: String,
        bank: String
    ) {
        val alertDialog = AlertDialog.Builder(this)
        val customLayout = layoutInflater.inflate(R.layout.dialogdone, null)
        alertDialog.setView(customLayout)
        val vNokartu = customLayout.findViewById<TextView>(R.id.TvDialogCardNumber)
        val vNominal = customLayout.findViewById<TextView>(R.id.TvDialogDebitAmount)
        val vSaldoAwal = customLayout.findViewById<TextView>(R.id.TvDialogPrevBalance)
        val vSaldoAkhir = customLayout.findViewById<TextView>(R.id.TvDialogLastBalance)
        val vBank = customLayout.findViewById<TextView>(R.id.TvDialogBank)

        val btnDialogClose = customLayout.findViewById<Button>(R.id.btnDialogClose)


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
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        pattraCardSDK?.enableReaderMode();
    }

    override fun onRestart() {
        super.onRestart()
        pattraCardSDK?.disableReaderMode();
    }

}
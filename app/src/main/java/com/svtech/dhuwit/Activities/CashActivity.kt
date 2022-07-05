package com.svtech.dhuwit.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.nfc.NfcManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.orm.SugarRecord
import com.pattra.pattracardsdk.*
import com.pattra.pattracardsdk.Service.SyncService
import com.svtech.dhuwit.Models.ItemTransaksi
import com.svtech.dhuwit.Models.Profile
import com.svtech.dhuwit.Models.Transaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOnline.ItemTransaksiJsonOnline
import com.svtech.dhuwit.modelOnline.ResponseId
import com.svtech.dhuwit.modelOnline.item_transaksi_get_id_transaksi
import kotlinx.android.synthetic.main.activity_cash.*
import kotlinx.android.synthetic.main.sheet_config.view.*
import org.json.JSONObject
import java.io.IOException
import java.util.*


class CashActivity : AppCompatActivity() {
    var pattraCardSDK: PattraCardSDK? = null


    var mBluetoothAdapter: BluetoothAdapter? = null
    var applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    lateinit var mBluetoothConnectProgressDialog: ProgressDialog
    lateinit var mBluetoothSocket: BluetoothSocket
    var mBluetoothDevice: BluetoothDevice? = null
    var deviceName: String? = null

    var token = ""
    var username = ""
    var progressDialog : ProgressDialog? = null
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
        setContentView(R.layout.activity_cash)
        val currentYear: Int = now.get(Calendar.YEAR)
        val currentMonth: Int = now.get(Calendar.MONTH) + 1
        val currentDay: Int = now.get(Calendar.DAY_OF_MONTH)
        val currentMill:Int = now.get(Calendar.MILLISECOND)
        invoice = "INV$currentYear$currentMonth$currentDay$currentMill"
        amount = intent.getDoubleExtra("Total", 0.0)
        See.log("total amount ${amount.toInt()}")
        total = amount.toInt()


        /*Setting toolbar*/
        setToolbar(this, "Transaksi CashCard")

        checkPairedDevice()
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Loading...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        token = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "").toString()
        nama = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.NAMA,"").toString()



        btnConnectBluetooth.setOnClickListener {
            if (btnConnectBluetooth.text.equals("Connect")) {
                if (mBluetoothAdapter == null) {
                    Toast.makeText(this, "Bluetooth Adapter null", Toast.LENGTH_SHORT).show()
                } else {
                    /*Check bluetooth status*/
                    if (!mBluetoothAdapter!!.isEnabled) {
                        startActivityForResult(
                            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                            MyConstant.REQUEST_ENABLE_BT
                        )
                    } else {
                        /*Menampilkan list device bluetooth dan printer*/
                        startActivityForResult(
                            Intent(this, BluetoothDeviceListActivity::class.java),
                            MyConstant.REQUEST_CONNECT_DEVICE
                        )
                    }
                }
            } else if (btnConnectBluetooth.text.equals("Disconnect")) {
                /*Memutuskan koneksi printer*/
                    btn_pay.visibility = View.INVISIBLE
                if (mBluetoothAdapter != null) mBluetoothAdapter!!.disable()
                if (mBluetoothSocket.isConnected) closeSocket(mBluetoothSocket)
                savePreferences(this, MyConstant.DEVICE_ADDRESS, "")
                tvPrinterStatus.setText("Disconnected")
                btnConnectBluetooth.text = "Connect"
            }

        }


        val myIntent = Intent(this, SyncService::class.java)
        startService(myIntent)

//        amount = intent.getStringExtra("Total").toString()
        balance_pay_total.text = "$total"

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

        btnStatusCashCard.setOnClickListener {
            val btnSheet = layoutInflater.inflate(R.layout.sheet_config, null)
            val dialog = BottomSheetDialog(this)
            val TvClose = btnSheet.TvClose
            val TvMessageStatus = btnSheet.TvMessageStatus
            val TvImei = btnSheet.TvImei
            TvImei.text = ": ${getDeviceId(this)}"
            TvMessageStatus.text = ": $message"

            TvClose.setOnClickListener {
                dialog.dismiss()
            }
            dialog.setContentView(btnSheet)
            dialog.show()
        }


        val cardCallback: PattraCardSDK.CardCallback = object : PattraCardSDK.CardCallback {
            override fun cardResult(cardResult: CardResult) {
                runOnUiThread {

                    card_number.setText(cardResult?.cardNumber)
                    balance_value.setText(cardResult?.balance)

                    when (cardResult?.bank) {
                        "DKI" -> {
                            card_background.setBackgroundResource(R.drawable.jakcard)
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
                    bank = purchaseData?.bank
                    nokartu = purchaseData?.cardNumber
                    saldoawal = purchaseData?.prevBalance.toInt()
                    saldoakhir = purchaseData?.lastBalance.toInt()
                    tid = purchaseData?.tid

//                    print()
                    UploadToServer()
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
//                if (btnConnectBluetooth.text.equals("Disconnect")){
                    btn_pay.setEnabled(true)
//                } else {
//                    See.toast(this@CashActivity, "Mohon Connectkan Printer..")
//                }

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
            pattraCardSDK!!.startPurchase(total, invoice, username, false)

//            UploadToServer()
        }


    }

    private fun UploadToServer() {

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
                    .addBodyParameter("status",0.toString().trim() )
                    .addBodyParameter("total_pembayaran",transaksi?.totalPembayaran?.toInt().toString().trim() )
                    .addBodyParameter("invoice",invoice)
                    .addBodyParameter("bank",bank)
                    .addBodyParameter("nokartu", nokartu)
                    .addBodyParameter("saldoawal", saldoawal.toString())
                    .addBodyParameter("saldoakhir", saldoakhir.toString())
                    .addBodyParameter("tid", tid)
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

                                See.toast(this@CashActivity, "Upload Trx to Server $apiMessage")
                            } else {
                                progressDialog!!.dismiss()
                                See.toast(this@CashActivity, "Upload Trx to Server $apiMessage")

                            }

                        }

                        override fun onError(anError: ANError?) {

                            progressDialog?.dismiss()
                            val json = JSONObject(anError?.errorBody)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiMessage != null) {
                                if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                    getToken(this@CashActivity)
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
                        See.toast(this@CashActivity, apiMessage!!)

                        UpdateProdukStok(id_transaksi)


                    }
                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@CashActivity)
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
                        See.toast(this@CashActivity, apiMessage)
                    }
                    else {
                        See.toast(this@CashActivity, apiMessage)
                    }

                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@CashActivity)
                        }
                    }

                    See.log("onError keranjang errorCode : ${anError?.errorCode}")
                }

            })

    }


    private fun checkPairedDevice() {
        if (mBluetoothAdapter == null) Toast.makeText(
            this,
            "Bluetooth Adapter null",
            Toast.LENGTH_SHORT
        ).show()
        else {
            if (!mBluetoothAdapter!!.isEnabled)
                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            else {
                val address = com.svtech.dhuwit.Utils.getPreferences(this)
                    .getString(MyConstant.DEVICE_ADDRESS, "")!!
                if (address.isNotEmpty()) {
                    mBluetoothDevice = mBluetoothAdapter?.getRemoteDevice(address)
                    deviceName = mBluetoothDevice?.name
                    mBluetoothConnectProgressDialog = ProgressDialog.show(
                        this, "Connecting...",
                        mBluetoothDevice!!.name + "\n" + mBluetoothDevice!!.address,
                        true, false
                    )
                    Thread {
                        try {
                            mBluetoothSocket =
                                mBluetoothDevice?.createInsecureRfcommSocketToServiceRecord(
                                    applicationUUID
                                )!!
                            mBluetoothAdapter?.cancelDiscovery()
                            mBluetoothSocket.connect()
                            mHandler.sendEmptyMessage(0)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            closeSocket(mBluetoothSocket)
                        } finally {
                            Thread.sleep(3000)
                            if (!mBluetoothSocket.isConnected) {
                                mBluetoothConnectProgressDialog.dismiss()
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        "Device tidak merespon!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }.start()
                }
            }
        }

    }


    private fun closeSocket(nOpenSocket: BluetoothSocket?) {
        try {
            nOpenSocket?.close()
            Log.d("Bluetooth", "SocketClosed")
        } catch (ex: IOException) {
            Log.d("Bluetooth", "CouldNotCloseSocket")
        }
    }


    private val mHandler =
        Handler(Handler.Callback {
            mBluetoothConnectProgressDialog.dismiss()
            tvPrinterStatus.text = "Connected to $deviceName"

            btnConnectBluetooth.text = "Disconnect"
            return@Callback true
        })



    private fun print() {
        val profile = SugarRecord.listAll(Profile::class.java).firstOrNull()
        val transaksi = SugarRecord.find(Transaksi::class.java, "status = ?", "1").firstOrNull()
        val itemsTrasaksi =
            SugarRecord.find(ItemTransaksi::class.java, "id_transaksi = ?", "${transaksi?.id}")
        Thread {
            try {

                val outputStream = mBluetoothSocket.outputStream
                val printer = BluetoothPrinterUtils(outputStream)
                printer.setFontStyle(BluetoothPrinterUtils.f2)
                printer.printText(profile?.namaToko!!, BluetoothPrinterUtils.ALIGN_CENTER)
                printer.printText(profile.alamatToko!!, BluetoothPrinterUtils.ALIGN_CENTER)
                printer.printNewLine()
                printer.printText(transaksi?.tanggalTrasaksi!!, BluetoothPrinterUtils.ALIGN_RIGHT)
                printer.printText(invoice,BluetoothPrinterUtils.ALIGN_RIGHT)
                printer.printText("Kasir : $nama", BluetoothPrinterUtils.ALIGN_LEFT)
                printer.printLine()
//                val item = java.lang.String.format(
//                    "%1$-12s %2$-12s %3$-5s %4$-12s",
//                    "Nama",
//                    "Harga",
//                    "Jumlah",
//                    "Total"
//                )
//                printer.printText(item, BluetoothPrinterUtils.ALIGN_LEFT)
//                printer.printText(item, BluetoothPrinterUtils.ALIGN_CENTER)
                for (it in itemsTrasaksi) {
                    val item = java.lang.String.format(
                        "%1s \n %2s X %3s %4$-5s",
                        if (it.namaProduk.toString().length >= 10) it.namaProduk?.substring(
                            0,
                            10
                        ) else it.namaProduk.toString(),
                        numberToCurrency(it.hargaProduk!!).removePrefix("Rp. "),
                        it.jumlah.toString(),
                        numberToCurrency(it.jumlah?.times(it.hargaProduk!!)!!).removePrefix("Rp. ")
                    )
                    printer.printText(item, BluetoothPrinterUtils.ALIGN_LEFT)
                }
                printer.printLine()
                var str = java.lang.String.format(
                    "%1$-10s %2$-10s %3$-20s",
                    " ",
                    "Total",
                    numberToCurrency(transaksi.totalPembayaran!!)
                )
                printer.printString(str, BluetoothPrinterUtils.ALIGN_RIGHT)
                str = java.lang.String.format(
                    "%1$-10s %2$-12s %3$-18s",
                    " ",
                    "Bank",
                    bank
                )
                printer.printString(str, BluetoothPrinterUtils.ALIGN_RIGHT)
                str = java.lang.String.format(
                    "%1$-2s %2$-12s %3$-18s",
                    " ",
                    "nokartu",
                    nokartu
                )
                printer.printString(str, BluetoothPrinterUtils.ALIGN_RIGHT)
                str = java.lang.String.format(
                    "%1$-2s %2$-12s %3$-18s",
                    " ",
                    "saldoawal",
                    numberToCurrency(saldoawal)
                )
                printer.printString(str, BluetoothPrinterUtils.ALIGN_RIGHT)
                str = java.lang.String.format(
                    "%1$-2s %2$-12s %3$-18s",
                    " ",
                    "saldoakhir",
                    numberToCurrency(saldoakhir)
                )
                printer.printString(str, BluetoothPrinterUtils.ALIGN_RIGHT)
                str = java.lang.String.format(
                    "%1$-10s %2$-12s %3$-18s",
                    " ",
                    "TID",
                    tid
                )
                printer.printString(str, BluetoothPrinterUtils.ALIGN_RIGHT)

                printer.printNewLines(2)
//                printer.printText(transaksi.namaPembeli!!, BluetoothPrinterUtils.ALIGN_CENTER)
                printer.printText("Terimakasih Sudah Berbelanja", BluetoothPrinterUtils.ALIGN_CENTER)
                printer.printNewLines(3)

                UploadToServer()

            } catch (e: Exception) {
                Log.e("Bluetooth", "Exe ", e)
            }
        }.start()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MyConstant.REQUEST_CONNECT_DEVICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    mBluetoothDevice =
                        mBluetoothAdapter?.getRemoteDevice(data?.getStringExtra(MyConstant.DEVICE_ADDRESS))
                    deviceName = mBluetoothDevice?.name
                    savePreferences(this, MyConstant.DEVICE_ADDRESS, mBluetoothDevice?.address!!)
                    mBluetoothConnectProgressDialog = ProgressDialog.show(
                        this, "Connecting...",
                        mBluetoothDevice!!.name + "\n" + mBluetoothDevice!!.address,
                        true, true
                    )

                    Thread {
                        try {
                            mBluetoothSocket =
                                mBluetoothDevice?.createInsecureRfcommSocketToServiceRecord(
                                    applicationUUID
                                )!!
                            mBluetoothAdapter?.cancelDiscovery()
                            mBluetoothSocket.connect()
                            mHandler.sendEmptyMessage(0)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            closeSocket(mBluetoothSocket)
                        } finally {
                            Thread.sleep(7000)
                            if (!mBluetoothSocket.isConnected) {
                                mBluetoothConnectProgressDialog.dismiss()
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        "Device tidak merespon!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }.start()
                }
            }
            MyConstant.REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    startActivityForResult(
                        Intent(this, BluetoothDeviceListActivity::class.java),
                        MyConstant.REQUEST_ENABLE_BT
                    )
                }
            }


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
            finish()
            val intent = Intent(this, StrukActivity::class.java)
            intent.putExtra(MyConstant.ID_TRANSAKSI, id_transaksi)
            startActivity(intent)



//            startActivity(Intent(this, DashboardActivity::class.java))
        }

    }

    override fun onStart() {
        super.onStart()
        checkPairedDevice()
    }

    override fun onResume() {
        super.onResume()
        pattraCardSDK!!.enableReaderMode()
    }

    override fun onPause() {
        super.onPause()
        pattraCardSDK!!.disableReaderMode()
    }



    override fun onDestroy() {
        super.onDestroy()
        try {
            if (mBluetoothSocket != null) mBluetoothSocket.close()
        } catch (e: java.lang.Exception) {
            Log.e("Bluetooth", "Exe ", e)
        }
    }

}
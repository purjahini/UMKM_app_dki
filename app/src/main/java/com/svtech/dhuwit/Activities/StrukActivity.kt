package com.svtech.dhuwit.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.orm.SugarRecord
import com.svtech.dhuwit.AdapterOnline.RclvStrukDetail
import com.svtech.dhuwit.AdapterOnline.ResponseStruk
import com.svtech.dhuwit.Models.ItemTransaksi
import com.svtech.dhuwit.Models.Profile
import com.svtech.dhuwit.Models.Transaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import kotlinx.android.synthetic.main.activity_checkout.*
import kotlinx.android.synthetic.main.activity_struk.*
import kotlinx.android.synthetic.main.activity_struk.btnConnectBluetooth
import kotlinx.android.synthetic.main.activity_struk.btnPrint
import kotlinx.android.synthetic.main.activity_struk.btnSelesai
import kotlinx.android.synthetic.main.activity_struk.tvAlamatCafe
import kotlinx.android.synthetic.main.activity_struk.tvNamaCafe
import kotlinx.android.synthetic.main.activity_struk.tvPrinterStatus
import kotlinx.android.synthetic.main.activity_struk.tvTanggal
import kotlinx.android.synthetic.main.activity_struk.tvTotalBayar
import org.json.JSONObject
import java.io.IOException
import java.util.*

class StrukActivity : AppCompatActivity() {
    var mBluetoothAdapter: BluetoothAdapter? = null
    var applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    lateinit var mBluetoothConnectProgressDialog: ProgressDialog
    lateinit var mBluetoothSocket: BluetoothSocket
    var mBluetoothDevice: BluetoothDevice? = null
    var deviceName: String? = null
    var token = ""
    var username = ""
    var id_transaksi = 0
    var progressDialog: ProgressDialog? = null

    var bank = ""
    var nokartu = ""
    var saldoawal = 0
    var saldoakhir = 0
    var tid = ""
    var invoice = ""
    var nama = ""
    var totalPembayaran = 0

   lateinit var item_produk: List<ResponseStruk.ItemProduk>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_struk)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        /*Setting toolbar*/
        setToolbar(this, "Struk")

        /*Check printer yang tersimpan*/
        checkPairedDevice()

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
                if (mBluetoothAdapter != null) mBluetoothAdapter!!.disable()
                if (mBluetoothSocket.isConnected) closeSocket(mBluetoothSocket)
                savePreferences(this, MyConstant.DEVICE_ADDRESS, "")
                tvPrinterStatus.setText("Disconnected")
                btnPrint.isEnabled = false
                btnConnectBluetooth.text = "Connect"
            }
        }

        btnPrint.setOnClickListener {
            /*Fungsi untuk melakukan print*/
            print()
        }



    }

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
                for (it in item_produk) {
                    val items = java.lang.String.format(
                        "%1s \n %2s X %3s %4$-5s",
                        if (it.nama_produk.toString().length >= 20) it.nama_produk?.substring(
                            0,
                            20
                        ) else it.nama_produk.toString(),
                        numberToCurrency(it.harga_produk!!).removePrefix("Rp. "),
                        it.jumlah.toString(),
                        numberToCurrency(it.jumlah?.times(it.harga_produk!!)!!).removePrefix("Rp. ")
                    )
                    printer.printText(items, BluetoothPrinterUtils.ALIGN_LEFT)
                }

//                for (it in itemsTrasaksi) {
//                    val item = java.lang.String.format(
//                        "%1s \n %2s X %3s %4$-5s",
//                        if (it.namaProduk.toString().length >= 10) it.namaProduk?.substring(
//                            0,
//                            10
//                        ) else it.namaProduk.toString(),
//                        numberToCurrency(it.hargaProduk!!).removePrefix("Rp. "),
//                        it.jumlah.toString(),
//                        numberToCurrency(it.jumlah?.times(it.hargaProduk!!)!!).removePrefix("Rp. ")
//                    )
//                    printer.printText(item, BluetoothPrinterUtils.ALIGN_LEFT)
//                }
                printer.printLine()
                var str = java.lang.String.format(
                    "%1$-10s %2$-10s %3$-20s",
                    " ",
                    "Total",
                    numberToCurrency(totalPembayaran)
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
                val address = getPreferences(this).getString(MyConstant.DEVICE_ADDRESS, "")!!
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
        }

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

                                invoice = it.invoice
                                totalPembayaran = it.total_pembayaran
                                bank = it.bank
                                nokartu = it.nokartu
                                saldoawal = it.saldoawal.toInt()
                                saldoakhir = it.saldoakhir.toInt()
                                tid = it.tid

                        }

                        val itemTransaksiProduk = data.item_produk

                        item_produk = data.item_produk

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
            btnPrint.isEnabled = true
            btnConnectBluetooth.text = "Disconnect"
            return@Callback true
        })

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (mBluetoothSocket != null) mBluetoothSocket.close()
        } catch (e: java.lang.Exception) {
            Log.e("Bluetooth", "Exe ", e)
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
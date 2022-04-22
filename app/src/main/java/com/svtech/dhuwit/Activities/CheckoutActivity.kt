package com.svtech.dhuwit.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.*
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import kotlinx.android.synthetic.main.activity_checkout.*
import kotlinx.android.synthetic.main.layout_table_row.view.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CheckoutActivity : AppCompatActivity() {
    var mBluetoothAdapter: BluetoothAdapter? = null
    var applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    lateinit var mBluetoothConnectProgressDialog: ProgressDialog
    lateinit var mBluetoothSocket: BluetoothSocket
    var mBluetoothDevice: BluetoothDevice? = null
    var deviceName: String? = null

    var token = ""
    var username = ""
    var progressDialog : ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        token = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "").toString()
        See.log("token login :  $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        /*Setting toolbar*/
        setToolbar(this, "Checkout")
        /*Setting tabel bukti transaksi*/
        initTransaksi()
        /*Check printer yang tersimpan*/
        checkPairedDevice()

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
        btnShare.setOnClickListener {
            /*Fungsi untuk melakukan share*/
            shareWhatsApp()
        }

        btnSelesai.setOnClickListener {
            /*Fungsi untuk menyelesaikan pesanan*/
            MaterialAlertDialogBuilder(this)
                .setMessage("Selesaikan pesanan?")
                .setNegativeButton(
                    "Tidak"
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .setPositiveButton("Ya") { _, _ ->
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
                            .addBodyParameter("tanggal_transaksi",transaksi?.tanggalTrasaksi.toString().trim() )
                            .addBodyParameter("total_pembayaran",transaksi?.totalPembayaran?.toInt().toString().trim() )
                            .addBodyParameter("username", username.trim())
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

                                        See.toast(this@CheckoutActivity, "Upload Trx to Server $apiMessage")
                                        finish()
                                    } else {
                                        progressDialog!!.dismiss()
                                        See.toast(this@CheckoutActivity, "Upload Trx to Server $apiMessage")

                                    }

                                }

                                override fun onError(anError: ANError?) {
                                    progressDialog?.dismiss()
                                    See.log("onError errorCode trx : ${anError?.errorCode}")
                                    See.log("onError errorBody trx: ${anError?.errorBody}")
                                    See.log("onError errorDetail trx: ${anError?.errorDetail}")
                                }

                            })

                        transaksi.save()
//                        kurangiStok(transaksi.id);
                        finish()
                        startActivity(Intent(this, DashboardActivity::class.java))
                    }
                }.show()
        }


    }

    private fun kurangiStok(idTransaksi: Long) {
        val listItemTransaksi =
            SugarRecord.find(ItemTransaksi::class.java, "id_transaksi = ?", idTransaksi.toString())
        listItemTransaksi.forEach {
            val produk =
                SugarRecord.find(Produk::class.java, "id = ?", it.produkId.toString()).first()
            produk.stok = produk.stok?.minus(it.jumlah!!)
            produk.save()
            val stok = Stok(
                jumlah = produk.stok,
                isTambah = false,
                idProduk = it.produkId,
                tanggal = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
                    Date().time
                )
            )
            stok.save()
        }
    }

    private fun shareWhatsApp() {
        val pm: PackageManager = packageManager
        val bitmap = viewToImage(container)
        try {
            val bytes = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            val path: String = MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                "Transaksi",
                null
            )
            val imageUri: Uri = Uri.parse(path)
            val info: PackageInfo = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA)
            val waIntent = Intent(Intent.ACTION_SEND)
            waIntent.type = "image/*"
            waIntent.setPackage("com.whatsapp")
            waIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            waIntent.putExtra(Intent.EXTRA_TEXT, "Nota pembayaran")
            startActivity(Intent.createChooser(waIntent, "Share with"))
        } catch (e: java.lang.Exception) {
            Log.e("Error on sharing", "$e ")
            Toast.makeText(this, "Aplikasi tidak ter install!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initTransaksi() {
        val profile = SugarRecord.listAll(Profile::class.java).firstOrNull()
        if (profile != null) {
            tvNamaCafe.text = profile.namaToko
            tvAlamatCafe.text = profile.alamatToko
        }
        val transaksi = SugarRecord.find(Transaksi::class.java, "status = ?", "1").firstOrNull()
        if (transaksi != null) {
            val itemTransaksi = SugarRecord.find(
                ItemTransaksi::class.java,
                "id_transaksi = ?",
                transaksi.id.toString()
            )
            for (item in itemTransaksi) {
                val view =
                    LayoutInflater.from(this).inflate(R.layout.layout_table_row, table, false)
                view.tvNamaProduk.text = item.namaProduk
                view.tvHargaProduk.text = numberToCurrency(item.hargaProduk!!)
                view.tvJumlah.text = item.jumlah.toString()
                view.tvTotal.text = numberToCurrency(item.hargaProduk!! * item.jumlah!!)
                table.addView(view)
            }

            /*Tampilkan diskon jika ada*/
            if (transaksi.diskon!! != 0.0) {
                /*Show diskon*/
                textViewDisc.visibility = View.VISIBLE
                tvDiskon.visibility = View.VISIBLE
                view1.visibility = View.VISIBLE
                tvDiskon.text = numberToCurrency(transaksi.diskon!!)
            } else {
                /*Hide diskon*/
                textViewDisc.visibility = View.GONE
                tvDiskon.visibility = View.GONE
                view1.visibility = View.GONE
            }

            tvTanggal.text = transaksi.tanggalTrasaksi
            tvTotalBayar.text = numberToCurrency(transaksi.totalPembayaran!!)
            tvBayar.text = numberToCurrency(transaksi.bayar!!)
            tvKembalian.text =
                numberToCurrency(transaksi.bayar?.minus(transaksi.totalPembayaran!!)!!)
        } else {
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show()
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
                        "%1s \n %2s X %3s %4s",
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
                    "%1$-10s %2$-12s %3$-18s",
                    " ",
                    "Total",
                    numberToCurrency(transaksi.totalPembayaran!!)
                )
                printer.printString(str, BluetoothPrinterUtils.ALIGN_RIGHT)
                str = java.lang.String.format(
                    "%1$-10s %2$-12s %3$-18s",
                    " ",
                    "Bayar",
                    numberToCurrency(transaksi.bayar!!)
                )
                printer.printString(str, BluetoothPrinterUtils.ALIGN_RIGHT)
                str = java.lang.String.format(
                    "%1$-10s %2$-12s %3$-18s",
                    "",
                    "Kembalian",
                    numberToCurrency(transaksi.bayar!!.minus(transaksi.totalPembayaran!!))
                )
                printer.printString(str, BluetoothPrinterUtils.ALIGN_RIGHT)
                printer.printNewLines(2)
                printer.printText(transaksi.namaPembeli!!, BluetoothPrinterUtils.ALIGN_CENTER)
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
}
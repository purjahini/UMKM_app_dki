package com.svtech.dhuwit.Activities

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.svtech.dhuwit.AdapterOnline.RclvLaporanTodaysDetail
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOnline.ResponseTransaksiTodays
import kotlinx.android.synthetic.main.activity_detail_laporan_harian.*
import org.json.JSONObject
import java.util.*

class LaporanPenjualanActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""

    var from = ""
    var to = ""
    var now = Calendar.getInstance()

    var dayFrom: Int = 0
    var monthFrom: Int = 0
    var yearFrom: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_laporan_harian)
        setToolbar(this, "Laporan Penjualan Detail")

        token =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        See.log("token lap harian :  $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true
        setToolbar(this, "Laporan Penjualan Detail")

        TvReset.setOnClickListener {
            TvTanggalTo.text = ""
            to = ""
            TvTanggalFrom.text = ""
            from = ""
            now = Calendar.getInstance()
            rclvLapDetail.visibility = View.GONE
            tvEmptyMesage.visibility = View.VISIBLE
            tvEmptyMesage.text = "Maaf ..Data Transaksi Kosong."

        }

        TvTanggalFrom.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { view, year, monthOfYear, dayOfMonth ->
                    val calendar: Calendar = GregorianCalendar(year, monthOfYear, dayOfMonth)

                    from = DateFormat.format(
                        "yyyy-MM-dd",
                        calendar.timeInMillis
                    )
                        .toString()

                    dayFrom = dayOfMonth
                    monthFrom = monthOfYear
                    yearFrom = year

                    TvTanggalFrom.setText(from)

                    See.log("See date From $from")

                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.getDatePicker().setMaxDate(now.getTimeInMillis())
            datePickerDialog.show()

        }

        TvTanggalTo.setOnClickListener {
            if (from.isNotEmpty()) {
                val calendarFromMax: Calendar = GregorianCalendar(yearFrom, monthFrom, dayFrom)
                now.clear()
                calendarFromMax.set(Calendar.YEAR, yearFrom)
                calendarFromMax.set(Calendar.MONTH, monthFrom + 1)
                calendarFromMax.set(Calendar.DAY_OF_MONTH, dayFrom)

                val calendarFromMin: Calendar = GregorianCalendar(yearFrom, monthFrom, dayFrom)
                now.clear()
                calendarFromMin.set(Calendar.YEAR, yearFrom)
                calendarFromMin.set(Calendar.MONTH, monthFrom)
                calendarFromMin.set(Calendar.DAY_OF_MONTH, dayFrom)

                now.set(yearFrom, monthFrom, dayFrom)

                val Date = DatePickerDialog(
                    this,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        val calendar: Calendar = GregorianCalendar(year, monthOfYear, dayOfMonth)

                        to = DateFormat.format(
                            "yyyy-MM-dd",
                            calendar.timeInMillis
                        )
                            .toString()

                        TvTanggalTo.setText(to)

                        See.log("See log time to $to")

                    },

                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
                )
                Date.getDatePicker().setMinDate(calendarFromMin.timeInMillis)
                Date.getDatePicker().setMaxDate(calendarFromMax.timeInMillis)
                Date.show()


            } else {
                See.toast(this, "Silahkan Pilih tanggal From Terlebih dahulu")
            }

        }

        TvSubmit.setOnClickListener {

            return@setOnClickListener when {
                from.isEmpty() -> {
                    See.toast(this, "Tanggal Form tidak boleh kosong")
                }
                else -> {
                    LoadDataTransToday()
                }
            }

        }

    }

    private fun LoadDataTransToday() {
        progressDialog?.show()
        rclvLapDetail.visibility = View.VISIBLE
        AndroidNetworking.post(MyConstant.Urllaporandetail)
            .addHeaders(MyConstant.AUTHORIZATION, "Bearer$token")
            .addBodyParameter(MyConstant.STATUS, "0")
            .addBodyParameter(MyConstant.USERNAME, username.trim())
            .addBodyParameter(MyConstant.DATE_FROM, from.trim())
            .addBodyParameter(MyConstant.DATE_TO, to.trim())
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    See.log("respon get today detail : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        val data = Gson().fromJson(respon, ResponseTransaksiTodays::class.java)
                        val list = data.data
                        if (list != null) {


                            val transaksi =
                                Gson().fromJson(respon, ResponseTransaksiTodays::class.java).data
                            See.log("transaksi : $transaksi")


                            if (transaksi.isNotEmpty()) {
                                tvEmptyMesage.visibility = View.GONE

                                rclvLapDetail.apply {
                                    adapter = RclvLaporanTodaysDetail(
                                        this@LaporanPenjualanActivity, transaksi
                                    )
                                    layoutManager =
                                        LinearLayoutManager(this@LaporanPenjualanActivity)
                                    setHasFixedSize(true)
                                }
                            } else {
                                tvEmptyMesage.visibility = View.VISIBLE
                                tvEmptyMesage.text = apiMessage

                            }
                        }



                        progressDialog!!.dismiss()


                    } else {
                        progressDialog!!.dismiss()
                        See.toast(this@LaporanPenjualanActivity, "Response Server : $apiMessage")

                    }

                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    See.toast(
                        this@LaporanPenjualanActivity,
                        "Check your Connecting Internet And Error Code Lap Today ${anError?.errorCode}"
                    )
                    See.log("Error Code Lap Today errorCode trx : ${anError?.errorCode}")
                    See.log("Error Code Lap Today errorBody trx: ${anError?.errorBody}")
                    See.log("Error Code Lap Today errorDetail trx: ${anError?.errorDetail}")
                }

            })


    }

//    fun savePDF(transaksi: Transaksi) {
//        this.transaksi = transaksi
//        Dexter.withContext(this)
//            .withPermissions(
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                android.Manifest.permission.READ_EXTERNAL_STORAGE
//            )
//            .withListener(object : MultiplePermissionsListener {
//                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
//                    if (p0?.areAllPermissionsGranted()!!) {
//                        createPDF(transaksi)
//                    }
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    p0: MutableList<PermissionRequest>?,
//                    p1: PermissionToken?
//                ) {
//                    p1?.continuePermissionRequest()
//                }
//
//            })
//            .check()
//
//    }
//
//    fun createPDF(transaksi: Transaksi) {
//        val folder = File(Environment.getExternalStorageDirectory(), "Laporan Penjualan")
//        if (!folder.exists()) folder.mkdir()
//        val title =
//            transaksi!!.tanggalTrasaksi?.substring(0, transaksi!!.tanggalTrasaksi?.indexOf(" ")!!)
//                ?.replace("/", "_")
//        val date = SimpleDateFormat("dd/MM/yyyy mm:hh:ss").parse(transaksi!!.tanggalTrasaksi)
//        val fileName = "LAPORAN_PENJUALAN_$title.pdf"
//        val file = File(folder.absolutePath, fileName)
//        val doc = PdfUtils(file.absolutePath)
//        val tanggal =
//            transaksi!!.tanggalTrasaksi?.substring(0, transaksi!!.tanggalTrasaksi?.indexOf(" ")!!)
//        val iTransaksi = SugarRecord.listAll(Transaksi::class.java).filter { l ->
//            l.status == false && l.tanggalTrasaksi?.substring(
//                0,
//                l.tanggalTrasaksi!!.indexOf(" ")
//            ).equals(tanggal)
//        }
//        var totalPenjualan = 0.0
//        var totalProduk = 0
//        var listItemTransaksi = mutableListOf<ItemTransaksi>()
//
//        if (iTransaksi.isNotEmpty()) {
//            for (it in iTransaksi) {
//                totalPenjualan += it.totalPembayaran!!
//                val itemTransaksi = SugarRecord.find(
//                    ItemTransaksi::class.java,
//                    "id_transaksi = ?",
//                    it.id.toString()
//                )
//                if (itemTransaksi.isNotEmpty()) {
//                    for (item in itemTransaksi) {
//                        listItemTransaksi.add(item)
//                        totalProduk += item.jumlah!!
//                    }
//                }
//            }
//        }
//
//        doc.addParagraf("Laporan Penjualan", PdfUtils.fontTitle, PdfUtils.align_center)
//        doc.addNewEnter()
//        doc.addNewEnter()
//
//        var table = PdfPTable(2)
//        table.addCell(doc.createCell("Tanggal", PdfUtils.fontNormal, PdfUtils.no_border))
//        table.addCell(
//            doc.createCell(
//                ": " + SimpleDateFormat("dd MMMM yyyy").format(date).toString(),
//                PdfUtils.fontNormal,
//                PdfUtils.no_border
//            )
//        )
//
//        table.addCell(doc.createCell("Total Penjualan", PdfUtils.fontNormal, PdfUtils.no_border))
//        table.addCell(
//            doc.createCell(
//                ": $totalProduk Produk",
//                PdfUtils.fontNormal,
//                PdfUtils.no_border
//            )
//        )
//
//        table.addCell(doc.createCell("Total Pendapatan", PdfUtils.fontNormal, PdfUtils.no_border))
//        table.addCell(
//            doc.createCell(
//                ": " + numberToCurrency(totalPenjualan),
//                PdfUtils.fontNormal,
//                PdfUtils.no_border
//            )
//        )
//
//        table.addCell(
//            doc.createCell(
//                "List Produk Terjual",
//                PdfUtils.fontNormal,
//                PdfUtils.no_border
//            )
//        )
//        table.addCell(doc.createCell(":", PdfUtils.fontNormal, PdfUtils.no_border))
//        doc.addTable(table, floatArrayOf(120f, 120f), PdfUtils.align_left)
//
//        doc.addNewEnter()
//        table = PdfPTable(2)
//        table.addCell(doc.createCell("Nama Produk", PdfUtils.fontHeader, PdfUtils.no_border))
//        table.addCell(doc.createCell("Jumlah", PdfUtils.fontHeader, PdfUtils.no_border))
//
//        val itemDistint = listItemTransaksi.distinctBy { l -> l.produkId }
//        for (item in itemDistint) {
//            val itemCount = listItemTransaksi.filter { l -> l.produkId == item.produkId }
//            var jml = 0
//            itemCount.forEach { l -> jml += l.jumlah!! }
//            table.addCell(
//                doc.createCell(
//                    "${item.namaProduk}",
//                    PdfUtils.fontNormal,
//                    PdfUtils.no_border
//                )
//            )
//            table.addCell(doc.createCell("$jml", PdfUtils.fontNormal, PdfUtils.no_border))
//        }
//        doc.addTable(table, floatArrayOf(200f, 120f), PdfUtils.align_center)
//        doc.close()
//        val snackbar =
//            Snackbar.make(rclvPenjualan.rootView, "Laporan berhasil tersimpan!", Snackbar.LENGTH_INDEFINITE)
//        snackbar.setAction("Tampilkan", View.OnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.setType("application/pdf")
//            val list =
//                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
//            if (list.isNotEmpty()) {
//                val uri = FileProvider.getUriForFile(this, packageName + ".provider", file)
//                val intent = Intent(Intent.ACTION_VIEW)
//                intent.setDataAndType(uri, "application/pdf")
//                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                startActivity(intent)
//            } else {
//                Toast.makeText(this, "Tidak ada aplikasi untuk membuka file!", Toast.LENGTH_SHORT)
//                    .show()
//            }
//
//        }).show()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK && requestCode == MyConstant.REQUEST_OPEN_FILE) {
//            if (transaksi != null) {
//
////                Toast.makeText(this, "Laporan berhasil tersimpan di ${file.absolutePath}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
}
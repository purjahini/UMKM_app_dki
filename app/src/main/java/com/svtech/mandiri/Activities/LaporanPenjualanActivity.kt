package com.svtech.mandiri.Activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.itextpdf.text.pdf.PdfPTable
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.svtech.mandiri.AdapterOnline.RclvLaporanTodaysDetail
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.*
import com.svtech.mandiri.modelOnline.ResponseDetailCetak
import com.svtech.mandiri.modelOnline.ResponseTransaksiTodays
import kotlinx.android.synthetic.main.activity_detail_laporan_harian.*
import kotlinx.android.synthetic.main.activity_detail_laporan_harian.TvReset
import kotlinx.android.synthetic.main.activity_detail_laporan_harian.TvSubmit
import kotlinx.android.synthetic.main.activity_detail_laporan_harian.TvTanggalFrom
import kotlinx.android.synthetic.main.activity_detail_laporan_harian.TvTanggalTo
import kotlinx.android.synthetic.main.activity_detail_laporan_harian.btnExport
import kotlinx.android.synthetic.main.activity_detail_laporan_harian.tvEmptyMesage
import org.json.JSONObject
import java.io.File
import java.util.*

class LaporanPenjualanActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    var nama = ""

    var from = ""
    var to = ""
    var now = Calendar.getInstance()

    var namaToko = ""
    var alamatToko = ""
    var dateFrom = ""
    var dateTo = ""
    var dateNow = Calendar.getInstance()
    lateinit var lists: List<ResponseDetailCetak.Data>


    var dayFrom: Int = 0
    var monthFrom: Int = 0
    var yearFrom: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_laporan_harian)
        setToolbar(this, getString(R.string.lap_rekap_detail))

        token =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()

        namaToko = com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.NAMA_TOKO, "")
            .toString()
        nama = com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.NAMA, "").toString()
        alamatToko =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.ALAMAT_TOKO, "")
                .toString()
        See.log("token lap harian :  $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        TvReset.setOnClickListener {
            TvTanggalTo.text = ""
            to = ""
            TvTanggalFrom.text = ""
            from = ""
            now = Calendar.getInstance()
            rclvLapDetail.visibility = View.GONE
            btnExport.visibility = View.GONE
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

                    dateFrom = DateFormat.format(
                        "dd-MM-yyyy",
                        calendar.timeInMillis
                    ).toString()

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

                        dateTo = DateFormat.format("dd-MM-yyyy", calendar.timeInMillis).toString()

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
                                btnExport.visibility = View.VISIBLE
                                btnExport.setOnClickListener {
                                    savePDF()
                                }

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
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@LaporanPenjualanActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })


    }

    fun savePDF() {

        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {

                    if (p0?.areAllPermissionsGranted()!!) {
                        progressDialog?.show()
                        AndroidNetworking.post(MyConstant.Urllapdetailcetak)
                            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
                            .addBodyParameter(MyConstant.DATE_TO, to)
                            .addBodyParameter(MyConstant.DATE_FROM, from)
                            .addBodyParameter(MyConstant.USERNAME, username)
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(object : JSONObjectRequestListener {
                                override fun onResponse(response: JSONObject?) {
                                    progressDialog?.dismiss()
                                    val respon = response?.toString()
                                    See.log("response Urllapdetailcetak $respon ")
                                    val json = JSONObject(respon)
                                    val status = json.getInt(MyConstant.API_STATUS)
                                    val message = json.getString(MyConstant.API_MESSAGE)

                                    if (status.equals(1)){
                                        val list = Gson().fromJson(respon, ResponseDetailCetak::class.java).data

                                        if (list.isNotEmpty()) {
                                            lists = list

                                            createPDF()
                                        }





                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    progressDialog?.dismiss()
                                    val json = JSONObject(anError?.errorBody)
                                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                                    if (apiMessage != null) {
                                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                            getToken(this@LaporanPenjualanActivity)
                                        }
                                    }

                                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                                }

                            })


                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            })
            .check()

    }

    fun createPDF() {

        val dateNows = DateFormat.format(
            "dd-MM-yyyy",
            dateNow
        )
            .toString()

        val pointColumnWidths = floatArrayOf(175f, 175f, 175f)
        val folder =
            File(Environment.getExternalStorageDirectory(), getString(R.string.lap_rekap_detail))
        if (!folder.exists()) folder.mkdir()


        val fileName = "Lap_detail_jual_$dateNows.pdf"
        val file = File(folder.absolutePath, fileName)
        val doc = PdfUtils(file.absolutePath)

        doc.addParagraf("Laporan Detail Penjualan", PdfUtils.fontTitle, PdfUtils.align_center)
        doc.addParagraf("Toko : $namaToko", PdfUtils.fontNormal, PdfUtils.align_center)
        doc.addParagraf("Alamat : $alamatToko", PdfUtils.fontNormal, PdfUtils.align_center)
        doc.addNewEnter()
        doc.addNewEnter()

        doc.addParagraf(
            "Range Tanggal : $dateFrom s/d $dateTo",
            PdfUtils.fontNormal,
            PdfUtils.align_left
        )
        doc.addNewEnter()

        var table = PdfPTable(3)
        table.addCell(doc.createCell("Tanggal", PdfUtils.fontHeader, PdfUtils.no_border))
        table.addCell(doc.createCell("Jumlah Invoice", PdfUtils.fontHeader, PdfUtils.no_border))
        table.addCell(doc.createCell("Nominal (Rp.)", PdfUtils.fontHeader, PdfUtils.no_border))


        var totals = 0

        var trx = lists.size
        lists.forEach {

            table.addCell(doc.createCell(it.tanggal, PdfUtils.fontNormal, PdfUtils.no_border))
            table.addCell(doc.createCell(it.invoice, PdfUtils.fontNormal, PdfUtils.no_border))
            table.addCell(doc.createCell(it.nominal.toString(), PdfUtils.fontNormal, PdfUtils.no_border))
            totals+=it.nominal



        }
        table.addCell(doc.createCell("Jumlah", PdfUtils.fontHeader, PdfUtils.no_border))
        table.addCell(doc.createCell(trx.toString()+" Invoice", PdfUtils.fontHeader, PdfUtils.no_border))
        table.addCell(doc.createCell(numberToCurrency(totals), PdfUtils.fontHeader, PdfUtils.no_border))


        doc.addTable(table, pointColumnWidths, PdfUtils.align_center)

        doc.addNewEnter()
        doc.addParagraf("Tanggal Cetak : $dateNows", PdfUtils.fontNormal, PdfUtils.align_left)
        doc.addParagraf("Dicetak Oleh  : $nama", PdfUtils.fontNormal, PdfUtils.align_left)
        doc.close()
        val snackbar =
            Snackbar.make(
                rclvLapDetail.rootView,
                "Laporan berhasil tersimpan!",
                Snackbar.LENGTH_INDEFINITE
            )
        val view = snackbar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.topMargin = 75
        view.layoutParams = params

        snackbar.setAction("Tampilkan", View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setType("application/pdf")
            val list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.isNotEmpty()) {
                val uri = FileProvider.getUriForFile(this, packageName + ".provider", file)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/pdf")
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Tidak ada aplikasi untuk membuka file!", Toast.LENGTH_SHORT)
                    .show()
            }

        }).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MyConstant.REQUEST_OPEN_FILE) {
//            if (transaksi != null) {
//
////                Toast.makeText(this, "Laporan berhasil tersimpan di ${file.absolutePath}", Toast.LENGTH_LONG).show()
//            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
package com.svtech.dhuwit.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
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
import com.svtech.dhuwit.Adapter.RclvLaporanStok
import com.svtech.dhuwit.AdapterOnline.SpinnerAdapterCustom
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOnline.ItemOption
import com.svtech.dhuwit.modelOnline.ResponseLapStok
import kotlinx.android.synthetic.main.activity_laporan_harian.*
import kotlinx.android.synthetic.main.activity_laporan_stok.*
import kotlinx.android.synthetic.main.activity_laporan_stok.btnExport
import kotlinx.android.synthetic.main.activity_laporan_stok.rclvPenjualan
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LaporanStokActivity : AppCompatActivity() {
    var token = ""
    var username = ""
    var nama = ""

    var progressDialog: ProgressDialog? = null

    var kategoriId = ""

    var time = ""
    var dateNow = Calendar.getInstance()
    var namaToko = ""
    var alamatToko = ""


    var arrayList: ArrayList<ItemOption> = ArrayList()
    lateinit var lists : List<ResponseLapStok.Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_stok)
        setToolbar(this, "Laporan Stok")

        token =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        nama = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.NAMA, "").toString()
        username =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        See.log("token Kategori : $token")
        See.log("token addProduk : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        progressDialog?.show()
        arrayList.add(ItemOption("0","All Kategori"))

        AndroidNetworking.post(MyConstant.UrlKategoriGetData)
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER + token)
            .addBodyParameter("username", username)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    See.log("respon getKategori: $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        val jsonArray = response!!.getJSONArray("data")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val kategoriIdName = jsonObject.optString("id")
                            val kategoriName = jsonObject.optString("kategori_nama")

                            val aItmOpt = ItemOption(kategoriIdName, kategoriName)
                            arrayList.add(aItmOpt)



                        }

                    } else {
                        See.toast(
                            this@LaporanStokActivity,
                            "Check Koneksi Internet anda Code " + apiMessage
                        )
                    }

                    var adapter = SpinnerAdapterCustom(
                        this@LaporanStokActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        arrayList
                    )


                    spnKategoriProduk.adapter = adapter
                    spnKategoriProduk.setSelection(See.getIndex(arrayList,spnKategoriProduk,
                        kategoriId.toString()
                    ))




                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@LaporanStokActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

        spnKategoriProduk.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {


                val ItemOptionModel: ItemOption = parent.selectedItem as ItemOption
                See.log("itemOption id   ${ItemOptionModel.optId} ")
                See.log("itemOption label : ", ItemOptionModel.optLabel)
                kategoriId = ItemOptionModel.optId

                progressDialog?.show()
                AndroidNetworking.post(MyConstant.Urlstokgetdata)
                    .addHeaders(MyConstant.AUTHORIZATION,MyConstant.BEARER+token)
                    .addBodyParameter(MyConstant.USERNAME, username)
                    .addBodyParameter(MyConstant.KATEGORI, if(kategoriId.equals("0")) "" else kategoriId)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener{
                        override fun onResponse(response: JSONObject?) {
                            progressDialog?.dismiss()
                            val respon = response.toString()
                            See.log("response data stok  $respon")
                            val json = JSONObject(respon)
                            val apiStatus = json.getInt(MyConstant.API_STATUS)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if(apiStatus.equals(1)) {
                                tvEmpty.visibility = View.GONE
                                val data = Gson().fromJson(respon, ResponseLapStok::class.java)
                                val dataToko = data.data_toko
                                time = DateFormat.format(
                                    "dd-MM-yyyy HH:mm:ss",
                                    dateNow.timeInMillis
                                ).toString()
                                TvTanggalCetak.setText("Date Print : "+time)
                                dataToko.forEach {
                                    namaToko = it.nama_toko
                                    alamatToko = it.alamat_toko
                                    tvNamaToko.text = "Nama Toko : ${it.nama_toko}"
                                    tvAlamatToko.text = "Alamat Toko : ${it.alamat_toko}"

                                }
                                if (data.data.isEmpty()){
                                    tvEmpty.visibility = View.VISIBLE
                                    rclvPenjualan.visibility = View.GONE
                                    btnExport.visibility = View.GONE
                                } else {
                                    btnExport.visibility = View.VISIBLE
                                    rclvPenjualan.visibility = View.VISIBLE
                                    val listData = data.data.reversed()
                                    lists = data.data
                                    btnExport.setOnClickListener {
                                        savePDF()
                                    }

                                    rclvPenjualan.apply {
                                        adapter = RclvLaporanStok(this@LaporanStokActivity, listData as MutableList<ResponseLapStok.Data>)
                                        layoutManager = LinearLayoutManager(this@LaporanStokActivity)
                                        setHasFixedSize(true)
                                    }
                                }


                            }
                            else {
                                tvEmpty.visibility = View.VISIBLE
                            }
                        }

                        override fun onError(anError: ANError?) {
                            progressDialog?.dismiss()
                            See.log("response api server ${anError?.errorCode}")
                        }

                    })


            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

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
                        createPDF()
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



        val pointColumnWidths = floatArrayOf(50f,150f, 175f, 150f)
        val folder =
            File(Environment.getExternalStorageDirectory(), getString(R.string.lap_stoks))
        if (!folder.exists()) folder.mkdir()


        val fileName = "Lap_stok_$time.pdf"
        val file = File(folder.absolutePath, fileName)
        val doc = PdfUtils(file.absolutePath)

        doc.addParagraf("Laporan Stok", PdfUtils.fontTitle, PdfUtils.align_center)
        doc.addParagraf("Toko : $namaToko", PdfUtils.fontNormal, PdfUtils.align_center)
        doc.addParagraf("Alamat : $alamatToko", PdfUtils.fontNormal, PdfUtils.align_center)
        doc.addNewEnter()
        doc.addNewEnter()

        var table = PdfPTable(4)
        table.addCell(doc.createCell("NO", PdfUtils.fontHeader, PdfUtils.no_border))
        table.addCell(doc.createCell("Kategori", PdfUtils.fontHeader, PdfUtils.no_border))
        table.addCell(doc.createCell("Produk", PdfUtils.fontHeader, PdfUtils.no_border))
        table.addCell(doc.createCell("Stok Akhir", PdfUtils.fontHeader, PdfUtils.no_border))

        var no = 0
        lists.forEach {
            no+=1
            table.addCell(doc.createCell(no.toString(), PdfUtils.fontNormal, PdfUtils.no_border))
            table.addCell(doc.createCell(it.kategori_nama, PdfUtils.fontNormal, PdfUtils.no_border))
            table.addCell(doc.createCell(it.nama_produk, PdfUtils.fontNormal, PdfUtils.no_border))
            table.addCell(doc.createCell(it.stok.toString(), PdfUtils.fontNormal, PdfUtils.no_border))
        }


        doc.addTable(table, pointColumnWidths, PdfUtils.align_center)

        doc.addNewEnter()
        doc.addParagraf("Tanggal Cetak : $time", PdfUtils.fontNormal, PdfUtils.align_left)
        doc.addParagraf("Dicetak Oleh : $nama", PdfUtils.fontNormal, PdfUtils.align_left)
        doc.close()
        val snackbar =
            Snackbar.make(
                rclvPenjualan.rootView,
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
//            if (lists != null) {
//
//                Toast.makeText(this, "Laporan berhasil tersimpan di ${file.absolutePath}", Toast.LENGTH_LONG).show()
//            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
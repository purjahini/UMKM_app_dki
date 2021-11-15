package com.svtech.dhuwit.Activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.itextpdf.text.pdf.PdfPTable
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.orm.SugarRecord
import com.svtech.dhuwit.Adapter.RclvLaporanHarian
import com.svtech.dhuwit.Models.ItemTransaksi
import com.svtech.dhuwit.Models.Transaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.PdfUtils
import com.svtech.dhuwit.Utils.numberToCurrency
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_laporan_harian.*
import java.io.File
import java.text.SimpleDateFormat

class LaporanHarianActivity : AppCompatActivity() {
    var transaksi: Transaksi? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_harian)
        setToolbar(this, "Laporan Penjualan Harian")

        val transaksi = SugarRecord.listAll(Transaksi::class.java)
            .distinctBy { it.tanggalTrasaksi?.substring(0, it.tanggalTrasaksi?.indexOf(" ")!!) }
        val sorted = transaksi.sortedBy { l -> l.tanggalTrasaksi }.reversed()
        if (transaksi.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
            textView12.visibility = View.VISIBLE
            rclv.apply {
                adapter = RclvLaporanHarian(
                    this@LaporanHarianActivity, sorted as MutableList<Transaksi>
                )
                layoutManager = LinearLayoutManager(this@LaporanHarianActivity)
                setHasFixedSize(true)
            }
        } else {
            tvEmpty.visibility = View.VISIBLE
            textView12.visibility = View.GONE
        }

    }

    fun savePDF(transaksi: Transaksi) {
        this.transaksi = transaksi
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0?.areAllPermissionsGranted()!!) {
                        createPDF(transaksi)
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

    fun createPDF(transaksi: Transaksi) {
        val folder = File(Environment.getExternalStorageDirectory(), "Laporan Penjualan Harian")
        if (!folder.exists()) folder.mkdir()
        val title =
            transaksi!!.tanggalTrasaksi?.substring(0, transaksi!!.tanggalTrasaksi?.indexOf(" ")!!)
                ?.replace("/", "_")
        val date = SimpleDateFormat("dd/MM/yyyy mm:hh:ss").parse(transaksi!!.tanggalTrasaksi)
        val fileName = "LAPORAN_PENJUALAN_$title.pdf"
        val file = File(folder.absolutePath, fileName)
        val doc = PdfUtils(file.absolutePath)
        val tanggal =
            transaksi!!.tanggalTrasaksi?.substring(0, transaksi!!.tanggalTrasaksi?.indexOf(" ")!!)
        val iTransaksi = SugarRecord.listAll(Transaksi::class.java).filter { l ->
            l.status == false && l.tanggalTrasaksi?.substring(
                0,
                l.tanggalTrasaksi!!.indexOf(" ")
            ).equals(tanggal)
        }
        var totalPenjualan = 0.0
        var totalProduk = 0
        var listItemTransaksi = mutableListOf<ItemTransaksi>()

        if (iTransaksi.isNotEmpty()) {
            for (it in iTransaksi) {
                totalPenjualan += it.totalPembayaran!!
                val itemTransaksi = SugarRecord.find(
                    ItemTransaksi::class.java,
                    "id_transaksi = ?",
                    it.id.toString()
                )
                if (itemTransaksi.isNotEmpty()) {
                    for (item in itemTransaksi) {
                        listItemTransaksi.add(item)
                        totalProduk += item.jumlah!!
                    }
                }
            }
        }

        doc.addParagraf("Laporan Penjualan Harian", PdfUtils.fontTitle, PdfUtils.align_center)
        doc.addNewEnter()
        doc.addNewEnter()

        var table = PdfPTable(2)
        table.addCell(doc.createCell("Tanggal", PdfUtils.fontNormal, PdfUtils.no_border))
        table.addCell(
            doc.createCell(
                ": " + SimpleDateFormat("dd MMMM yyyy").format(date).toString(),
                PdfUtils.fontNormal,
                PdfUtils.no_border
            )
        )

        table.addCell(doc.createCell("Total Penjualan", PdfUtils.fontNormal, PdfUtils.no_border))
        table.addCell(
            doc.createCell(
                ": $totalProduk Produk",
                PdfUtils.fontNormal,
                PdfUtils.no_border
            )
        )

        table.addCell(doc.createCell("Total Pendapatan", PdfUtils.fontNormal, PdfUtils.no_border))
        table.addCell(
            doc.createCell(
                ": " + numberToCurrency(totalPenjualan),
                PdfUtils.fontNormal,
                PdfUtils.no_border
            )
        )

        table.addCell(
            doc.createCell(
                "List Produk Terjual",
                PdfUtils.fontNormal,
                PdfUtils.no_border
            )
        )
        table.addCell(doc.createCell(":", PdfUtils.fontNormal, PdfUtils.no_border))
        doc.addTable(table, floatArrayOf(120f, 120f), PdfUtils.align_left)

        doc.addNewEnter()
        table = PdfPTable(2)
        table.addCell(doc.createCell("Nama Produk", PdfUtils.fontHeader, PdfUtils.no_border))
        table.addCell(doc.createCell("Jumlah", PdfUtils.fontHeader, PdfUtils.no_border))

        val itemDistint = listItemTransaksi.distinctBy { l -> l.produkId }
        for (item in itemDistint) {
            val itemCount = listItemTransaksi.filter { l -> l.produkId == item.produkId }
            var jml = 0
            itemCount.forEach { l -> jml += l.jumlah!! }
            table.addCell(
                doc.createCell(
                    "${item.namaProduk}",
                    PdfUtils.fontNormal,
                    PdfUtils.no_border
                )
            )
            table.addCell(doc.createCell("$jml", PdfUtils.fontNormal, PdfUtils.no_border))
        }
        doc.addTable(table, floatArrayOf(200f, 120f), PdfUtils.align_center)
        doc.close()
        val snackbar =
            Snackbar.make(rclv.rootView, "Laporan berhasil tersimpan!", Snackbar.LENGTH_INDEFINITE)
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
            if (transaksi != null) {

//                Toast.makeText(this, "Laporan berhasil tersimpan di ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
package com.svtech.dhuwit.Activities

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.GridLayoutManager
import com.orm.SugarRecord
import com.svtech.dhuwit.Adapter.RclvProduk
import com.svtech.dhuwit.Models.Kategori
import com.svtech.dhuwit.Models.Produk
import com.svtech.dhuwit.Models.Stok
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.PathUtil
import com.svtech.dhuwit.Utils.calculateNoOfColumns
import com.svtech.dhuwit.Utils.hideSoftKeyboard
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_menu_pembelian.edtPencarian
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.btnAdd
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.rclv
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.tvEmpty
import kotlinx.android.synthetic.main.activity_menu_tambah_produk.*
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFPicture
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MenuTambahProdukActivity : AppCompatActivity() {
    private var isError: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_tambah_produk)
        /*Setting toolbar*/
        setToolbar(this, "Produk")
        /*Setting list item produk*/
        setToRecyclerView()

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddProdukActivity::class.java))
        }

        btnImport.setOnClickListener {
            importExcel()
            requestPermission()
        }

        /*Fungsi Pencarian*/
        edtPencarian.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                searchItem(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    /*Import data dari excel*/
    fun importExcel() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/*"
        startActivityForResult(intent, 1)
    }

    //result after get file from storage
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri = data?.data
            //get path
            val path = PathUtil(this).getPath(uri!!)
            Log.d("FILEPATH", path!!)
            readExcel(path)

            if (!isError) {
                Toast.makeText(this, "Upload Berhasil", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Upload Gagal", Toast.LENGTH_SHORT).show()
        }
    }

    fun readExcel(path: String?) {
        try {
            val inputFile = File(path)
            val inputStream = FileInputStream(inputFile)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            //read excel sheet
            val formulaEvaluator = workbook.creationHelper.createFormulaEvaluator()
            val list = ArrayList<String>()

            //get all image and it's positions
            val byteArrayOfImages = mutableMapOf<Int, ByteArray>()
            val dp = sheet.createDrawingPatriarch()
            val shapes = dp.shapes
            for (i in 0 until shapes.size) {
                val pict = shapes[i] as XSSFPicture
                val anchor = pict.clientAnchor
                val pictData = pict.pictureData
                val byteArray = pictData.data
                byteArrayOfImages[anchor.row1] = byteArray
            }

            //loop through rows
            val rowCount = sheet.lastRowNum
            for (r in 1 until rowCount) {
                val row = sheet.getRow(r)
                val cellCount = row.lastCellNum
                /*break jika row kosong*/
                if (cellCount < 0) break
                for (c in 0 until cellCount) {
                    /*Skip for image cell*/
                    if (c == 3) continue
                    val value = getCellAsString(row, c, formulaEvaluator).trim()
                    list.add(value)
                    Log.d("nilai : ", value)
                }
                //end of row
                var kategori =
                    SugarRecord.find(Kategori::class.java, "nama = ?", list[1]).firstOrNull()
                if (kategori == null) {
                    /*Buat kategori baru*/
                    val bitmap = ContextCompat.getDrawable(this, R.drawable.photo)?.toBitmap()
                    val stream = ByteArrayOutputStream()
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val byteArray = stream.toByteArray()
                    kategori = Kategori(
                        nama = list[1],
                        gambar = Base64.encodeToString(byteArray, Base64.DEFAULT)
                    )
                    kategori.save()
                }
                /*Insert produk*/
                val produk = Produk(
                    nama = list[0],
                    kategori = kategori,
                    harga = list[2].toDouble(),
                    foto = Base64.encodeToString(byteArrayOfImages[r], Base64.DEFAULT),
                    diskon = if (list[3] != "-") list[3].toDouble() else null,
                    minimalPembelian = if (list[4] != "-") list[4].toInt() else null,
                    stok = list[5].toInt(),
                    satuan = list[6]
                )
                Log.d("Satuan : ", list[6])
                produk.save()
                /*Update Stok*/
                val stok = Stok(
                    list[5].toInt(),
                    true,
                    produk.id,
                    SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
                        Date().time
                    )
                )
                stok.save()

                //reset list
                list.clear()
            }
            // change error status
            isError = false

        } catch (e: FileNotFoundException) {
            isError = true
            Toast.makeText(this, "File tidak ditemukan!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            isError = true
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getCellAsString(row: Row, c: Int, formulaEvaluator: FormulaEvaluator): String {
        var value = ""
        try {
            val cell = row.getCell(c)
            val cellValue = formulaEvaluator.evaluate(cell) ?: return "-"
            when (cellValue.cellType) {
                Cell.CELL_TYPE_BOOLEAN -> value = "" + cellValue.booleanValue
                Cell.CELL_TYPE_NUMERIC -> {
                    var myString: String = cellValue.numberValue.toInt().toString()
                    //IF IT'S A DATE
                    value = if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        val date = cellValue.numberValue
                        val formatter = SimpleDateFormat("MM/dd/yy")
                        formatter.format(HSSFDateUtil.getJavaDate(date))
                    } else {
                        "" + myString
                    }
                }
                Cell.CELL_TYPE_STRING -> value = cellValue.stringValue
                else -> {
                }
            }
        } catch (e: NullPointerException) {
            Log.e(ContentValues.TAG, "getCellAsString: NullPointerException: " + e.message)
        }
        return value
    }


    //request permission to access storage
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 2
            )
        }
    }


    fun setToRecyclerView(): Boolean {
        val listProduk = SugarRecord.listAll(Produk::class.java)
        if (listProduk.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
            val rclvadapter = RclvProduk(this, listProduk, false, false)
            rclv.apply {
                adapter = rclvadapter
                layoutManager = GridLayoutManager(context, calculateNoOfColumns(context, 180F))
                setHasFixedSize(true)
            }
        }
        return true
    }

    private fun searchItem(search: String) {
        val adapter = rclv.adapter
        if (adapter != null) {
            adapter as RclvProduk
            adapter.searchItem(search)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideSoftKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        setToRecyclerView()
    }

}
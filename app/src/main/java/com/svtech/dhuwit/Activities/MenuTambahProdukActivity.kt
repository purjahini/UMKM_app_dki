package com.svtech.dhuwit.Activities

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.svtech.dhuwit.AdapterOnline.RclvProdukOnline
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOnline.ProdukOnline
import kotlinx.android.synthetic.main.activity_menu_pembelian.edtPencarian
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.btnAdd
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.tvEmpty
import kotlinx.android.synthetic.main.activity_menu_tambah_produk.*
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.json.JSONObject
import java.text.SimpleDateFormat


class MenuTambahProdukActivity : AppCompatActivity() {
    private var isError: Boolean = false
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_tambah_produk)
        token =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        See.log("token addProduk : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

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
//            readExcel(path)

            if (!isError) {
                Toast.makeText(this, "Upload Berhasil", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Upload Gagal", Toast.LENGTH_SHORT).show()
        }
    }

//    fun readExcel(path: String?) {
//        try {
//            val inputFile = File(path)
//            val inputStream = FileInputStream(inputFile)
//            val workbook = XSSFWorkbook(inputStream)
//            val sheet = workbook.getSheetAt(0)
//
//            //read excel sheet
//            val formulaEvaluator = workbook.creationHelper.createFormulaEvaluator()
//            val list = ArrayList<String>()
//
//            //get all image and it's positions
//            val byteArrayOfImages = mutableMapOf<Int, ByteArray>()
//            val dp = sheet.createDrawingPatriarch()
//            val shapes = dp.shapes
//            for (i in 0 until shapes.size) {
//                val pict = shapes[i] as XSSFPicture
//                val anchor = pict.clientAnchor
//                val pictData = pict.pictureData
//                val byteArray = pictData.data
//                byteArrayOfImages[anchor.row1] = byteArray
//            }
//
//            //loop through rows
//            val rowCount = sheet.lastRowNum
//            for (r in 1 until rowCount) {
//                val row = sheet.getRow(r)
//                val cellCount = row.lastCellNum
//                /*break jika row kosong*/
//                if (cellCount < 0) break
//                for (c in 0 until cellCount) {
//                    /*Skip for image cell*/
//                    if (c == 3) continue
//                    val value = getCellAsString(row, c, formulaEvaluator).trim()
//                    list.add(value)
//                    Log.d("nilai : ", value)
//                }
//                //end of row
//                var kategori =
//                    SugarRecord.find(Kategori::class.java, "nama = ?", list[1]).firstOrNull()
//                if (kategori == null) {
//                    /*Buat kategori baru*/
//                    val bitmap = ContextCompat.getDrawable(this, R.drawable.photo)?.toBitmap()
//                    val stream = ByteArrayOutputStream()
//                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
//                    val byteArray = stream.toByteArray()
//                    kategori = Kategori(
//                        nama = list[1],
//                        gambar = Base64.encodeToString(byteArray, Base64.DEFAULT)
//                    )
//                    kategori.save()
//                }
//                /*Insert produk*/
//                val produk = Produk(
//                    nama = list[0],
//                    kategori = kategori.id,
//                    harga = list[2].toDouble(),
//                    foto = Base64.encodeToString(byteArrayOfImages[r], Base64.DEFAULT),
//                    diskon = if (list[3] != "-") list[3] else null,
//                    minimalPembelian = if (list[4] != "-") list[4].toInt() else null,
//                    stok = list[5].toInt(),
//                    satuan = list[6]
//                )
//                Log.d("Satuan : ", list[6])
//                produk.save()
//                /*Update Stok*/
//                val stok = Stok(
//                    list[5].toInt(),
//                    true,
//                    produk.id,
//                    SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
//                        Date().time
//                    )
//                )
//                stok.save()
//
//                //reset list
//                list.clear()
//            }
//            // change error status
//            isError = false
//
//        } catch (e: FileNotFoundException) {
//            isError = true
//            Toast.makeText(this, "File tidak ditemukan!", Toast.LENGTH_LONG).show()
//        } catch (e: Exception) {
//            isError = true
//            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
//        }
//    }

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
//        val listProduk = SugarRecord.listAll(Produk::class.java)
        progressDialog?.show()
        AndroidNetworking.post(MyConstant.Urlproduklistdata)
            .addHeaders("Authorization", "Bearer${token}")
            .addBodyParameter("username", username)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject( object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                   progressDialog?.dismiss()

                    if (response != null ) {
                        val respon = response?.toString()
                        See.log("respon getProduk: \n $respon")
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt(MyConstant.API_STATUS)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)

                        if (apiStatus.equals(1)) {
                            val data = Gson().fromJson(respon, ProdukOnline::class.java)

                            val list = data.data
                            if (list?.isEmpty() == true) {
                                tvEmpty.visibility = View.VISIBLE
                            } else {
                                tvEmpty.visibility = View.GONE
                                val rclvadapter = RclvProdukOnline(this@MenuTambahProdukActivity,list,false, false)
                                rclvProduk.apply {
                                    adapter = rclvadapter
                                    layoutManager = GridLayoutManager(context, 2)
                                    setHasFixedSize(true)
                                }
                            }
                        } else {
                            See.toast(this@MenuTambahProdukActivity, apiMessage)
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    See.toast(this@MenuTambahProdukActivity, anError?.errorBody.toString())
                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

        return true
    }

    private fun searchItem(search: String) {
        val adapter = rclvProduk.adapter
        if (adapter != null) {
            adapter as RclvProdukOnline
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
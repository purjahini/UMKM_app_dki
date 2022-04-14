package com.svtech.dhuwit.Activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.svtech.dhuwit.AdapterOnline.SpinnerAdapterCustom
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*

import kotlinx.android.synthetic.main.activity_produk_custome.*
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import com.svtech.dhuwit.modelOnline.ItemOption


lateinit var adapterSpinnerSatuan: ArrayAdapter<String>



class AddProdukActivity : AppCompatActivity() {

    var token = ""
    var username = ""
    var progressDialog: ProgressDialog? = null
    var file: File? = null
    var fileName = ""
    var kategoriId = ""


    var arrayList: ArrayList<ItemOption> = ArrayList()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.svtech.dhuwit.R.layout.activity_produk_custome)

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

        /*Setting tombol back dan title*/
        setToolbar(this, "Tambah Produk")
        val kategori = intent.getIntExtra("kategori", 0)
        progressDialog?.show()
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




//                        KategoriList.add(kategoriName)
//                        KategoriListId.add(kategoriId)
//                        KategoriAdapter = ArrayAdapter(
//                            this@AddProdukActivity,
//                            android.R.layout.simple_spinner_item,
//                            KategoriList)


                        }

                    } else {
                        See.toast(
                            this@AddProdukActivity,
                            "Check Koneksi Internet anda Code " + apiMessage
                        )
                    }

                  var adapter = SpinnerAdapterCustom(
                        this@AddProdukActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        arrayList
                    )


                    spnKategoriProduk.adapter = adapter
                    spnKategoriProduk.setSelection(See.getIndex(arrayList,spnKategoriProduk,
                        kategori.toString()
                    ))




                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@AddProdukActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })
        /*Setting adapter spinner*/


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

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        })

        val listSatuan = mutableListOf<String>()
        listSatuan.add("Pcs")
        listSatuan.add("Karton")
        listSatuan.add("Rtg")
        listSatuan.add("Pack")
        listSatuan.add("Qty")
        adapterSpinnerSatuan = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listSatuan
        )

        spnSatuan.adapter = adapterSpinnerSatuan

        btnLoadImage.setOnClickListener {
            /*Membuka galeri*/
//            pickImage(this, imgFoto,"Produk")

            var folder = File(Environment.getExternalStorageDirectory(), "UmkmImage")
            if (!folder.exists()) folder.mkdir()
            file = File(folder.absolutePath, "Kategori")
            See.log("file dir : ${file}")
            /*Membuka galeri*/
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0?.areAllPermissionsGranted()!!) {
                            ImagePicker.with(this@AddProdukActivity)
                                .galleryOnly()
                                .cropSquare()
                                .compress(1024)
                                .saveDir(file!!)
                                .maxResultSize(1080, 1080)
                                .start { resultCode, data ->


                                    if (resultCode == Activity.RESULT_OK) {
                                        Glide.with(this@AddProdukActivity).load(data?.data)
                                            .apply(RequestOptions.bitmapTransform(RoundedCorners(10F.toInt())))
                                            .into(imgFoto)
                                        fileName = File(data?.data?.path).name

                                        See.log(" nama files : $fileName")
                                    } else if (resultCode == ImagePicker.RESULT_ERROR) {
                                        Toast.makeText(
                                            this@AddProdukActivity,
                                            ImagePicker.getError(data),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        p1?.continuePermissionRequest()
                    }

                }).check()
        }


        val diskon = intent.getIntExtra("diskon", 0)
        val foto = intent.getStringExtra("foto")
        val harga = intent.getIntExtra("harga", 0)

        val minimal_pembelian = intent.getIntExtra("minimal_pembelian", 0)
        val nama = intent.getStringExtra("nama")
        val satuan = intent.getStringExtra("satuan")
        val stok = intent.getIntExtra("stok", 0)
        val update = intent.getBooleanExtra("update", false)
        val produkId = intent.getIntExtra("id", 0)



//        val produk = SugarRecord.findById(Produk::class.java, produkId)

        btnSimpanProduk.setOnClickListener {
            See.log("button klik simpan")
            val FilePath: String = "${file}/${fileName}"
            val NameFile = File(FilePath)
            val vSpn = spnKategoriProduk.selectedItem.toString()
            See.log("file upload : ${NameFile}  nilai sPnKt : ${vSpn}")

            AndroidNetworking.upload(MyConstant.Urlprodukcreate)
                .addHeaders("Authorization", "Bearer$token")
                .addMultipartFile("foto", NameFile)
                .addMultipartParameter("diskon", textInputDiskon.editText?.text.toString().trim())
                .addMultipartParameter(
                    "harga",
                    textInputHargaProduk.editText?.text.toString().trim()
                )
                .addMultipartParameter("kategori", kategoriId.trim())
                .addMultipartParameter(
                    "minimal_pembelian",
                    textInputMinimalPembelian.editText?.text.toString().trim()
                )
                .addMultipartParameter("nama", textInputNamaProduk.editText?.text.toString().trim())
                .addMultipartParameter("satuan", spnSatuan.selectedItem as String)
                .addMultipartParameter("stok", textInputStok.editText?.text.toString())
                .addMultipartParameter("username", username.trim())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        val respon = response?.toString()
                        See.log("respon insert produk : $respon")
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt(MyConstant.API_STATUS)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiStatus.equals(1)) {
                            progressDialog!!.dismiss()

                            See.toast(this@AddProdukActivity, "Upload produk to Server $apiMessage")
                            finish()
                        } else {
                            progressDialog!!.dismiss()
                            See.toast(this@AddProdukActivity, "Upload produk to Server $apiMessage")

                        }

                    }

                    override fun onError(anError: ANError?) {

                        progressDialog?.dismiss()
                        val json = JSONObject(anError?.errorBody)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiMessage != null) {
                            if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                getToken(this@AddProdukActivity)
                            }
                        }

                        See.log("onError getProduk errorCode : ${anError?.errorCode}")
                        See.log("onError getProduk errorBody : ${anError?.errorBody}")
                        See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                    }

                })


            /*Simpan dengan diskon*/
            if (cbDiskon.isChecked || update) {
                See.log("CbDiskon isCheck")
                if (checkInput(textInputNamaProduk) && checkInput(spnKategoriProduk)
                    && checkInput(textInputHargaProduk) && checkInput(spnSatuan) && checkInput(
                        textInputDiskon
                    )
                    && checkInput(textInputMinimalPembelian) && checkInput(textInputStok)
                ) {


//                    if (cbDiskon.isChecked){
//                        AndroidNetworking.upload(MyConstant.Urlprodukupdateid)
//                            .addHeaders("Authorization", "Bearer$token")
//                            .addMultipartFile("foto", NameFile)
//                            .addMultipartParameter("id", produkId.toString().trim())
//                            .addMultipartParameter("diskon", textInputDiskon.editText?.text.toString().trim())
//                            .addMultipartParameter(
//                                "harga",
//                                textInputHargaProduk.editText?.text.toString().trim()
//                            )
//                            .addMultipartParameter("kategori", kategoriId.trim())
//                            .addMultipartParameter(
//                                "minimal_pembelian",
//                                textInputMinimalPembelian.editText?.text.toString().trim()
//                            )
//                            .addMultipartParameter("nama", textInputNamaProduk.editText?.text.toString().trim())
//                            .addMultipartParameter("satuan", spnSatuan.selectedItem as String)
//                            .addMultipartParameter("stok", textInputStok.editText?.text.toString())
//                            .addMultipartParameter("username", username.trim())
//                            .setPriority(Priority.MEDIUM)
//                            .build()
//                            .getAsJSONObject(object : JSONObjectRequestListener {
//                                override fun onResponse(response: JSONObject?) {
//                                    val respon = response?.toString()
//                                    See.log("respon insert produk : $respon")
//                                    val json = JSONObject(respon)
//                                    val apiStatus = json.getInt(MyConstant.API_STATUS)
//                                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
//                                    if (apiStatus.equals(1)) {
//                                        progressDialog!!.dismiss()
//
//                                        See.toast(this@AddProdukActivity, "Upload produk to Server $apiMessage")
//                                        finish()
//                                    } else {
//                                        progressDialog!!.dismiss()
//                                        See.toast(this@AddProdukActivity, "Upload produk to Server $apiMessage")
//
//                                    }
//
//                                }
//
//                                override fun onError(anError: ANError?) {
//                                    progressDialog?.dismiss()
//                                    See.log("onError errorCode insertKategori : ${anError?.errorCode}")
//                                    See.log("onError errorBody insertKategori: ${anError?.errorBody}")
//                                    See.log("onError errorDetail insertKategori: ${anError?.errorDetail}")
//                                }
//
//                            })
//
//                    } else {
//                        AndroidNetworking.upload(MyConstant.Urlprodukupdateid)
//                            .addHeaders("Authorization", "Bearer$token")
//                            .addMultipartFile("foto", NameFile)
//                            .addMultipartParameter("id", produkId.toString().trim())
//                            .addMultipartParameter("diskon", textInputDiskon.editText?.text.toString().trim())
//                            .addMultipartParameter(
//                                "harga",
//                                textInputHargaProduk.editText?.text.toString().trim()
//                            )
//                            .addMultipartParameter("kategori", kategoriId.trim())
//                            .addMultipartParameter(
//                                "minimal_pembelian",
//                                textInputMinimalPembelian.editText?.text.toString().trim()
//                            )
//                            .addMultipartParameter("nama", textInputNamaProduk.editText?.text.toString().trim())
//                            .addMultipartParameter("satuan", spnSatuan.selectedItem as String)
//                            .addMultipartParameter("stok", textInputStok.editText?.text.toString())
//                            .addMultipartParameter("username", username.trim())
//                            .setPriority(Priority.MEDIUM)
//                            .build()
//                            .getAsJSONObject(object : JSONObjectRequestListener {
//                                override fun onResponse(response: JSONObject?) {
//                                    val respon = response?.toString()
//                                    See.log("respon insert produk : $respon")
//                                    val json = JSONObject(respon)
//                                    val apiStatus = json.getInt(MyConstant.API_STATUS)
//                                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
//                                    if (apiStatus.equals(1)) {
//                                        progressDialog!!.dismiss()
//
//                                        See.toast(this@AddProdukActivity, "Upload produk update to Server $apiMessage")
//                                        finish()
//                                    } else {
//                                        progressDialog!!.dismiss()
//                                        See.toast(this@AddProdukActivity, "Upload produk update to Server $apiMessage")
//
//                                    }
//
//                                }
//
//                                override fun onError(anError: ANError?) {
//                                    progressDialog?.dismiss()
//                                    See.log("onError errorCode insertKategori : ${anError?.errorCode}")
//                                    See.log("onError errorBody insertKategori: ${anError?.errorBody}")
//                                    See.log("onError errorDetail insertKategori: ${anError?.errorDetail}")
//                                }
//
//                            })
//
//                    }


                    See.log("update = ${update} isCheck")
                        /*Update produk*/
                        updateProduk(produkId)


                        /*Insert produk*/
//                        insertProduk()


//                    Toast.makeText(this, "Produk berhasil disimpan!", Toast.LENGTH_SHORT).show()
//                    finish()
                }
            } else {
                /*Simpan tanpa diskon*/
                if (checkInput(textInputNamaProduk) && checkInput(spnKategoriProduk)
                    && checkInput(textInputHargaProduk) && checkInput(textInputStok) && checkInput(
                        spnSatuan
                    )
                ) {

                    if (cbDiskon.isChecked) {
                    } else {
                        /*insert tanpa diskon*/

                        AndroidNetworking.upload(MyConstant.Urlprodukcreate)
                            .addHeaders("Authorization", "Bearer$token")
                            .addMultipartFile("foto", NameFile)
                            .addMultipartParameter("diskon", null)
                            .addMultipartParameter(
                                "harga",
                                textInputHargaProduk.editText?.text.toString().trim()
                            )
                            .addMultipartParameter("kategori", kategoriId.trim())
                            .addMultipartParameter("minimal_pembelian", null)
                            .addMultipartParameter("nama", textInputNamaProduk.editText?.text.toString().trim())
                            .addMultipartParameter("satuan", spnSatuan.selectedItem as String)
                            .addMultipartParameter("stok", textInputStok.editText?.text.toString())
                            .addMultipartParameter("username", username.trim())
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(object : JSONObjectRequestListener {
                                override fun onResponse(response: JSONObject?) {
                                    val respon = response?.toString()
                                    See.log("respon insert produk : $respon")
                                    val json = JSONObject(respon)
                                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                                    if (apiStatus.equals(1)) {
                                        progressDialog!!.dismiss()

                                        See.toast(this@AddProdukActivity, "Upload produk to Server $apiMessage")
                                        finish()
                                    } else {
                                        progressDialog!!.dismiss()
                                        See.toast(this@AddProdukActivity, "Upload produk to Server $apiMessage")

                                    }

                                }

                                override fun onError(anError: ANError?) {

                                    progressDialog?.dismiss()
                                    val json = JSONObject(anError?.errorBody)
                                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                                    if (apiMessage != null) {
                                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                            getToken(this@AddProdukActivity)
                                        }
                                    }

                                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                                }

                            })


                    }



                    /*Insert produk*/
                        insertProduk()
                    See.log("insert Produk")

//                    Toast.makeText(this, "Produk berhasil disimpan!", Toast.LENGTH_SHORT).show()
//                    finish()
                }
            }

        }

        if (produkId != 0) {
            Glide.with(this).load(foto).fitCenter()
                .placeholder(R.drawable.logo)
                .into(imgFoto)
            textInputNamaProduk.editText?.setText(nama)
            textInputHargaProduk.editText?.setText(harga?.toInt().toString())
//            spnKategoriProduk.selectedItem = adapterKategori.getView(kategori)
            spnKategoriProduk.setSelection(See.getIndex(arrayList,spnKategoriProduk,kategori.toString()))
            textInputStok.editText?.setText(stok.toString())
            spnSatuan.selection = adapterSpinnerSatuan.getPosition(satuan)
            if (diskon != null) {
                showDiskonInput()
                cbDiskon.isChecked = true
                textInputDiskon.editText?.setText(diskon.toString())
                textInputMinimalPembelian.editText?.setText(minimal_pembelian.toString())
            }
        }

        /*CheckBox Diskon Listener*/
        cbDiskon.setOnCheckedChangeListener { _, isChecked ->
            /*Tampilkan diskon input*/
            if (isChecked) showDiskonInput()
            /*Sembunyikan diskon input*/
            else hideDiskonInput()
        }
    }



    fun insertProduk(): Boolean {



        /*insert dengan diskon*/
//        produk.save()
        /*Update Stok*/
//        val stok = Stok(
//            jumlah = produk.stok,
//            isTambah = true,
//            idProduk = produk.id,
//            tanggal = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
//                Date().time
//            )
//        )
//        stok.save()
        return true
    }


    fun updateProduk(produkId: Int): Boolean {




//
//        val byteArray = ImageViewToByteArray(imgFoto)
//        val tempStok = produk.stok
//        produk.nama = textInputNamaProduk.editText?.text.toString()
//        produk.harga = textInputHargaProduk.editText?.text.toString().toDouble()
//        produk.foto = Base64.encodeToString(byteArray, Base64.DEFAULT)
////        produk.kategori = spnKategori.selectedItem as Kategori
//        produk.stok = textInputStok.editText?.text.toString().toInt()
//        produk.satuan = spnSatuan.selectedItem as String
//        /*jika ada diskon*/
//        if (cbDiskon.isChecked) {
////            produk.diskon = textInputDiskon.editText?.text.toString().toDouble()
//            produk.minimal_pembelian = textInputMinimalPembelian.editText?.text.toString().toInt()
//        } else {
//            produk.diskon = null
//            produk.minimal_pembelian = null
//        }
////        produk.save()
//
//        /*Update Stok*/
//        val stok: Stok
//        if (produk.stok!! > tempStok!!) {
//            stok = Stok(
//                jumlah = produk.stok,
//                isTambah = true,
//                idProduk = produk.id,
//                tanggal = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
//                    Date().time
//                )
//            )
//        } else {
//            stok = Stok(
//                jumlah = produk.stok,
//                isTambah = false,
//                idProduk = produk.id,
//                tanggal = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
//                    Date().time
//                )
//            )
//        }
//        stok.save()
        return true
    }



    fun showDiskonInput() {
        textInputDiskon.visibility = View.VISIBLE
        textInputMinimalPembelian.visibility = View.VISIBLE
        /*Change focus to diskon input*/
        textInputDiskon.requestFocus()
    }

    fun hideDiskonInput() {
        textInputDiskon.visibility = View.GONE
        textInputMinimalPembelian.visibility = View.GONE
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideSoftKeyboard()
        }
        return super.onTouchEvent(event)
    }
}


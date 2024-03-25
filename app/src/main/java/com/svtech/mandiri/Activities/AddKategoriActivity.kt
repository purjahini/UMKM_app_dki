package com.svtech.mandiri.Activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
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
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.*
import kotlinx.android.synthetic.main.activity_add_kategori.*
import org.json.JSONObject
import java.io.File
import java.util.*

class AddKategoriActivity : AppCompatActivity() {
    var token = ""
    var username = ""
    var progressDialog: ProgressDialog? = null
    var data = ""
    var file: File? = null
    var fileName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_kategori)
        /*Setting tombol back dan title*/
        setToolbar(this, "Tambah Kategori")
        token =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        See.log("token addProduk : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        val update = intent.getBooleanExtra("update", false)
        val kategoriNama = intent.getStringExtra("kategori_nama")
        val kategoriGambar = intent.getStringExtra("kategori_gambar")
        val kategoriId = intent.getIntExtra("kategori_id", 0)
        textInputNamaKategori.editText?.setText(kategoriNama)

        See.log("$kategoriId,  $kategoriNama, $kategoriGambar, $update")

        btnLoadImage.setOnClickListener {

//           var nameImage =  pickImage(this, imgFoto, "Kategori")
//            See.log("name image : ${nameImage}")
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
                            ImagePicker.with(this@AddKategoriActivity)
                                .galleryOnly()
                                .cropSquare()
                                .compress(1024)
                                .saveDir(file!!)
                                .maxResultSize(1080, 1080)
                                .start { resultCode, data ->


                                    if (resultCode == Activity.RESULT_OK) {
                                        Glide.with(this@AddKategoriActivity).load(data?.data)
                                            .apply(RequestOptions.bitmapTransform(RoundedCorners(10F.toInt())))
                                            .into(imgFoto)
                                        fileName = File(data?.data?.path).name

                                        See.log(" nama files : $fileName")
                                    } else if (resultCode == ImagePicker.RESULT_ERROR) {
                                        Toast.makeText(
                                            this@AddKategoriActivity,
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

            return@setOnClickListener
        }



        btnSimpan.setOnClickListener {
            if (checkInput(textInputNamaKategori)) {
                if (update) {
                    /*Update kategori*/
                    updateKategori(kategoriId)
                } else {
                    /*Insert kategori*/
                    insertServerKategori()

                }
//                Toast.makeText(this, "Kategori berhasil disimpan!", Toast.LENGTH_SHORT).show()
//                finish()
            }

        }
        if (kategoriNama != null) {
            Glide.with(this).load(kategoriGambar).fitCenter()
                .into(imgFoto)
            textInputNamaKategori.editText?.setText(kategoriNama)
        }

    }

    fun insertServerKategori() {
        progressDialog!!.show()

        val FilePath: String = "${file}/${fileName}"
        val NameFile = File(FilePath)
        See.log("file upload : ${NameFile}")

        AndroidNetworking.upload(MyConstant.Urlkategoricreate)
            .addHeaders("Authorization", "Bearer$token")
            .addMultipartFile("kategori_gambar",NameFile)
            .addMultipartParameter("kategori_nama", textInputNamaKategori.editText?.text.toString().trim())
            .addMultipartParameter("username", username.trim())
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    See.log("respon insertKategori : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        progressDialog!!.dismiss()

                        See.toast(this@AddKategoriActivity, "Upload Kategori to Server $apiMessage")
                        finish()
                    } else {
                        progressDialog!!.dismiss()
                        See.toast(this@AddKategoriActivity, "Upload Kategori to Server $apiMessage")

                    }

                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@AddKategoriActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })


    }

    fun updateKategori(id : Int?): Boolean {
        progressDialog!!.show()
        val FilePath = "${file}/${fileName}"
        val NameFile = File(FilePath)
        See.log("file upload : ${NameFile}")
        if (fileName.isNullOrEmpty()){
            AndroidNetworking.post(MyConstant.Urlkategoriupdate)
                .addHeaders("Authorization", "Bearer$token")
                .addBodyParameter("id", id.toString().trim())
                .addBodyParameter("kategori_nama", textInputNamaKategori.editText?.text.toString().trim())
                .addBodyParameter("username", username.trim())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        val respon = response?.toString()
                        See.log("respon insertKategori : $respon")
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt(MyConstant.API_STATUS)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiStatus.equals(1)) {
                            progressDialog!!.dismiss()
//                        insertLocalKategori()
                            See.toast(this@AddKategoriActivity, "Update Kategori no change image to Server $apiMessage")
                            finish()
                        } else {
                            progressDialog!!.dismiss()
                            See.toast(this@AddKategoriActivity, "Nama Label Kategori sudah ada")

                        }

                    }

                    override fun onError(anError: ANError?) {

                        progressDialog?.dismiss()
                        val json = JSONObject(anError?.errorBody)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiMessage != null) {
                            if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                getToken(this@AddKategoriActivity)
                            }
                        }

                        See.log("onError getProduk errorCode : ${anError?.errorCode}")
                        See.log("onError getProduk errorBody : ${anError?.errorBody}")
                        See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                    }

                })

        } else {
            AndroidNetworking.upload(MyConstant.Urlkategoriupdate)
                .addHeaders("Authorization", "Bearer$token")
                .addMultipartParameter("id", id.toString().trim())
                .addMultipartFile("kategori_gambar", NameFile)
                .addMultipartParameter("kategori_nama", textInputNamaKategori.editText?.text.toString().trim())
                .addMultipartParameter("username", username.trim())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        val respon = response?.toString()
                        See.log("respon insertKategori : $respon")
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt(MyConstant.API_STATUS)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiStatus.equals(1)) {
                            progressDialog!!.dismiss()

                            See.toast(this@AddKategoriActivity, apiMessage)
                            finish()
                        } else {
                            progressDialog!!.dismiss()
                            See.toast(this@AddKategoriActivity, apiMessage)

                        }

                    }

                    override fun onError(anError: ANError?) {

                        progressDialog?.dismiss()
                        val json = JSONObject(anError?.errorBody)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiMessage != null) {
                            if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                getToken(this@AddKategoriActivity)
                            }
                        }

                        See.log("onError getProduk errorCode : ${anError?.errorCode}")
                        See.log("onError getProduk errorBody : ${anError?.errorBody}")
                        See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                    }

                })

        }




        return true
    }
}
package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.*
import kotlinx.android.synthetic.main.activity_add_kategori.*
import org.json.JSONObject
import java.io.File

class AddKategoriActivity : AppCompatActivity(),ImagePickerCallback {
    var token = ""
    var username = ""
    var progressDialog: ProgressDialog? = null
    var data = ""
    var file: File? = null
    var fileName = ""

    private lateinit var imagePickerHelper: ImagePickerHelper
    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_kategori)
        /*Setting tombol back dan title*/
        setToolbar(this, "Tambah Kategori")

        imagePickerHelper = ImagePickerHelper(this,this)
        imageView = findViewById(R.id.imgFoto)
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

            imagePickerHelper.showImagePickerDialog()

        }



        btnSimpan.setOnClickListener {
            selectedImageUri.let { uri ->



            if (checkInput(textInputNamaKategori)) {
                if (update) {
                    /*Update kategori*/
                    updateKategori(kategoriId,uri)
                } else {
                    /*Insert kategori*/
                    insertServerKategori()

                }
//                Toast.makeText(this, "Kategori berhasil disimpan!", Toast.LENGTH_SHORT).show()
//                finish()
            }

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

        val NameFile =  File(selectedImageUri?.let { getRealPathFromURI(it) })
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

    fun updateKategori(id: Int?, uri: Uri?): Boolean {
        progressDialog!!.show()
//        val FilePath = "${file}/${fileName}"
        val NameFile = File(selectedImageUri?.let { getRealPathFromURI(it) })
//        See.log("file upload : ${NameFile}")

        if (uri.toString().isNullOrEmpty()){
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

    override fun onImagePicked(imageUri: Uri) {
        selectedImageUri = imageUri
        imageView.setImageURI(imageUri)
    }

    private fun getRealPathFromURI(uri: Uri): String {
        var result = ""
        contentResolver.query(uri, null, null, null, null)?.apply {
            moveToFirst()
            val index = getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = getString(index)
            close()
        }
        return result
    }
}
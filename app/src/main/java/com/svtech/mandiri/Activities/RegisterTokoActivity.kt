package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.ImagePickerCallback
import com.svtech.mandiri.Utils.ImagePickerHelper
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.getPreferences
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.savePreferences
import com.svtech.mandiri.Utils.setToolbar
import com.svtech.mandiri.modelOnline.ProfileOnline
import org.json.JSONObject
import java.io.File
import java.util.UUID

class RegisterTokoActivity : AppCompatActivity(), ImagePickerCallback {
    private var progressDialog: ProgressDialog? = null
    private var token = ""
    private var username = ""

    private lateinit var btnUploadFoto: Button
    private lateinit var etNamaToko: EditText
    private lateinit var etAlamatToko: EditText
    private lateinit var btnSimpan: Button
    private lateinit var imagePickerHelper: ImagePickerHelper
    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null

    var id = ""
    var kode = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_toko)
        val update = intent.getBooleanExtra("update", false)
        token = getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username = getPreferences(this).getString(MyConstant.CURRENT_USER, "").toString()

        btnUploadFoto = findViewById(R.id.btnUploadFoto)
        etNamaToko = findViewById(R.id.etNamaToko)
        etAlamatToko = findViewById(R.id.etAlamatToko)
        btnSimpan = findViewById(R.id.btnSimpan)
        imageView = findViewById(R.id.ivFotoToko)

        imagePickerHelper = ImagePickerHelper(this, this)
        if (update) {
            setToolbar(this, "Update Toko")
            kode = getPreferences(this).getString(MyConstant.kode, "").toString()
            btnSimpan.setText("SIMPAN UPDATE")
            loadDataToko(username, token, kode)
        } else {
            setToolbar(this, "Daftar Toko")
        }





        progressDialog = ProgressDialog(this).apply {
            setTitle("Proses")
            setMessage("Mohon Menunggu...")
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(false)
            isIndeterminate = true
        }



        btnUploadFoto.setOnClickListener {
            imagePickerHelper.showImagePickerDialog()
        }

        btnSimpan.setOnClickListener {
            if (update) {
                updateData()

            } else {
                if (validateInput()) {
                    uploadData()
                }
            }

        }
    }

    private fun loadDataToko(username: String, token: String, random: String?) {
        progressDialog?.show()
        AndroidNetworking.post(MyConstant.Urlgetdatatoko)
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER + token)
            .addBodyParameter(MyConstant.kode, random)
            .addBodyParameter(MyConstant.USERNAME, username)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    See.log("respon getLoginUser: \n $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus == 1) {
                        val data = Gson().fromJson(respon, ProfileOnline::class.java)
                        val list = data.data

                        if (list != null) {
                            id = list.id.toString()
                            etNamaToko.setText(list.nama_toko)
                            etAlamatToko.setText(list.alamat_toko)
                            Glide.with(this@RegisterTokoActivity)
                                .load(list.logo_toko)
                                .into(imageView)


                        }

                    } else {
                        if (apiMessage != null) {
                            See.toast(this@RegisterTokoActivity, apiMessage)
                        }

                    }

                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage == MyConstant.FORBIDDEN) {
                        getToken(this@RegisterTokoActivity)
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

    }

    private fun updateData() {
        progressDialog?.show()

        // Cek apakah gambar diubah
        val NameFile: File? = selectedImageUri?.let { uri ->
            val filePath = getRealPathFromURI(uri)
            if (!filePath.isNullOrEmpty()) File(filePath) else null
        }

        val request = AndroidNetworking.upload(MyConstant.Urlupdatetoko)
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER + token)
            .addMultipartParameter(MyConstant.ID, id)
            .addMultipartParameter(MyConstant.kode, kode)
            .addMultipartParameter(MyConstant.USERNAME, username)
            .addMultipartParameter(MyConstant.ALAMAT_TOKO, etAlamatToko.text.toString().trim())
            .addMultipartParameter(MyConstant.NAMA_TOKO, etNamaToko.text.toString().trim())
            .setPriority(Priority.MEDIUM)

        // Jika file ada dan valid, tambahkan ke request
        if (NameFile != null && NameFile.exists() && NameFile.length() > 0) {
            request.addMultipartFile(MyConstant.LOGO_TOKO, NameFile)
        }

        request.build().getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject?) {
                progressDialog?.dismiss()
                response?.let {
                    val apiStatus = it.getInt(MyConstant.API_STATUS)
                    val apiMessage = it.getString(MyConstant.API_MESSAGE)
                    showSnackbar(apiMessage)
                    if (apiStatus == 1) {
                        finish()
                    }
                }
            }

            override fun onError(anError: ANError?) {
                progressDialog?.dismiss()
                anError?.errorBody?.let {
                    val json = JSONObject(it)
                    val apiMessage = json.optString(MyConstant.API_MESSAGE, "Terjadi kesalahan")
                    if (apiMessage == MyConstant.FORBIDDEN) {
                        getToken(this@RegisterTokoActivity)
                    }
                    showSnackbar(apiMessage)
                }
            }
        })
    }


    private fun uploadData() {
        val randomUUID = UUID.randomUUID().toString()
        progressDialog?.show()

        val NameFile = File(selectedImageUri?.let { getRealPathFromURI(it) })
        See.log("file upload : ${NameFile}")

        AndroidNetworking.upload(MyConstant.urlToko)
            .addHeaders("Authorization", "Bearer $token")  // Tambahkan spasi setelah "Bearer"
            .addMultipartFile("logo_toko", NameFile) // Kirim file asli
            .addMultipartParameter("nama_toko", etNamaToko.text.toString().trim())
            .addMultipartParameter("alamat_toko", etAlamatToko.text.toString().trim())
            .addMultipartParameter("username", username)
            .addMultipartParameter("kode", randomUUID.trim())
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    response?.let {
                        val apiStatus = it.getInt(MyConstant.API_STATUS)
                        val apiMessage = it.getString(MyConstant.API_MESSAGE)
                        if (apiStatus == 1) {
                            savePreferences(this@RegisterTokoActivity, MyConstant.kode, randomUUID)
                            startActivity(Intent(applicationContext, DashboardActivity::class.java))
                            finish()
                        } else {
                            showSnackbar(apiMessage)
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    anError?.errorBody?.let {
                        val json = JSONObject(it)
                        val apiMessage = json.optString(MyConstant.API_MESSAGE, "Terjadi kesalahan")
                        if (apiMessage == MyConstant.FORBIDDEN) {
                            getToken(this@RegisterTokoActivity)
                        }
                        showSnackbar(apiMessage)
                    }
                }
            })
    }

    private fun validateInput(): Boolean {
        if (etNamaToko.text.toString().isEmpty()) {
            etNamaToko.error = "Nama toko harus diisi"
            return false
        }
        if (etAlamatToko.text.toString().isEmpty()) {
            etAlamatToko.error = "Alamat toko harus diisi"
            return false
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Silakan pilih foto toko", Toast.LENGTH_SHORT).show()
            return false
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

    private fun showSnackbar(message: String) {
        val snackbar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
        snackbar.view.setBackgroundColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.primary
            )
        )
        snackbar.show()
    }
}

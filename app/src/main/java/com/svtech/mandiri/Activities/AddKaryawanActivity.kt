package com.svtech.mandiri.Activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.svtech.mandiri.Models.User
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.*
import kotlinx.android.synthetic.main.activity_add_karyawan.*
import org.json.JSONObject
import java.io.File


class AddKaryawanActivity : AppCompatActivity(),ImagePickerCallback {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    var file: File? = null
    var fileName = ""

    private lateinit var imagePickerHelper: ImagePickerHelper
    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_karyawan)
        imagePickerHelper = ImagePickerHelper(this,this)
        imageView = findViewById(R.id.imgFoto)

        token =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        See.log("token add Karyawan :  $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        /*Setting tombol back dan title*/
        setToolbar(this, "Tambah Pegawai")

        btnLoadImage.setOnClickListener {
           imagePickerHelper.showImagePickerDialog()
        }

        val update = intent.getBooleanExtra("update", false)
        val idUser = intent.getIntExtra(MyConstant.ID, 0)
        val userkontak = intent.getStringExtra(MyConstant.KONTAK)
        val nama = intent.getStringExtra(MyConstant.NAMA)
        val foto = intent.getStringExtra(MyConstant.FOTO)

        if (update) {
            labelwarning.visibility = View.VISIBLE
        }


        btnSimpan.setOnClickListener {
            selectedImageUri.let { uri ->
                if (update) {

                    if ( checkInput(
                            textInputNamaPegawai
                        ) && checkInputUsername(
                            textInputUsernameAdmin
                        )
                    ) {

                        /*Update karyawan*/
                        updateKaryawan(idUser,uri)

                    } else {
                        Toast.makeText(this, "nama / username tidak boleh kosong", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                } else {
                    if (textInputPasswordAdmin.editText?.text.toString().length == 6 && checkInput(
                            textInputNamaPegawai
                        ) && checkInputUsername(
                            textInputUsernameAdmin
                        ) && checkInputPassword(textInputPasswordAdmin)
                    ) {

                        /*Insert karyawan*/
                        insertKaryawan(uri)

                    } else {
                        Toast.makeText(this, "Password Harus 6 Karakter", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                }
            }



        }
        if (idUser != null) {
            Glide.with(this).load(foto).fitCenter()
                .placeholder(R.drawable.logo1)
                .into(imgFoto)
            textInputNamaPegawai.editText?.setText(nama)
            textInputUsernameAdmin.editText?.setText(userkontak)
//            textInputPasswordAdmin.editText?.setText(pass)
        }
    }

    private fun insertKaryawan(uri: Uri?) {
        progressDialog?.show()

        val NameFile =  File(selectedImageUri?.let { getRealPathFromURI(it) })


        when {
            uri.toString().isEmpty() -> {

                AndroidNetworking.upload(MyConstant.Urlpegawaicreate)
                    .addHeaders("Authorization", "Bearer$token")
                    .addMultipartParameter("kontak",textInputUsernameAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("nama",textInputNamaPegawai.editText?.text.toString().trim())
                    .addMultipartParameter("password",textInputPasswordAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("role", User.userAdmin.trim())
                    .addMultipartParameter("username", username.trim())
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            val respon = response?.toString()
                            See.log("respon add karyawan : $respon")
                            val json = JSONObject(respon)
                            val apiStatus = json.getInt(MyConstant.API_STATUS)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiStatus.equals(1)) {
                                progressDialog!!.dismiss()

                                See.toast(this@AddKaryawanActivity, "Insert add Karyawan $apiMessage")
                                finish()
                            } else {
                                progressDialog!!.dismiss()
                                See.toast(this@AddKaryawanActivity, "Insert add Karyawan $apiMessage")

                            }

                        }

                        override fun onError(anError: ANError?) {

                            progressDialog?.dismiss()
                            val json = JSONObject(anError?.errorBody)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiMessage != null) {
                                if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                    getToken(this@AddKaryawanActivity)
                                }
                            }

                            See.log("onError getProduk errorCode : ${anError?.errorCode}")
                            See.log("onError getProduk errorBody : ${anError?.errorBody}")
                            See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                        }

                    })


            }
            uri.toString().isNotEmpty() -> {

                AndroidNetworking.upload(MyConstant.Urlpegawaicreate)
                    .addHeaders("Authorization", "Bearer$token")
                    .addMultipartFile("foto", NameFile)
                    .addMultipartParameter("kontak",textInputUsernameAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("nama",textInputNamaPegawai.editText?.text.toString().trim())
                    .addMultipartParameter("password",textInputPasswordAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("role", User.userAdmin)
                    .addMultipartParameter("username", username.trim())
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            val respon = response?.toString()
                            See.log("respon add karyawan : $respon")
                            val json = JSONObject(respon)
                            val apiStatus = json.getInt(MyConstant.API_STATUS)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiStatus.equals(1)) {
                                progressDialog!!.dismiss()

                                See.toast(this@AddKaryawanActivity, "Insert add Karyawan $apiMessage")
                                finish()
                            } else {
                                progressDialog!!.dismiss()
                                See.toast(this@AddKaryawanActivity, "Insert add Karyawan $apiMessage")

                            }

                        }

                        override fun onError(anError: ANError?) {

                            progressDialog?.dismiss()
                            val json = JSONObject(anError?.errorBody)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiMessage != null) {
                                if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                    getToken(this@AddKaryawanActivity)
                                }
                            }

                            See.log("onError getProduk errorCode : ${anError?.errorCode}")
                            See.log("onError getProduk errorBody : ${anError?.errorBody}")
                            See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                        }

                    })

            }
        }



    }

    private fun updateKaryawan(userid: Int, uri: Uri?) {
        progressDialog?.show()
        val NameFile =  File(selectedImageUri?.let { getRealPathFromURI(it) })

        when {
            textInputPasswordAdmin.editText?.text.toString().isEmpty() && uri.toString().isEmpty()-> {
                AndroidNetworking.upload(MyConstant.Urlpegawaiupdate)
                    .addHeaders("Authorization", "Bearer$token")
                    .addMultipartParameter("kontak",textInputUsernameAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("nama",textInputNamaPegawai.editText?.text.toString().trim())
                    .addMultipartParameter("id", userid.toString().trim())
                    .addMultipartParameter("role", User.userAdmin)
                    .addMultipartParameter("username", username.trim())
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            val respon = response?.toString()
                            See.log("respon update no pass karyawan : $respon")
                            val json = JSONObject(respon)
                            val apiStatus = json.getInt(MyConstant.API_STATUS)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiStatus.equals(1)) {
                                progressDialog!!.dismiss()

                                See.toast(this@AddKaryawanActivity, "Insert update Karyawan $apiMessage")
                                finish()
                            } else {
                                progressDialog!!.dismiss()
                                See.toast(this@AddKaryawanActivity, "Insert update Karyawan $apiMessage")

                            }

                        }

                        override fun onError(anError: ANError?) {

                            progressDialog?.dismiss()
                            val json = JSONObject(anError?.errorBody)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiMessage != null) {
                                if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                    getToken(this@AddKaryawanActivity)
                                }
                            }

                            See.log("onError getProduk errorCode : ${anError?.errorCode}")
                            See.log("onError getProduk errorBody : ${anError?.errorBody}")
                            See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                        }

                    })

            }
            textInputPasswordAdmin.editText?.text.toString().isNotEmpty() && uri.toString().isEmpty() -> {
                AndroidNetworking.upload(MyConstant.Urlpegawaiupdate)
                    .addHeaders("Authorization", "Bearer$token")
                    .addMultipartParameter("kontak",textInputUsernameAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("nama",textInputNamaPegawai.editText?.text.toString().trim())
                    .addMultipartParameter("id", userid.toString().trim())
                    .addMultipartParameter("password",textInputPasswordAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("role", User.userAdmin)
                    .addMultipartParameter("username", username.trim())
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            val respon = response?.toString()
                            See.log("respon update no pass karyawan : $respon")
                            val json = JSONObject(respon)
                            val apiStatus = json.getInt(MyConstant.API_STATUS)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiStatus.equals(1)) {
                                progressDialog!!.dismiss()

                                See.toast(this@AddKaryawanActivity, "Insert update Karyawan $apiMessage")
                                finish()
                            } else {
                                progressDialog!!.dismiss()
                                See.toast(this@AddKaryawanActivity, "Insert update Karyawan $apiMessage")

                            }

                        }

                        override fun onError(anError: ANError?) {

                            progressDialog?.dismiss()
                            val json = JSONObject(anError?.errorBody)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiMessage != null) {
                                if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                    getToken(this@AddKaryawanActivity)
                                }
                            }

                            See.log("onError getProduk errorCode : ${anError?.errorCode}")
                            See.log("onError getProduk errorBody : ${anError?.errorBody}")
                            See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                        }

                    })

            }

            textInputPasswordAdmin.editText?.text.toString().isNotEmpty() -> {
                AndroidNetworking.upload(MyConstant.Urlpegawaiupdate)
                    .addHeaders("Authorization", "Bearer$token")
                    .addMultipartParameter("kontak",textInputUsernameAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("nama",textInputNamaPegawai.editText?.text.toString().trim())
                    .addMultipartParameter("id", userid.toString().trim())
                    .addMultipartParameter("password",textInputPasswordAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("role", User.userAdmin)
                    .addMultipartParameter("username", username.trim())
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            val respon = response?.toString()
                            See.log("respon update no pass karyawan : $respon")
                            val json = JSONObject(respon)
                            val apiStatus = json.getInt(MyConstant.API_STATUS)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiStatus.equals(1)) {
                                progressDialog!!.dismiss()

                                See.toast(this@AddKaryawanActivity, "Insert update Karyawan $apiMessage")
                                finish()
                            } else {
                                progressDialog!!.dismiss()
                                See.toast(this@AddKaryawanActivity, "Insert update Karyawan $apiMessage")

                            }

                        }

                        override fun onError(anError: ANError?) {

                            progressDialog?.dismiss()
                            val json = JSONObject(anError?.errorBody)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiMessage != null) {
                                if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                    getToken(this@AddKaryawanActivity)
                                }
                            }

                            See.log("onError getProduk errorCode : ${anError?.errorCode}")
                            See.log("onError getProduk errorBody : ${anError?.errorBody}")
                            See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                        }

                    })

            }


            uri.toString().isNotEmpty() && textInputPasswordAdmin.editText?.text.toString().isNotEmpty()-> {
                AndroidNetworking.upload(MyConstant.Urlpegawaiupdate)
                    .addHeaders("Authorization", "Bearer$token")
                    .addMultipartFile("foto", NameFile)
                    .addMultipartParameter("kontak",textInputUsernameAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("nama",textInputNamaPegawai.editText?.text.toString().trim())
                    .addMultipartParameter("id", userid.toString().trim())
                    .addMultipartParameter("password",textInputPasswordAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("role", User.userAdmin)
                    .addMultipartParameter("username", username.trim())
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            val respon = response?.toString()
                            See.log("respon update no pass karyawan : $respon")
                            val json = JSONObject(respon)
                            val apiStatus = json.getInt(MyConstant.API_STATUS)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiStatus.equals(1)) {
                                progressDialog!!.dismiss()

                                See.toast(this@AddKaryawanActivity, "Insert update Karyawan $apiMessage")
                                finish()
                            } else {
                                progressDialog!!.dismiss()
                                See.toast(this@AddKaryawanActivity, "Insert update Karyawan $apiMessage")

                            }

                        }

                        override fun onError(anError: ANError?) {

                            progressDialog?.dismiss()
                            val json = JSONObject(anError?.errorBody)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiMessage != null) {
                                if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                    getToken(this@AddKaryawanActivity)
                                }
                            }

                            See.log("onError getProduk errorCode : ${anError?.errorCode}")
                            See.log("onError getProduk errorBody : ${anError?.errorBody}")
                            See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                        }

                    })

            }
            uri.toString().isNotEmpty() && textInputPasswordAdmin.editText?.text.toString().isEmpty()-> {
                AndroidNetworking.upload(MyConstant.Urlpegawaiupdate)
                    .addHeaders("Authorization", "Bearer$token")
                    .addMultipartFile("foto", NameFile)
                    .addMultipartParameter("kontak",textInputUsernameAdmin.editText?.text.toString().trim())
                    .addMultipartParameter("nama",textInputNamaPegawai.editText?.text.toString().trim())
                    .addMultipartParameter("id", userid.toString().trim())
                    .addMultipartParameter("role", User.userAdmin)
                    .addMultipartParameter("username", username.trim())
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            val respon = response?.toString()
                            See.log("respon update no pass karyawan : $respon")
                            val json = JSONObject(respon)
                            val apiStatus = json.getInt(MyConstant.API_STATUS)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiStatus.equals(1)) {
                                progressDialog!!.dismiss()

                                See.toast(this@AddKaryawanActivity, "Insert update Karyawan $apiMessage")
                                finish()
                            } else {
                                progressDialog!!.dismiss()
                                See.toast(this@AddKaryawanActivity, "Insert update Karyawan $apiMessage")

                            }

                        }

                        override fun onError(anError: ANError?) {

                            progressDialog?.dismiss()
                            val json = JSONObject(anError?.errorBody)
                            val apiMessage = json.getString(MyConstant.API_MESSAGE)
                            if (apiMessage != null) {
                                if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                                    getToken(this@AddKaryawanActivity)
                                }
                            }

                            See.log("onError getProduk errorCode : ${anError?.errorCode}")
                            See.log("onError getProduk errorBody : ${anError?.errorBody}")
                            See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                        }

                    })

            }
        }


    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideSoftKeyboard()
        }
        return super.dispatchTouchEvent(ev)
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
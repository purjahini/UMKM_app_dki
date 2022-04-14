package com.svtech.dhuwit.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
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
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import kotlinx.android.synthetic.main.activity_add_karyawan.*
import org.json.JSONObject
import java.io.File


class AddKaryawanActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    var file: File? = null
    var fileName = ""

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_karyawan)

        token =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
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
            var folder = File(Environment.getExternalStorageDirectory(), "UmkmImage")
            if (!folder.exists()) folder.mkdir()
            file = File(folder.absolutePath, "Pegawai")
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
                            ImagePicker.with(this@AddKaryawanActivity)
                                .galleryOnly()
                                .cropSquare()
                                .compress(1024)
                                .saveDir(file!!)
                                .maxResultSize(1080, 1080)
                                .start { resultCode, data ->


                                    if (resultCode == Activity.RESULT_OK) {
                                        Glide.with(this@AddKaryawanActivity).load(data?.data)
                                            .apply(RequestOptions.bitmapTransform(RoundedCorners(10F.toInt())))
                                            .into(imgFoto)
                                        fileName = File(data?.data?.path).name

                                        See.log(" nama files : $fileName")
                                    } else if (resultCode == ImagePicker.RESULT_ERROR) {
                                        Toast.makeText(
                                            this@AddKaryawanActivity,
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

        val update = intent.getBooleanExtra("update", false)
        val idUser = intent.getIntExtra(MyConstant.ID, 0)
        val userkontak = intent.getStringExtra(MyConstant.KONTAK)
        val nama = intent.getStringExtra(MyConstant.NAMA)
        val foto = intent.getStringExtra(MyConstant.FOTO)

        if (update) {
            labelwarning.visibility = View.VISIBLE
        }


        btnSimpan.setOnClickListener {
            if (update) {

                if ( checkInput(
                        textInputNamaPegawai
                    ) && checkInputUsername(
                        textInputUsernameAdmin
                    )
                ) {

                        /*Update karyawan*/
                        updateKaryawan(idUser)

                } else {
                    Toast.makeText(this, "nama / username tidak boleh kosong", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            } else {
                if (textInputPasswordAdmin.editText?.text.toString().length == 5 && checkInput(
                        textInputNamaPegawai
                    ) && checkInputUsername(
                        textInputUsernameAdmin
                    ) && checkInputPassword(textInputPasswordAdmin)
                ) {

                        /*Insert karyawan*/
                        insertKaryawan()

                } else {
                    Toast.makeText(this, "Password Harus 5 Karakter", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
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

    private fun insertKaryawan() {
        progressDialog?.show()


        val FilePath: String = "${file}/${fileName}"


        when {
            fileName.isEmpty() -> {
                See.log("filepath : $FilePath")
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
            fileName.isNotEmpty() -> {
                See.log("filepath : $FilePath")
                AndroidNetworking.upload(MyConstant.Urlpegawaicreate)
                    .addHeaders("Authorization", "Bearer$token")
                    .addMultipartFile("foto", File(FilePath))
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

    private fun updateKaryawan(userid: Int) {
        progressDialog?.show()
        val FilePath: String = "${file}/${fileName}"

        when {
            textInputPasswordAdmin.editText?.text.toString().isEmpty() && fileName.isEmpty()-> {
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
            textInputPasswordAdmin.editText?.text.toString().isNotEmpty() && fileName.isEmpty() -> {
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


            fileName.isNotEmpty() && textInputPasswordAdmin.editText?.text.toString().isNotEmpty()-> {
                AndroidNetworking.upload(MyConstant.Urlpegawaiupdate)
                    .addHeaders("Authorization", "Bearer$token")
                    .addMultipartFile("foto", File(FilePath))
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
            fileName.isNotEmpty() && textInputPasswordAdmin.editText?.text.toString().isEmpty()-> {
                AndroidNetworking.upload(MyConstant.Urlpegawaiupdate)
                    .addHeaders("Authorization", "Bearer$token")
                    .addMultipartFile("foto", File(FilePath))
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
}
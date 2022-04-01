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
import kotlinx.android.synthetic.main.activity_add_karyawan.btnLoadImage
import kotlinx.android.synthetic.main.activity_add_karyawan.btnSimpan
import kotlinx.android.synthetic.main.activity_add_karyawan.imgFoto
import kotlinx.android.synthetic.main.activity_add_kategori.*
import org.json.JSONObject
import java.io.File
import java.util.*


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
        See.log("token login :  $token")
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
                .into(imgFoto)
            textInputNamaPegawai.editText?.setText(nama)
            textInputUsernameAdmin.editText?.setText(userkontak)
//            textInputPasswordAdmin.editText?.setText(pass)
        }
    }

    private fun insertKaryawan() {
        progressDialog?.show()
        var NameFile: File?
        val imageUrl: String = getURLForResource(R.drawable.logo1).toString()

        val FilePath: String = "${file}/${fileName}"

        if (FilePath.isEmpty()) {
            NameFile = File(imageUrl)
        } else {
            NameFile = File(FilePath)
        }
        AndroidNetworking.upload(MyConstant.Urlpegawaicreate)
            .addHeaders("Authorization", "Bearer$token")
            .addMultipartFile("foto", NameFile)
            .addMultipartParameter(
                "kontak",
                textInputUsernameAdmin.editText?.text.toString().trim()
            )
            .addMultipartParameter("nama", textInputNamaPegawai.editText?.text.toString().trim())
            .addMultipartParameter(
                "password",
                textInputPasswordAdmin.editText?.text.toString().trim()
            )
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
                    See.toast(
                        this@AddKaryawanActivity,
                        "Error Code Add karyawan ${anError?.errorCode}"
                    )
                    See.log("onError errorCode trx : ${anError?.errorCode}")
                    See.log("onError errorBody trx: ${anError?.errorBody}")
                    See.log("onError errorDetail trx: ${anError?.errorDetail}")
                }

            })

    }

    private fun updateKaryawan(userid: Int) {
        progressDialog?.show()
        var NameFile: File?
        val imageUrl: String = getURLForResource(R.drawable.logo1).toString()

        val FilePath: String = "${file}/${fileName}"

        if (FilePath.isEmpty()) {
            NameFile = File(imageUrl)
        } else {
            NameFile = File(FilePath)
        }

        if (textInputPasswordAdmin.editText?.text.toString().isEmpty()) {
            AndroidNetworking.upload(MyConstant.Urlpegawaiupdate)
                .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER + token)
                .addMultipartParameter(MyConstant.ID, userid.toString().trim())
                .addMultipartParameter(
                    MyConstant.NAMA,
                    textInputNamaPegawai.editText?.text.toString().trim()
                )
                .addMultipartParameter(MyConstant.USERNAME, username)
                .addMultipartFile(MyConstant.FOTO, NameFile)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        progressDialog?.dismiss()

                        val apiStatus = response?.getInt(Cons.API_STATUS)
                        val apiMessage = response?.getString(Cons.API_MESSAGE)

                        if (apiStatus!!.equals(1)) {
                            See.toast(
                                this@AddKaryawanActivity,
                                "Update Pegawai berhasil disimpan! $apiMessage"
                            )
                            finish()
                        } else {
                            See.toast(this@AddKaryawanActivity, "Update Pegawai gagal disimpan! $apiMessage")
                        }

                    }

                    override fun onError(anError: ANError?) {
                        progressDialog?.dismiss()
                        See.toast(this@AddKaryawanActivity, "Server Error Code : ${anError?.errorCode}")
                    }

                })

        }
        else {
            AndroidNetworking.upload(MyConstant.Urlpegawaiupdate)
                .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER + token)
                .addMultipartParameter(MyConstant.ID, userid.toString().trim())
                .addMultipartParameter(
                    MyConstant.NAMA,
                    textInputNamaPegawai.editText?.text.toString().trim()
                )
                .addMultipartParameter(MyConstant.PASSWORD, textInputPasswordAdmin.editText?.text.toString().trim())
                .addMultipartFile(MyConstant.FOTO, NameFile)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        progressDialog?.dismiss()
                        val apiStatus = response?.getInt(Cons.API_STATUS)
                        val apiMessage = response?.getString(Cons.API_MESSAGE)

                        if (apiStatus!!.equals(1)) {
                            See.toast(
                                this@AddKaryawanActivity,
                                "Update Pegawai berhasil disimpan! $apiMessage"
                            )
                            finish()
                        } else {
                            See.toast(this@AddKaryawanActivity, "Update Pegawai gagal disimpan! $apiMessage")
                        }

                    }

                    override fun onError(anError: ANError?) {
                        progressDialog?.dismiss()
                        See.toast(this@AddKaryawanActivity, "Server Error Code : ${anError?.errorCode}")
                    }

                })

        }



    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideSoftKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }
}
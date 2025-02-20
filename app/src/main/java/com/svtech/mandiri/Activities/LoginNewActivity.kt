package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.svtech.mandiri.Models.Profile
import com.svtech.mandiri.Models.User
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.checkInputPassword
import com.svtech.mandiri.Utils.checkInputUsername
import com.svtech.mandiri.Utils.getPreferences
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.savePreferences
import com.svtech.mandiri.Utils.setToolbar
import com.svtech.mandiri.modelOnline.ProfileOnline
import com.svtech.mandiri.modelOnline.UserOnline
import kotlinx.android.synthetic.main.activity_login.textInputPassword
import kotlinx.android.synthetic.main.activity_login.textInputUsername
import org.json.JSONObject

class LoginNewActivity : AppCompatActivity() {
    private lateinit var etKontak: EditText
    private lateinit var etPass: EditText

    private lateinit var btnMasuk: MaterialButton
    private lateinit var TvDaftar: TextView
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_new)
        setToolbar(this, "Login")

        token = getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username = getPreferences(this).getString(MyConstant.CURRENT_USER, "").toString()

        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Proses")
        progressDialog?.setMessage("Mohon Menunggu...")
        progressDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog?.setCancelable(false)
        progressDialog?.isIndeterminate = true

        etKontak = findViewById(R.id.etKontak)
        etPass = findViewById(R.id.etPass)
        btnMasuk = findViewById(R.id.btnMasuk)
        TvDaftar = findViewById(R.id.TvDaftar)

        TvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterNewActivity::class.java))
            finish()
        }

        btnMasuk.setOnClickListener {
            if (validasiInput()) {
                kirimData()
            }
        }


    }


    private fun kirimData() {
        val kontak = etKontak.text.toString().trim()
        val password = etPass.text.toString().trim()

        progressDialog?.show()

        AndroidNetworking.post(MyConstant.Urllogin_new)
            .addHeaders(MyConstant.AUTHORIZATION, "Bearer$token")
            .addBodyParameter(MyConstant.kontak, kontak)
            .addBodyParameter(MyConstant.password, password)
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
                        val data = Gson().fromJson(respon, UserOnline::class.java)
                        val list = data.data


                        if (list != null) {

                            if (!list.username.toString().isNullOrEmpty()) {
                                savePreferences(
                                    applicationContext,
                                    MyConstant.CURRENT_TOKO,
                                    list.username.toString()
                                )

                            }

                            if (!list.kontak.toString().isNullOrEmpty()){
                                savePreferences(
                                    applicationContext,
                                    MyConstant.CURRENT_USER,
                                    list.kontak.toString()
                                )
                            }


                            User(
                                id = list.id,
                                nama = list.nama,
                                kontak = list.kontak,
                                username = list.username,
                                role = list.role
                            ).save()

                        }

                        startActivity(Intent(this@LoginNewActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        if (apiMessage.isNotEmpty()) {
                            See.toast(this@LoginNewActivity, apiMessage)
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage == MyConstant.FORBIDDEN) {
                        getToken(this@LoginNewActivity)
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }
            })
    }







    private fun validasiInput(): Boolean {
        val kontak = etKontak.text.toString().trim()
        val pass = etPass.text.toString().trim()

        if (kontak.isEmpty()) {
            etKontak.error = "Masukkan nomor HP /email yang valid"
            return false
        }
        if (pass.isEmpty()) {
            etPass.error = "Mausukan Password yang valid"
            return false
        }

        return true
    }
}
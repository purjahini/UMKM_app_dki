package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.button.MaterialButton
import com.svtech.mandiri.Models.User
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.setToolbar
import org.json.JSONObject

class RegisterNewActivity : AppCompatActivity() {
    private lateinit var etKontak: EditText
    private lateinit var etPass: EditText
    private lateinit var etPassTwo: EditText
    private lateinit var cbKonfir: CheckBox
    private lateinit var btnDaftar: MaterialButton
    private lateinit var TvMasuk: TextView
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register_new)

        setToolbar(this, "Register")
        token =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()



        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        etKontak = findViewById(R.id.etKontak)
        etPass = findViewById(R.id.etPass)
        etPassTwo = findViewById(R.id.etPassTwo)
        cbKonfir = findViewById(R.id.cbKonfir)
        btnDaftar = findViewById(R.id.btnDaftar)
        TvMasuk = findViewById(R.id.TvMasuk)

        TvMasuk.setOnClickListener {
            startActivity(Intent(this, LoginNewActivity::class.java))
            finish()
        }




        btnDaftar.setOnClickListener {
            if (validasiInput()) {
                kirimData()
            }

        }


    }

    private fun kirimData() {
        val kontak = etKontak.text.toString().trim()
        val password = etPass.text.toString().trim()

        progressDialog?.show()

        AndroidNetworking.post(MyConstant.Urlregister_new)
            .addHeaders(MyConstant.AUTHORIZATION, "Bearer$token")
            .addBodyParameter(MyConstant.kontak, kontak)
            .addBodyParameter(MyConstant.USERNAME,kontak)
            .addBodyParameter(MyConstant.ROLE,User.userSysAdmin)
            .addBodyParameter(MyConstant.password, password)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog!!.dismiss()
                    val respon = response?.toString()
                    See.log("respon get bank Status : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {

                        startActivity(
                            Intent(
                                this@RegisterNewActivity,
                                LoginNewActivity::class.java
                            )
                        )
                        finish()

                    } else {
                        if (!apiMessage.isNullOrEmpty()) {
                            See.toast(this@RegisterNewActivity, apiMessage)
                        }
                    }

                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@RegisterNewActivity)
                        }
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
        val passTwo = etPassTwo.text.toString().trim()

        if (kontak.isEmpty() || kontak.length < 10) {
            etKontak.error = "Masukkan nomor HP yang valid"
            return false
        }
        if (pass.isEmpty() || pass.length < 6) {
            etPass.error = "Password minimal 6 karakter"
            return false
        }
        if (pass != passTwo) {
            etPassTwo.error = "Password tidak cocok"
            return false
        }
        if (!cbKonfir.isChecked) {
            Toast.makeText(this, "Setujui syarat dan ketentuan", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}
package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.Utils.checkInput
import com.svtech.dhuwit.Utils.getToken
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.util.*

class RegisterActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var randomUUID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        token = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        See.log("token Register : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        randomUUID = UUID.randomUUID().toString()
        TvToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        btnDaftar.setOnClickListener {
            progressDialog!!.show()
            if (textInputPassword.editText?.text.toString().length != 5) {
                progressDialog!!.dismiss()

                Toast.makeText(this, "Password Harus 5 Karakter", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }

            if (textInputPassword.editText?.text.toString().length == 5 && checkInput(textInputNamaToko) && checkInput(
                    textInputAlamatToko
                )
                && checkInput(textInputNamaPengguna) && checkInput(textInputUsername) && checkInput(
                    textInputPassword
                )
            ) {

                ProsesDaftarUser()

            }


        }
    }

    private fun ProsesDaftarUser() {
        progressDialog?.show()
        See.log("token ProsesDaftarUser  post : $token")
        AndroidNetworking.post(MyConstant.urlUser)
            .addHeaders("Authorization", "Bearer$token")
            .addBodyParameter("nama", textInputNamaPengguna.editText?.text.toString().trim())
            .addBodyParameter("username", textInputUsername.editText?.text.toString().trim())
            .addBodyParameter("password", textInputPassword.editText?.text.toString().trim())
            .addBodyParameter("kontak", textInputUsername.editText?.text.toString().trim())
            .addBodyParameter("role", User.userSysAdmin.trim())
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    Log.d("respon", "response  : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        progressDialog?.dismiss()
                        RegisterToko()
                        See.log("response  Register User  : $apiStatus, $apiMessage")

                    } else {

                        val snackbar = Snackbar.make(
                            findViewById(android.R.id.content),
                            apiMessage,
                            Snackbar.LENGTH_SHORT
                        )
                        snackbar.view.setBackgroundColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.primary
                            )
                        )
                        snackbar.show()
                        progressDialog?.dismiss()
                    }
                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@RegisterActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

    }


    private fun RegisterToko() {
        progressDialog?.show()
        See.log("token Register Toko: $token, USERNAME :${textInputUsername.editText?.text.toString()}")
        AndroidNetworking.post(MyConstant.urlToko)
            .addHeaders("Authorization", "Bearer$token")
            .addBodyParameter("nama_toko", textInputNamaToko.editText?.text.toString().trim())
            .addBodyParameter("alamat_toko", textInputAlamatToko.editText?.text.toString().trim())
            .addBodyParameter("username", textInputUsername.editText?.text.toString().trim())
            .addBodyParameter("kode", randomUUID.trim())
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        progressDialog!!.dismiss()

                        val intent = Intent(applicationContext, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    } else {
                        progressDialog!!.dismiss()
                        val snackbar = Snackbar.make(
                            findViewById(android.R.id.content),
                            apiMessage,
                            Snackbar.LENGTH_SHORT
                        )
                        snackbar.view.setBackgroundColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.primary
                            )
                        )
                        snackbar.show()
                    }

                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@RegisterActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })
    }
}
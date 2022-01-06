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
import com.svtech.dhuwit.Models.TokenModel
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.Utils.checkInput
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.util.*

class RegisterActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var tokenUser = ""
    var tokenToko = ""
    var randomUUID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
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
            ProsesDaftarUserToken()
        }
    }


    private fun ProsesDaftarUserToken() {


        if (textInputPassword.editText?.text.toString().length != 5) {
            progressDialog!!.dismiss()

            Toast.makeText(this, "Password Harus 5 Karakter", Toast.LENGTH_SHORT).show();
            return
        }

        if (textInputPassword.editText?.text.toString().length == 5 && checkInput(textInputNamaToko) && checkInput(
                textInputAlamatToko
            )
            && checkInput(textInputNamaPengguna) && checkInput(textInputUsername) && checkInput(
                textInputPassword
            )
        ) {

            progressDialog!!.dismiss()
//            pbLoadingRegister.visibility = View.GONE
        }
//        See.log("token Register User : $token, USERNAME :${textInputUsername.editText?.text.toString()}")


        AndroidNetworking.post(MyConstant.TOKENS)
            .addBodyParameter("secret", MyConstant.SECRET)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    Log.d("respon", "getTokenS $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt("api_status")
                    if (apiStatus.equals(1)) {
                        val data = Gson().fromJson(respon, TokenModel::class.java)
                        val list = data.data
                        tokenUser = list.access_token
                        ProsesDaftarUser()
                        progressDialog?.dismiss()
                        See.log("token ProsesDaftarUser : $tokenUser")

                    } else {
                        progressDialog?.dismiss()
                        See.log("respon token ProsesDaftarUser  : " + respon.toString())
                    }
                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    See.log("onError token ProsesDaftarUser errorCode : ${anError?.errorCode}")
                    See.log("onError token ProsesDaftarUser errorBody : ${anError?.errorBody}")
                    See.log("onError token ProsesDaftarUser errorDetail : ${anError?.errorDetail}")
                }

            })


    }

    private fun ProsesDaftarUser() {
        progressDialog?.show()
        See.log("token ProsesDaftarUser  post : $tokenUser")
        AndroidNetworking.post(MyConstant.urlUser)
            .addHeaders("Authorization", "Bearer$tokenUser")
            .addBodyParameter("NAMA", textInputNamaPengguna.editText?.text.toString().trim())
            .addBodyParameter("USERNAME", textInputUsername.editText?.text.toString().trim())
            .addBodyParameter("PASSWORD", textInputPassword.editText?.text.toString().trim())
            .addBodyParameter("KONTAK", textInputUsername.editText?.text.toString().trim())
            .addBodyParameter("ROLE", User.userSysAdmin.trim())
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
                        RegisterTokenToko()
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
                    See.log("onError ProsesDaftarUser errorCode : ${anError?.errorCode}")
                    See.log("onError ProsesDaftarUser errorBody : ${anError?.errorBody}")
                    See.log("onError ProsesDaftarUser errorDetail : ${anError?.errorDetail}")

                }

            })

    }

    private fun RegisterTokenToko() {
        progressDialog!!.show()
        AndroidNetworking.post(MyConstant.TOKENS)
            .addBodyParameter("secret", MyConstant.SECRET)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    Log.d("respon", "getTokenS $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    if (apiStatus.equals(1)) {
                        val data = Gson().fromJson(respon, TokenModel::class.java)
                        val list = data.data
                        tokenToko = list.access_token
                        progressDialog?.dismiss()
                        RegisterToko()
                        See.log(" Register Toko token: $tokenToko")

                    } else {
                        progressDialog?.dismiss()
                        See.log("respon  Register toko token  : " + respon.toString())
                    }
                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    See.log("onError RegisterToko token errorCode : ${anError?.errorCode}")
                    See.log("onError RegisterToko token errorBody : ${anError?.errorBody}")
                    See.log("onError RegisterToko token errorDetail : ${anError?.errorDetail}")
                }

            })


    }

    private fun RegisterToko() {
        progressDialog?.show()
        See.log("token Register Toko: $tokenToko, USERNAME :${textInputUsername.editText?.text.toString()}")
        AndroidNetworking.post(MyConstant.urlToko)
            .addHeaders("Authorization", "Bearer$tokenToko")
            .addBodyParameter("NAMA_TOKO", textInputNamaToko.editText?.text.toString().trim())
            .addBodyParameter("ALAMAT_TOKO", textInputAlamatToko.editText?.text.toString().trim())
            .addBodyParameter("USERNAME", textInputUsername.editText?.text.toString().trim())
            .addBodyParameter("KODE", randomUUID.trim())
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
                    See.log("onError errorCode register toko : ${anError?.errorCode}")
                    See.log("onError errorBody register toko: ${anError?.errorBody}")
                    See.log("onError errorDetail register toko: ${anError?.errorDetail}")
                }

            })
    }
}
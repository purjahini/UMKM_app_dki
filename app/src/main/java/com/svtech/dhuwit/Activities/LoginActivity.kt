package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.svtech.dhuwit.Models.Profile
import com.svtech.dhuwit.Models.TokenModel
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOl.LoginModel
import com.svtech.dhuwit.modelOl.ProfilModel
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var tokenUser = ""
    var tokenToko = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        AndroidNetworking.initialize(applicationContext)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        TvToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        btnMasuk.setOnClickListener {
            progressDialog?.show()
            getTokenLogin()


        }
    }

    private fun getLoginUser() {
        progressDialog?.show()
        if (checkInputUsername(textInputUsername) && checkInputPassword(textInputPassword)) {
            val username = textInputUsername.editText?.text.toString().trim()
            val password = textInputPassword.editText?.text.toString().trim()
            AndroidNetworking.post(MyConstant.UrlLoginUser)
                .addHeaders("Authorization", "Bearer$tokenUser")
                .addBodyParameter("USERNAME", username)
                .addBodyParameter("PASSWORD", password)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        progressDialog?.dismiss()
                        val respon = response?.toString()
                        Log.d("respon", respon.toString())
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt(MyConstant.API_STATUS)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiStatus.equals(1)) {

                            val data = Gson().fromJson(respon, LoginModel::class.java)
                            val list = data.data

                            User(
                                nama = list.NAMA,
                                username = list.USERNAME,
                                role = list.ROLE
                            ).save()
                            getTokenToko()


                        } else {
                            progressDialog?.dismiss()
                            Toast.makeText(
                                applicationContext,
                                apiMessage,
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.d("respon", "api status 0 : " + respon.toString())
                        }
                    }

                    override fun onError(anError: ANError?) {
                        progressDialog?.dismiss()
                        Log.d("respon", "aanError btnMasuk: " + anError.toString())
                    }

                })


        }
    }

    private fun getTokenToko() {
        AndroidNetworking.post(MyConstant.TOKENS)
            .addBodyParameter("secret", MyConstant.SECRET)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    Log.d("respon", respon.toString())
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt("api_status")
                    if (apiStatus.equals(1)) {
                        val data = Gson().fromJson(respon, TokenModel::class.java)
                        val list = data.data
                        tokenToko = list.access_token
                        getLoginToko()
                        Log.d("respon", "token : $tokenToko")

                    } else {

                        Log.d("respon", "api status 0 : " + respon.toString())
                    }
                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    Log.d("respon ", "aanError LoginActivity : " + anError.toString())
                }

            })

    }

    private fun getLoginToko() {
        progressDialog?.show()
        if (checkInputUsername(textInputUsername) && checkInputPassword(textInputPassword)) {
            val username = textInputUsername.editText?.text.toString().trim()
            val password = textInputPassword.editText?.text.toString().trim()
            AndroidNetworking.post(MyConstant.UrlLoginToko)
                .addHeaders("Authorization", "Bearer$tokenUser")
                .addBodyParameter("USERNAME", username)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        progressDialog?.dismiss()
                        val respon = response?.toString()
                        Log.d("respon", respon.toString())
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt("api_status")
                        if (apiStatus.equals(1)) {

                            val data = Gson().fromJson(respon, ProfilModel::class.java)
                            val list = data.data

                            Profile(
                                alamatToko = list.ALAMAT_TOKO,
                                kode = list.KODE,
                                namaToko = list.NAMA_TOKO,
                                USERNAME = list.USERNAME
                            ).save()

                            savePreferences(applicationContext, MyConstant.CURRENT_USER, username)
                            startActivity(Intent(applicationContext, DashboardActivity::class.java))
                            finish()


                        } else {
                            progressDialog?.dismiss()
                            Toast.makeText(
                                applicationContext,
                                "Login gagal user / password salah!",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.d("respon", "api status 0 : " + respon.toString())
                        }
                    }

                    override fun onError(anError: ANError?) {
                        progressDialog?.dismiss()
                        Log.d("respon", "aanError btnMasuk: " + anError.toString())
                    }

                })

        }
    }

    private fun getTokenLogin() {
        AndroidNetworking.post(MyConstant.TOKENS)
            .addBodyParameter("secret", MyConstant.SECRET)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    Log.d("respon", respon.toString())
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt("api_status")
                    if (apiStatus.equals(1)) {
                        val data = Gson().fromJson(respon, TokenModel::class.java)
                        val list = data.data
                        tokenUser = list.access_token
                        getLoginUser()
                        Log.d("respon", "token : $tokenUser")

                    } else {

                        Log.d("respon", "api status 0 : " + respon.toString())
                    }
                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    Log.d("respon ", "aanError LoginActivity : " + anError.toString())
                }

            })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideSoftKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }

}
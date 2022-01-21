package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.svtech.dhuwit.Models.Profile
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOl.LoginModel
import com.svtech.dhuwit.modelOl.ProfilModel
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        AndroidNetworking.initialize(applicationContext)

        token = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN,"")!!

        See.log("token login :  $token")
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
            if (checkInputUsername(textInputUsername) && checkInputPassword(textInputPassword)) {
                progressDialog?.show()
                getLoginUser()
            }


        }
    }

    private fun getLoginUser() {
        if (checkInputUsername(textInputUsername) && checkInputPassword(textInputPassword)) {
            val username = textInputUsername.editText?.text.toString().trim()
            val password = textInputPassword.editText?.text.toString().trim()
            AndroidNetworking.post(MyConstant.UrlLoginUser)
                .addHeaders("Authorization", "Bearer${token}")
                .addBodyParameter("USERNAME", username)
                .addBodyParameter("PASSWORD", password)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        progressDialog?.dismiss()
                        val respon = response?.toString()
                        See.log("respon getLoginUser: \n $respon")
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt(MyConstant.API_STATUS)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiStatus.equals(1)) {

                            val data = Gson().fromJson(respon, LoginModel::class.java)
                            val list = data.data

                            User(
                                id =list.id,
                                nama = list.NAMA,
                                username = list.USERNAME,
                                role = list.ROLE
                            ).save()
                            getLoginToko()
                            See.log("respon user : ${list.NAMA}, ${list.USERNAME} , ${list.ROLE}")


                        } else {
                            progressDialog?.dismiss()
                            Toast.makeText(
                                applicationContext,
                                apiMessage,
                                Toast.LENGTH_SHORT
                            ).show()

                            See.log("api status 0 : " + respon.toString())
                        }
                    }

                    override fun onError(anError: ANError?) {
                        progressDialog?.dismiss()

                        See.log("anError btnMasuk errorCode : ${anError?.errorCode}")
                        See.log("anError btnMasuk errorBody : ${anError?.errorBody}")
                        See.log("anError btnMasuk errorDetail : ${anError?.errorDetail}")
                    }

                })


        }
    }


    private fun getLoginToko() {
        progressDialog?.show()
        if (checkInputUsername(textInputUsername) && checkInputPassword(textInputPassword)) {
            val username = textInputUsername.editText?.text.toString().trim()

            AndroidNetworking.post(MyConstant.UrlLoginToko)
                .addHeaders("Authorization", "Bearer${token}")
                .addBodyParameter("USERNAME", username)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        progressDialog?.dismiss()
                        val respon = response?.toString()
                        See.log("respon getLoginToko: \n $respon")
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt("api_status")
                        if (apiStatus.equals(1)) {

                            val data = Gson().fromJson(respon, ProfilModel::class.java)
                            val list = data.data

                            Profile(
                                id = list.id,
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

                            See.log("api status 0 : " + respon.toString())
                        }
                    }

                    override fun onError(anError: ANError?) {
                        progressDialog?.dismiss()
                        See.log("aanError getLoginToko : ${anError?.errorCode}, ${anError?.errorBody}, ${anError?.errorDetail}" )
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
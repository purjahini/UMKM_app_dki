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
import com.svtech.dhuwit.Models.Produk
import com.svtech.dhuwit.Models.Profile
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOnline.ProdukOnline
import com.svtech.dhuwit.modelOnline.ProfileOnline
import com.svtech.dhuwit.modelOnline.UserOnline

import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        AndroidNetworking.initialize(applicationContext)

        token = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "")!!

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

                            val data = Gson().fromJson(respon, UserOnline::class.java)
                            val list = data.data

                            if (list != null) {
                                User(
                                    id = list.id,
                                    nama = list.NAMA,
                                    username = list.USERNAME,
                                    role = list.ROLE
                                ).save()
                                See.log("respon user : ${list.NAMA}, ${list.USERNAME} , ${list.ROLE}")
                            }
                            getLoginToko()


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

                            val data = Gson().fromJson(respon, ProfileOnline::class.java)
                            val list = data.data

                            if (list != null) {
                                Profile(
                                    id = list.id,
                                    alamatToko = list.ALAMAT_TOKO,
                                    kode = list.KODE,
                                    namaToko = list.NAMA_TOKO,
                                    USERNAME = list.USERNAME
                                ).save()
                                InsertProduks()
                            }

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
                        See.log("aanError getLoginToko : ${anError?.errorCode}, ${anError?.errorBody}, ${anError?.errorDetail}")
                    }

                })

        }
    }

    private fun InsertProduks() {
        progressDialog?.show()
//        val username = textInputUsername.editText?.text.toString().trim()
//        AndroidNetworking.post(MyConstant.UrlGetProduk)
//            .addHeaders("Authorization", "Bearer${token}")
//            .addBodyParameter("USERNAME", username)
//            .setPriority(Priority.HIGH)
//            .build()
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                override fun onResponse(response: JSONObject?) {
//                    progressDialog?.dismiss()
//
//
//                    if (response != null) {
//                        val respon = response?.toString()
//                        See.log("respon getLoginToko: \n $respon")
//                        val json = JSONObject(respon)
//                        val apiStatus = json.getInt("api_status")
//                        val data = Gson().fromJson(respon, ProdukOnline::class.java)
//
//                        val list = data.data?.toTypedArray()
//
//
//                        if (apiStatus.equals(1)) {
//
//                            for (i in 1 until response.length()) {
//                                list?.map {
//                                    if (it != null) {
//                                        val produk = Produk(
//                                            nama = it.NAMA,
//                                            kategori = it.KATEGORI,
//                                            harga = it.HARGA?.toDouble(),
//                                            foto = it.FOTO,
//                                            diskon = it.DISKON,
//                                            minimalPembelian = it.MINIMAL_PEMBELIAN,
//                                            stok = it.STOK,
//                                            satuan = it.SATUAN
//                                        )
//                                        produk.save()
//                                    }
//
//                                }
//
//
//                            }
//
//                            savePreferences(applicationContext, MyConstant.CURRENT_USER, username)
//                            startActivity(Intent(applicationContext, DashboardActivity::class.java))
//                            finish()
//
//
//                        } else {
//                            progressDialog?.dismiss()
//                            Toast.makeText(
//                                applicationContext,
//                                "Login gagal user / password salah!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//
//                            See.log("api status 0 : " + response.toString())
//                        }
//                    }
//
//                override fun onError(anError: ANError?) {
//                    progressDialog?.dismiss()
//                    See.log("aanError getLoginToko : ${anError?.errorCode}, ${anError?.errorBody}, ${anError?.errorDetail}" )
//                }
//
//            })
//
    }
}
//    private fun InsertProduks() {
//        progressDialog?.show()
//        val username = textInputUsername.editText?.text.toString().trim()
//        AndroidNetworking.post(MyConstant.UrlGetProduk)
//            .addHeaders("Authorization", "Bearer${token}")
//            .addBodyParameter("USERNAME", username)
//            .setPriority(Priority.HIGH)
//            .build()
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                override fun onResponse(response: JSONObject?) {
//                    progressDialog?.dismiss()
//
//
//                    if (response != null) {
//                        val respon = response?.toString()
//                        See.log("respon getLoginToko: \n $respon")
//                        val json = JSONObject(respon)
//                        val apiStatus = json.getInt("api_status")
//                        val data = Gson().fromJson(respon, ProdukOnline::class.java)
//
//                        val list = data.data?.toTypedArray()
//
//
//                        if (apiStatus.equals(1)) {
//
//                            for (i in 1 until response.length()) {
//                                list?.map {
//                                    if (it != null) {
//                                        val produk = Produk(
//                                            nama = it.NAMA,
//                                            kategori = it.KATEGORI,
//                                            harga = it.HARGA?.toDouble(),
//                                            foto = it.FOTO,
//                                            diskon = it.DISKON,
//                                            minimalPembelian = it.MINIMAL_PEMBELIAN,
//                                            stok = it.STOK,
//                                            satuan = it.SATUAN
//                                        )
//                                        produk.save()
//                                    }
//
//                                }
//
//
//                            }
//
//                            savePreferences(applicationContext, MyConstant.CURRENT_USER, username)
//                            startActivity(Intent(applicationContext, DashboardActivity::class.java))
//                            finish()
//
//
//                        } else {
//                            progressDialog?.dismiss()
//                            Toast.makeText(
//                                applicationContext,
//                                "Login gagal user / password salah!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//
//                            See.log("api status 0 : " + response.toString())
//                        }
//                    }
//
//                override fun onError(anError: ANError?) {
//                    progressDialog?.dismiss()
//                    See.log("aanError getLoginToko : ${anError?.errorCode}, ${anError?.errorBody}, ${anError?.errorDetail}" )
//                }
//
//            })
//
//
//    }
//
//
//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        if (currentFocus != null) {
//            hideSoftKeyboard()
//        }
//        return super.dispatchTouchEvent(ev)
//    }
//
//    }

package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.orm.SugarRecord
import com.svtech.dhuwit.Adapter.RclvUser
import com.svtech.dhuwit.AdapterOnline.RclvUserOnline
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOnline.ResponsePegawai
import kotlinx.android.synthetic.main.activity_menu_tambah_pegawai.*
import org.json.JSONObject

class MenuTambahPegawaiActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_tambah_pegawai)
        /*Setting toolbar*/
        setToolbar(this,"Tambah Karyawan")

        token =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        See.log("token Tambah Karyawan : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        /*Menampilkan item pegawai*/
        setRecyclerView()

        btnAdd.setOnClickListener {
            startActivity(Intent(this,AddKaryawanActivity::class.java))
        }
    }

   fun setRecyclerView() : Boolean {
        progressDialog?.show()
        AndroidNetworking.post(MyConstant.Urlpegawaigetdata)
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
            .addBodyParameter(MyConstant.USERNAME, username)
            .addBodyParameter(MyConstant.ROLE, User.userAdmin)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object  : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response.toString()
                    See.log("response tambah pegawai, $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(Cons.API_STATUS)
                    val apiMessage = json.getString(Cons.API_MESSAGE)
                    if (apiStatus.equals(1)) {

                        val data = Gson().fromJson(respon, ResponsePegawai::class.java)
                        if (data.toString().isNotEmpty()) {
                            tvEmpty.visibility = View.GONE

                            rclvPenjualan.apply {
                                adapter = RclvUserOnline(this@MenuTambahPegawaiActivity, data.data as MutableList<ResponsePegawai.Data> )
                                layoutManager = LinearLayoutManager(this@MenuTambahPegawaiActivity)
                                setHasFixedSize(true)
                            }

                        }



                    }
                    else {
                        tvEmpty.visibility = View.VISIBLE
                        See.toast(this@MenuTambahPegawaiActivity, apiMessage)
                    }
                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    See.toast(this@MenuTambahPegawaiActivity, "code api : ${anError?.errorCode}")
                }

            })


       return true
    }

    override fun onResume() {
        super.onResume()
        setRecyclerView()
    }
}
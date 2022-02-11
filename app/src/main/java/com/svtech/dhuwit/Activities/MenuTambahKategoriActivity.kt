package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.orm.SugarRecord
import com.svtech.dhuwit.AdapterOnline.RclvKategoriOnline
import com.svtech.dhuwit.Models.Kategori
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.Utils.setToolbar
import com.svtech.dhuwit.modelOnline.KategoriOnline
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.*
import org.json.JSONObject


class MenuTambahKategoriActivity : AppCompatActivity() {
    var token = ""
    var username = ""
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_tambah_kategori)
        /*Setting toolbar*/
        setToolbar(this, "Kategori")
        token =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "").toString()
        See.log("token Kategori : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true
        /*Setting list item kategori*/
        setToRecyclerView()

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddKategoriActivity::class.java))
        }
    }


    fun setToRecyclerView(): Boolean {
        val listKategori = SugarRecord.listAll(Kategori::class.java)
        progressDialog?.show()
        AndroidNetworking.post(MyConstant.UrlListKategori)
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
            .addBodyParameter("KATEGORI_USERNAME", username)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                  progressDialog?.dismiss()
                    val respon = response?.toString()
                    See.log("respon getKategori: $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        val data = Gson().fromJson(respon, KategoriOnline::class.java)
                        if (data.data.isNullOrEmpty()){
                            tvEmpty.visibility = View.VISIBLE
                        }else {
                            tvEmpty.visibility = View.GONE
                            val rclvadapter = RclvKategoriOnline(this@MenuTambahKategoriActivity, data.data)
                            rclv.apply {
                                adapter = rclvadapter
                                layoutManager = GridLayoutManager(context, 2)
                                setHasFixedSize(true)
                            }
                        }

                    }


                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    See.toast(this@MenuTambahKategoriActivity, "Check Koneksi Internet anda Code "+anError?.errorCode.toString() )
                    See.log("aanError getLoginToko : ${anError?.errorCode}, ${anError?.errorBody}, ${anError?.errorDetail}")
                }

            })

        return true
    }

    override fun onResume() {
        super.onResume()
        setToRecyclerView()
    }
}
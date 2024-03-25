package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.svtech.mandiri.AdapterOnline.RclvKategoriOnline
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.setToolbar
import com.svtech.mandiri.modelOnline.KategoriOnline
import kotlinx.android.synthetic.main.activity_menu_pembelian.*
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.*
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.btnAdd
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.edtPencarian
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.rclvPenjualan
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.tvEmpty
import kotlinx.android.synthetic.main.activity_menu_tambah_produk.*
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
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "").toString()
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

        edtPencarian.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                searchItem(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

    }
    private fun searchItem(search: String) {
        val adapter = rclvPenjualan.adapter
        if (adapter != null) {
            adapter as RclvKategoriOnline
            adapter.searchItem(search)
        }
    }


   private fun setToRecyclerView(): Boolean {

        progressDialog?.show()
        AndroidNetworking.post(MyConstant.UrlKategoriGetData)
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER+token)
            .addBodyParameter("username", username)
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
                            val rclvadapter = RclvKategoriOnline(this@MenuTambahKategoriActivity, data.data, false, false)
                            rclvPenjualan.apply {
                                adapter = rclvadapter
                                layoutManager = GridLayoutManager(context, 2)
                                setHasFixedSize(true)
                            }
                        }

                    } else {
                        See.toast(this@MenuTambahKategoriActivity, "response api $apiMessage")
                    }


                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@MenuTambahKategoriActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

        return true
    }

    override fun onResume() {
        super.onResume()
        setToRecyclerView()
    }
}
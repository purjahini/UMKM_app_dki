package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.orm.SugarRecord
import com.svtech.dhuwit.Adapter.RclvKategori
import com.svtech.dhuwit.Models.Kategori
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.Utils.calculateNoOfColumns
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.*


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

        if (listKategori.isEmpty()) {
//            AndroidNetworking.post(MyConstant.UrlListKategori)
//                .addHeaders("Authorization", "Bearer$token")
//                .addBodyParameter("USERNAME", username?.trim())
//                .setPriority(Priority.MEDIUM)
//                .build()
//                .getAsJSONObject(object : JSONObjectRequestListener {
//                    override fun onResponse(response: JSONObject?) {
//                        val respon = response?.toString()
//                        See.log("respon insertKategori : \n $respon")
//                        val json = JSONObject(respon)
//                        val apiStatus = json.getInt(MyConstant.API_STATUS)
//                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
//                        if (apiStatus.equals(1)) {
//
//                            val data = Gson().fromJson(respon, KategoriModel::class.java).data
//
//                            val kategori = Kategori(
//                                     id = data.id,
//                                     nama =data.NAMA,
//                                     gambar = data.GAMBAR
//                                 )
//                                 kategori.save()
//                            finish()
//
//
//
////                            val listKt = listOf<KategoriModel>()
//
////                            for (data in listKt){
////                             data.let {
////                                 val kategori = Kategori(
////                                     id = it.data.id,
////                                     nama =it.data.NAMA,
////                                     gambar = it.data.GAMBAR
////                                 )
////                                 kategori.save()
////                             }
//
//
////                            }
//
//
//                            progressDialog!!.dismiss()
//
//                            See.toast(this@MenuTambahKategoriActivity, "Upload Kategori to Server $apiMessage")
//                            finish()
//                        } else {
//                            progressDialog!!.dismiss()
//                            See.toast(this@MenuTambahKategoriActivity, "Upload Kategori to Server $apiMessage")
//
//                        }
//
//                    }
//
//                    override fun onError(anError: ANError?) {
//                        progressDialog?.dismiss()
//                        See.log("onError errorCode insertKategori : ${anError?.errorCode}")
//                        See.log("onError errorBody insertKategori: ${anError?.errorBody}")
//                        See.log("onError errorDetail insertKategori: ${anError?.errorDetail}")
//                    }
//
//                })

            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
            val rclvadapter = RclvKategori(this, listKategori)
            rclv.apply {
                adapter = rclvadapter
                layoutManager = GridLayoutManager(context, 2)
                setHasFixedSize(true)
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        setToRecyclerView()
    }
}
package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.orm.SugarRecord

import com.svtech.dhuwit.AdapterOnline.RclvProdukOnline
import com.svtech.dhuwit.Models.ItemTransaksi
import com.svtech.dhuwit.Models.Transaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOnline.ProdukOnline
import kotlinx.android.synthetic.main.activity_menu_pembelian.*
import kotlinx.android.synthetic.main.activity_menu_pembelian.edtPencarian
import kotlinx.android.synthetic.main.activity_menu_pembelian.rclvPenjualan
import kotlinx.android.synthetic.main.activity_menu_pembelian.tvEmpty
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.*
import kotlinx.android.synthetic.main.activity_menu_tambah_produk.*
import org.json.JSONObject

class MenuPembelianActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_pembelian)
        /*Setting toolbar*/
        setToolbar(this, getString(R.string.penjualan))

        token =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        See.log("token penjualan : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        /*Setting list item produk*/
        setToRecyclerView()
        /*Setting tampilan item dalam keranjang*/
        setBadgeKeranjang()
        /*Membuka menu keranjang*/
        btnKeranjang.setOnClickListener {
            startActivity(Intent(this, MenuKeranjangActivity::class.java))
            finish()
        }

        /*Sorting*/
        btnSort.setOnClickListener {
            if (btnSort.tag == null || btnSort.tag.equals("Dsc")) {
                btnSort.tag = "Asc"
                btnSort.icon = getDrawable(R.drawable.ic_arrow_downward_24)
                sortItem("Dsc")
            } else {
                btnSort.tag = "Dsc"
                btnSort.icon = getDrawable(R.drawable.ic_arrow_upward_24)
                sortItem("Asc")
            }

        }

        edtPencarian.setOnFocusChangeListener { view, b ->
            if (b) {
                guideline6.setGuidelinePercent(.8f)
            } else {
                guideline6.setGuidelinePercent(.5f)
            }
        }
        /*Fungsi Pencarian*/
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
        if(adapter != null){
            adapter as RclvProdukOnline
            adapter.searchItem(search)
        }
    }

    private fun sortItem(sort: String) {
        val adapter = rclvPenjualan.adapter
        if(adapter != null){
            adapter as RclvProdukOnline
            adapter.sortItem(sort)
        }
    }

    fun setToRecyclerView(): Boolean {
        AndroidNetworking.post(MyConstant.Urlproduklistdata)
            .addHeaders("Authorization", "Bearer${token}")
            .addBodyParameter("username", username)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject( object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()

                    if (response != null ) {
                        val respon = response?.toString()
                        See.log("respon getProduk: \n $respon")
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt(MyConstant.API_STATUS)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)

                        if (apiStatus.equals(1)) {
                            val data = Gson().fromJson(respon, ProdukOnline::class.java)

                            val list = data.data
                            if (list?.isEmpty() == true) {
                                tvEmpty.visibility = View.VISIBLE
                            } else {
                                tvEmpty.visibility = View.GONE
                                val rclvadapter = RclvProdukOnline(this@MenuPembelianActivity,list,true, true)
                                rclvPenjualan.apply {
                                    adapter = rclvadapter
                                    layoutManager = GridLayoutManager(context, 2)
                                    setHasFixedSize(true)
                                }
                            }
                        } else {
                            See.toast(this@MenuPembelianActivity, apiMessage)
                        }
                    }
                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@MenuPembelianActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

//        if (listProduk.isEmpty()) {
//            tvEmpty.visibility = View.VISIBLE
//        } else {
//            tvEmpty.visibility = View.GONE
//            val rclvadapter = RclvProduk(this, listProduk, true, true)
//            rclv.apply {
//                adapter = rclvadapter
//                layoutManager = GridLayoutManager(context, calculateNoOfColumns(context, 180F))
//                setHasFixedSize(true)
//            }
//        }
        return true
    }

    open fun setBadgeKeranjang() {
        val transaksi = SugarRecord.find(Transaksi::class.java, "status =?", "1").firstOrNull()
        if (transaksi != null) {
            val itemTransaksi = SugarRecord.find(
                ItemTransaksi::class.java,
                "id_transaksi = ?",
                transaksi.id.toString()
            )
            var count = 0
            for (item in itemTransaksi) {
                count += item.jumlah!!
            }
            btnKeranjang.count = count
        } else {
            btnKeranjang.count = 0
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideSoftKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }
}
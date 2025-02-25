package com.svtech.mandiri.Activities

import AdapterSlider
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.orm.SugarRecord
import com.svtech.mandiri.Adapter.RclvItemMenu
import com.svtech.mandiri.Models.Menu
import com.svtech.mandiri.Models.Profile
import com.svtech.mandiri.Models.User
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.Cons
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.numberToCurrency
import com.svtech.mandiri.Utils.savePreferences
import com.svtech.mandiri.modelOnline.ProfileOnline
import com.svtech.mandiri.modelOnline.ResponseBilboardSaldo
import kotlinx.android.synthetic.main.activity_dashboard.LLSaldo
import kotlinx.android.synthetic.main.activity_dashboard.TvSaldo
import kotlinx.android.synthetic.main.activity_dashboard.btnEditProfile
import kotlinx.android.synthetic.main.activity_dashboard.imgEditProfile
import kotlinx.android.synthetic.main.activity_dashboard.imgLogo
import kotlinx.android.synthetic.main.activity_dashboard.rclvPenjualan
import kotlinx.android.synthetic.main.activity_dashboard.tvAlamatCafe
import kotlinx.android.synthetic.main.activity_dashboard.tvNamaCafe
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

class DashboardActivity : AppCompatActivity() {


    lateinit var vpSlider: ViewPager
    lateinit var timer: Timer
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    var kontak = ""
    private lateinit var arrSlider: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        vpSlider = findViewById(R.id.vp_slider)

        token =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()

        username =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_TOKO, "")
                .toString()

        kontak =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        showPopUpCheckAvail(username)
        val user = SugarRecord.find(User::class.java, "USERNAME = ?", username)
            .firstOrNull()
        if (user != null) {
            initMenu(user)
        }


        LLSaldo.setOnClickListener {
            LoadBilboardSaldo()
//            timer.cancel()
        }

//        val arrSlider = ArrayList<Int>()
//        arrSlider.add(R.drawable.slider1)
//        arrSlider.add(R.drawable.slider2)
//        arrSlider.add(R.drawable.slider3)

//        val adapterSlider = AdapterSlider(arrSlider, this)
//        vpSlider.adapter = adapterSlider
//
//
//        val timerTask: TimerTask = object : TimerTask() {
//            override fun run() {
//                vpSlider.post(Runnable { vpSlider.setCurrentItem((vpSlider.getCurrentItem() + 1) % arrSlider.size) })
//            }
//        }
//        timer = Timer()
//        timer.schedule(timerTask, 3000, 3000)


        /*Setting menu*/
//        val username = com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER,"")


        /*Setting data profile*/
//        initProfile(list)

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfilNewActivity::class.java))
        }

    }

    private fun showPopUpCheckAvail(username: String) {
        progressDialog?.show()

        AndroidNetworking.post(MyConstant.UrlLoginToko)
            .addHeaders("Authorization", "Bearer${token}")
            .addBodyParameter("username", username)
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

                        initProfile(list)

                        LoadBilboardSaldo()

                        if (list != null) {

                            Profile(
                                id = list.id,
                                alamatToko = list.alamat_toko,
                                kode = list.kode,
                                namaToko = list.nama_toko,
                                USERNAME = list.username
                            ).save()
                            savePreferences(
                                applicationContext,
                                MyConstant.CURRENT_USER,
                                username.toString()
                            )
                            savePreferences(this@DashboardActivity,MyConstant.kode,list.kode.toString())
                        }


                    } else {
                        startActivity(
                            Intent(
                                this@DashboardActivity,
                                RegisterTokoActivity::class.java
                            )
                        )
                        progressDialog?.dismiss()
                        Toast.makeText(
                            this@DashboardActivity,
                            "Silahkan Isi Detail Toko",
                            Toast.LENGTH_SHORT
                        ).show()

                        See.log("api status 0 : " + respon.toString())
                    }
                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@DashboardActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }


            })

    }

    private fun LoadBilboardSaldo() {
        progressDialog?.show()
        AndroidNetworking.post(MyConstant.Urlbilboardsaldo)
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "Bearer${token}")
            .addBodyParameter("username", username)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    See.Companion.log("response bilboardsaldo : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(Cons.API_STATUS)
                    val apiMessage = json.getString(Cons.API_MESSAGE)
                    if (apiStatus.equals(Cons.INT_STATUS)) {
                        val data = Gson().fromJson(respon, ResponseBilboardSaldo::class.java)
                        val saldo = data.saldo
                        if (saldo != null) {
                            TvSaldo.setText("SALDO : " + numberToCurrency(saldo))
                        }


//                        arrSlider = ArrayList<String>()
//                        for (item in data.data) {
//                            arrSlider.add(item.foto)
//                        }
//
//                        setSliderAdapter(arrSlider)


                    } else {
                        See.toast(this@DashboardActivity, apiMessage)

                    }

                }

                override fun onError(anError: ANError?) {
                    See.toast(
                        this@DashboardActivity,
                        "Mohon Check Koneksi Internet Anda.. \nCode Error :  ${anError?.errorCode}"
                    )
                    See.log("anError DashboardActivity errorCode : ${anError?.errorCode}")
                    See.log("anError DashboardActivity errorBody : ${anError?.errorBody}")
                    See.log("anError DashboardActivity errorDetail : ${anError?.errorDetail}")


                }

            })
    }

    private fun setSliderAdapter(arrSlider: ArrayList<String>) {
        val adapterSlider = AdapterSlider(arrSlider, this)
        vpSlider.adapter = adapterSlider

        // Mulai timer untuk memindahkan slider
        startTimer()
    }

    private fun startTimer() {
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                vpSlider.post(Runnable {
                    vpSlider.setCurrentItem(
                        (vpSlider.currentItem + 1) % arrSlider.size,
                        true
                    )
                })
            }
        }
        timer = Timer()
        timer.schedule(timerTask, 3000, 3000)
    }

    private fun initMenu(user: User) {
        var listMenu: MutableList<Menu>
        var colSpan: Int
        if (user.role.equals(User.userSysAdmin)) {
            colSpan = 3
            listMenu = mutableListOf(
                Menu(R.drawable.penjualan, "Penjualan"),
                Menu(R.drawable.keranjang, "Keranjang"),
                Menu(R.drawable.icon4, "Kategori"),
                Menu(R.drawable.produk, "Produk"),
                Menu(R.drawable.pegawai, "Pegawai"),
                Menu(R.drawable.keuangan, "Keuangan"),
//                Menu(R.drawable.ic_account, "Wallet"),
//                Menu(R.drawable.icon6, "Laporan")
            )
            imgEditProfile.setImageDrawable(getDrawable(R.drawable.ic_user))
        } else {
            colSpan = 3
            listMenu = mutableListOf(
                Menu(R.drawable.penjualan, "Penjualan"),
                Menu(R.drawable.keranjang, "Keranjang"),
                Menu(R.drawable.icon3, "Kategori"),
                Menu(R.drawable.produk, "Produk")
            )
            imgEditProfile.setImageDrawable(getDrawable(R.drawable.ic_user))
        }

        rclvPenjualan.apply {
            adapter = RclvItemMenu(this@DashboardActivity, listMenu)
            layoutManager = GridLayoutManager(this@DashboardActivity, colSpan)
            setHasFixedSize(true)
        }
    }


    fun initProfile(list: ProfileOnline.Data?) {

        if (list != null) {
            if (list.logo_toko != null) {
                Glide.with(this)
                    .load(list.logo_toko)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgLogo)
            }
            tvNamaCafe.text = list.nama_toko
            tvAlamatCafe.text = list.alamat_toko

        }
    }

    override fun onResume() {
        super.onResume()
        showPopUpCheckAvail(username)

    }


}



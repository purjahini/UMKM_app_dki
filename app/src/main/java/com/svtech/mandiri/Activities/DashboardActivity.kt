package com.svtech.mandiri.Activities

import AdapterSlider
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
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
import com.svtech.mandiri.Utils.numberToCurrency
import com.svtech.mandiri.modelOnline.ResponseBilboardSaldo
import kotlinx.android.synthetic.main.activity_dashboard.*
import org.json.JSONObject
import java.util.*

class DashboardActivity : AppCompatActivity() {
    //...

    lateinit var vpSlider: ViewPager
    lateinit var timer: Timer
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    private lateinit var arrSlider: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        vpSlider = findViewById(R.id.vp_slider)
        token =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()

        username =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        LoadBilboardSaldo()

        LLSaldo.setOnClickListener {
            LoadBilboardSaldo()
            timer.cancel()
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
        val username = com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER,"")
        val user = SugarRecord.find(User::class.java,"USERNAME = ?",username).firstOrNull()
        if(user != null){
            initMenu(user)
        }

        /*Setting data profile*/
        initProfile()

        btnEditProfile.setOnClickListener{
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

    }

    private fun LoadBilboardSaldo() {
        progressDialog?.show()
        AndroidNetworking.post(MyConstant.Urlbilboardsaldo)
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "Bearer${token}")
            .addBodyParameter("username",username)
            .build()
            .getAsJSONObject(object :JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    See.Companion.log("response bilboardsaldo : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(Cons.API_STATUS)
                    val apiMessage = json.getString(Cons.API_MESSAGE)
                    if (apiStatus.equals(Cons.INT_STATUS)) {
                        val data = Gson().fromJson(respon,ResponseBilboardSaldo::class.java)
                        val saldo = data.saldo
                        if (saldo != null) {
                            TvSaldo.setText("SALDO : "+numberToCurrency(saldo))
                        }



                        arrSlider = ArrayList<String>()
                        for (item in data.data) {
                            arrSlider.add(item.foto)
                        }

                        setSliderAdapter(arrSlider)



                    } else {
                        See.toast(this@DashboardActivity, apiMessage)

                    }

                }

                override fun onError(anError: ANError?) {
                    See.toast(this@DashboardActivity,
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
                vpSlider.post(Runnable { vpSlider.setCurrentItem((vpSlider.currentItem + 1) % arrSlider.size, true) })
            }
        }
        timer = Timer()
        timer.schedule(timerTask, 3000, 3000)
    }

    private fun initMenu(user: User) {
        var listMenu : MutableList<Menu>
        var colSpan : Int
        if (user.role.equals(User.userSysAdmin)){
            colSpan = 4
            listMenu = mutableListOf(
                Menu(R.drawable.icon1,"Penjualan"),
                Menu(R.drawable.icon2,"Keranjang"),
                Menu(R.drawable.icon3,"Kategori"),
                Menu(R.drawable.icon4,"Produk"),
                Menu(R.drawable.icon5,"Pegawai"),
                Menu(R.drawable.ic_chart, "Neraca"),
                Menu(R.drawable.ic_account,"Wallet"),
                Menu(R.drawable.icon6,"Laporan")
            )
            imgEditProfile.setImageDrawable(getDrawable(R.drawable.ic_user))
        }else{
            colSpan = 4
            listMenu = mutableListOf(
                Menu(R.drawable.icon1,"Penjualan"),
                Menu(R.drawable.icon2,"Keranjang"),
                Menu(R.drawable.icon3,"Kategori"),
                Menu(R.drawable.icon4,"Produk")
            )
            imgEditProfile.setImageDrawable(getDrawable(R.drawable.ic_user))
        }

        rclvPenjualan.apply {
            adapter = RclvItemMenu(this@DashboardActivity,listMenu)
            layoutManager = GridLayoutManager(this@DashboardActivity, colSpan)
            setHasFixedSize(true)
        }
    }


    fun initProfile(){
        val profile  = SugarRecord.listAll(Profile::class.java).firstOrNull()
        if(profile != null){
            if(profile.logoToko != null){
                Glide.with(this).load(profile.logoToko).fitCenter().into(imgLogo)
            }
            tvNamaCafe.text = profile.namaToko
            tvAlamatCafe.text = profile.alamatToko
        }
    }

    override fun onResume() {
        super.onResume()
        initProfile()
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()

    }


}
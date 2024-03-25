package com.svtech.mandiri.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.orm.SugarRecord
import com.svtech.mandiri.Adapter.AdapterSliderSplash
import com.svtech.mandiri.BuildConfig
import com.svtech.mandiri.Models.Profile

import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.getDeviceId

import com.svtech.mandiri.Utils.savePreferences
import com.svtech.mandiri.modelOnline.SliderItem
import org.json.JSONObject
import com.svtech.mandiri.modelOnline.TokenModel
import kotlinx.android.synthetic.main.activity_splash_screen.*
import org.json.JSONException
import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask


class SplashScreenActivity : AppCompatActivity() {

    var time: Long = 0

    lateinit var vpSlider: ViewPager
    lateinit var timer: Timer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Glide.with(this)
            .load(R.drawable.logo1)
            .into(ivImageSplash)
        AndroidNetworking.initialize(applicationContext)

        getDeviceId(this)
        See.log(" id device "+getDeviceId(this))

        vpSlider = findViewById(R.id.vp_slider)

        val arrSlider = ArrayList<SliderItem>()
        arrSlider.add(SliderItem(R.drawable.slidersplash1,"Teknologi kami memudahkan anda untuk bertransaksi lebih cepat"))
        arrSlider.add(SliderItem(R.drawable.slidersplash2,"Membantu Kemudahan Transaksi Untuk UMKM"))
        arrSlider.add(SliderItem(R.drawable.slidersplash3,"Pelayana lebih cepat dan tepat untuk semua Konsumen"))

        val adapterSlider = AdapterSliderSplash(arrSlider, this)
        vpSlider.adapter = adapterSlider


        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                vpSlider.post(Runnable { vpSlider.setCurrentItem((vpSlider.getCurrentItem() + 1) % arrSlider.size) })
            }
        }
        timer = Timer()
        timer.schedule(timerTask, 3000, 3000)

        RegisterDevice()





    }

    private fun RegisterDevice() {
        val DeviceData = JSONObject()
        try {
            DeviceData.put("mobile_id", getDeviceId(this))
            DeviceData.put(
                "app_name",
                getString(R.string.app_name) + " Ver. " + BuildConfig.VERSION_NAME
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        AndroidNetworking.post(MyConstant.BASE_URL+MyConstant.UrlrequestDevice)
            .addJSONObjectBody(DeviceData)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    See.log("Response RegisterDevice :$respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getString(MyConstant.API_STATUS_DVS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE_DEVICE)
                    val apiStatusDevice = json.getInt(MyConstant.API_STATUS_DEVICE)

                    if (apiStatus.equals(MyConstant.API_SUCCESS)) {
                        labelwarning.setText(apiMessage)

                        when(apiStatusDevice) {
                            1 -> {



                            }
                            2-> {
                                val timeRightnow :Long = System.currentTimeMillis()/1000

                                val idDevice = getDeviceId(this@SplashScreenActivity)

                                time = com.svtech.mandiri.Utils.getPreferences(this@SplashScreenActivity).getLong(MyConstant.TIME,0)

                                if (timeRightnow > time){
                                    hitToken()
                                }

                                See.log("jam sekararang $timeRightnow , waktu  expiry $time , idDevice : $idDevice")

                                /*Insert data default ke database*/
                                val profile = SugarRecord.listAll(Profile::class.java).firstOrNull()
                                if (profile != null) {


                                }
//        feedFirstDataToDatabase()
                                val username =   com.svtech.mandiri.Utils.getPreferences(this@SplashScreenActivity).getString(MyConstant.CURRENT_USER, "")
                                Handler().postDelayed({
                                    if (username != null) {
                                        if (username.isEmpty()) {
                                            startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
                                            finish()
                                        } else {
                                            startActivity(Intent(this@SplashScreenActivity, DashboardActivity::class.java))
                                            finish()
                                        }
                                    }
                                    See.log("Username Splash : $username")
                                }, 2000)
                            }
                            3->{

                            }
                        }

                    } else {
                        RegisterDevice()
                    }


                }

                override fun onError(anError: ANError?) {
                    See.toast(this@SplashScreenActivity,
                    "Mohon Check Koneksi Internet Anda.. \nCode Error :  ${anError?.errorCode}"
                    )
                    See.log("anError $this@SplashActivity errorCode : ${anError?.errorCode}")
                    See.log("anError$this@SplashActivity errorBody : ${anError?.errorBody}")
                    See.log("anError $this@SplashActivity errorDetail : ${anError?.errorDetail}")

                }

            })

    }

    private fun hitToken() {
        AndroidNetworking.post(MyConstant.UrlTOKENS)
            .addBodyParameter("secret",MyConstant.SECRET)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {

                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    See.log("respon hitToken: \n $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    if (apiStatus.equals(1)){
                        val data = Gson().fromJson(respon, TokenModel::class.java)
                        val list = data.data
                        if (list != null) {
                            var  token = list.access_token
                            time = list.expiry!!.toLong()
                            if (token != null) {
                                savePreferences(this@SplashScreenActivity, MyConstant.TOKEN,token)
                            }
                            savePreferences(this@SplashScreenActivity, MyConstant.TIME, time)
                            See.log("token Splash: $token")
                        }

                    }
                    else {
                        See.log( "api status 0 : "+respon.toString())
                    }
                }

                override fun onError(anError: ANError?) {
                    See.log("onError errorCode : ${anError?.errorCode}")
                    See.log("onError errorBody : ${anError?.errorBody}")
                    See.log("onError errorDetail : ${anError?.errorDetail}")
                }

            })
    }

}
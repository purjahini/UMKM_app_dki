package com.svtech.dhuwit.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.Profile

import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.Utils.getDeviceId

import com.svtech.dhuwit.Utils.savePreferences
import org.json.JSONObject
import com.svtech.dhuwit.modelOnline.TokenModel
import kotlinx.android.synthetic.main.activity_splash_screen.*


class SplashScreenActivity : AppCompatActivity() {

    var time: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Glide.with(this)
            .load(R.drawable.logo1)
            .into(ivImageSplash)
        AndroidNetworking.initialize(applicationContext)
        val timeRightnow :Long = System.currentTimeMillis()/1000

        val idDevice = getDeviceId(this)

        time = com.svtech.dhuwit.Utils.getPreferences(this).getLong(MyConstant.TIME,0)

        if (timeRightnow > time){
            hitToken()
        }

        See.log("jam sekararang $timeRightnow , waktu  expiry $time , idDevice : $idDevice")

        /*Insert data default ke database*/
        val profile = SugarRecord.listAll(Profile::class.java).firstOrNull()
        if (profile != null) {


        }
//        feedFirstDataToDatabase()
      val username =   com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
        Handler().postDelayed({
            if (username != null) {
                if (username.isEmpty()) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            }
            See.log("Username Splash : $username")
        }, 2000)

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
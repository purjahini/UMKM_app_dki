package com.svtech.dhuwit.Activities

import android.R.attr
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.Profile
import com.svtech.dhuwit.Models.TokenModel
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.See

import com.svtech.dhuwit.Utils.savePreferences
import org.json.JSONObject
import android.content.SharedPreferences






class SplashScreenActivity : AppCompatActivity() {

    var time: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        AndroidNetworking.initialize(applicationContext)
        val timeRightnow :Long = System.currentTimeMillis()/1000

        time = com.svtech.dhuwit.Utils.getPreferences(this).getLong(MyConstant.TIME,0)

        if (timeRightnow > time){
            hitToken()
        }

        See.log("jam sekararang $timeRightnow , waktu  expiry $time")

        /*Insert data default ke database*/
        val profile = SugarRecord.listAll(Profile::class.java).firstOrNull()
        if (profile != null) {

        }
//        feedFirstDataToDatabase()
        Handler().postDelayed({
            if (com.svtech.dhuwit.Utils.getPreferences(this)
                    .getString(MyConstant.CURRENT_USER, "")!!.isEmpty()

            ) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
        }, 2000)

    }

    private fun hitToken() {
        AndroidNetworking.post(MyConstant.TOKENS)
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
                      var  token = list.access_token
                        time = list.expiry.toLong()


                        savePreferences(this@SplashScreenActivity, MyConstant.TOKEN,token)
                        savePreferences(this@SplashScreenActivity, MyConstant.TIME, time)
                        See.log("token Splash: $token")

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
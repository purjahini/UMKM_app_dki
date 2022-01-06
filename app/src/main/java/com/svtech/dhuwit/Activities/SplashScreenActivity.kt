package com.svtech.dhuwit.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
import com.svtech.dhuwit.Utils.getToken
import com.svtech.dhuwit.Utils.savePreferences
import org.json.JSONObject

class SplashScreenActivity : AppCompatActivity() {
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        AndroidNetworking.initialize(applicationContext)


//        AndroidNetworking.post(MyConstant.TOKENS)
//            .addBodyParameter("secret",MyConstant.SECRET)
//            .setPriority(Priority.HIGH)
//            .build()
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                override fun onResponse(response: JSONObject?) {
//                   val respon = response?.toString()
//                    Log.d("respon", respon.toString())
//                    val json = JSONObject(respon)
//                    val apiStatus = json.getInt(MyConstant.API_STATUS)
//                    if (apiStatus.equals(1)){
//                        val data = Gson().fromJson(respon, TokenModel::class.java)
//                        val list = data.data
//                        token = list.access_token
//                        See.log("token Splash: $token")
//
//                    }
//                    else {
//                        See.log( "api status 0 : "+respon.toString())
//                    }
//                }
//
//                override fun onError(anError: ANError?) {
//                    See.log("onError errorCode : ${anError?.errorCode}")
//                    See.log("onError errorCode : ${anError?.errorBody}")
//                    See.log("onError errorCode : ${anError?.errorDetail}")
//                }
//
//            })

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

    private fun feedFirstDataToDatabase() {
        val firstStart = com.svtech.dhuwit.Utils.getPreferences(this)
            .getBoolean(MyConstant.FIRST_START, false)
        if (!firstStart) {
            savePreferences(this, MyConstant.FIRST_START, true)
            Profile(
                namaToko = "Dhuwit POS",
                alamatToko = "Jl. Lengkong Besar 73, Bandung, Jawa Barat, 40261, Indonesia"
            ).save()

            User(
                nama = "Admin",
                username = "085712312345",
                password = "123456",
                role = User.userSysAdmin
            ).save()
        }
    }
}
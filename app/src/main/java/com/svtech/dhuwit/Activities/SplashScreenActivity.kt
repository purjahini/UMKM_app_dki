package com.svtech.dhuwit.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.orm.SugarRecord
import com.svtech.dhuwit.MainActivity
import com.svtech.dhuwit.Models.Profile
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.savePreferences

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
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
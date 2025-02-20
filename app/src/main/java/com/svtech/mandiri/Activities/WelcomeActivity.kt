package com.svtech.mandiri.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.svtech.mandiri.R
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        startLoadingAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, SplashScreenActivity::class.java)
            startActivity(intent)
            finish()

        },3000)




    }

    private fun startLoadingAnimation() {
        val  progressRunnable = Runnable {
            var progress = 0
            while (progress <= 100) {
                try {
                    Thread.sleep(30)
                    progressBar.progress = progress
                    progress++

                } catch (e : InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        Thread(progressRunnable).start()

    }
}
package com.svtech.mandiri.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_laporan.*

class LaporanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)
        setToolbar(this, "Laporan")

        btnLaporanPenjualan.setOnClickListener {
            startActivity(Intent(this, LaporanPenjualanActivity::class.java))
        }

        btnLaporanStok.setOnClickListener {
            startActivity(Intent(this, LaporanStokActivity::class.java))

        }

        btnLaporanHarian.setOnClickListener{
            startActivity(Intent(this, LaporanHarianActivity::class.java))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
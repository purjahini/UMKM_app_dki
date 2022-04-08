package com.svtech.dhuwit.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_laporan.*

class LaporanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)
        setToolbar(this, "Laporan")

        btnLaporanPenjualan.setOnClickListener {
            startActivity(Intent(this, LaporanPenjualanActivity::class.java))
//            See.toast(this, "Dalam Pengembangan")
        }

        btnLaporanStok.setOnClickListener {
            startActivity(Intent(this, LaporanStokActivity::class.java))
//            See.toast(this, "Dalam Pengembangan")
        }

        btnLaporanHarian.setOnClickListener{
            startActivity(Intent(this, LaporanHarianActivity::class.java))
        }
    }
}
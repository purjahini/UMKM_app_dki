package com.svtech.dhuwit.Activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.orm.SugarRecord
import com.svtech.dhuwit.Adapter.RclvLaporanStok
import com.svtech.dhuwit.Models.Stok
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_laporan_penjualan.*

class LaporanStokActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_stok)
        setToolbar(this, "Laporan Stok")

        //set recyclerview
        val stok = SugarRecord.listAll(Stok::class.java)
        val sorted = stok.sortedBy { l -> l.tanggal }.reversed()
        if (stok.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
            rclv.apply {
                adapter = RclvLaporanStok(this@LaporanStokActivity, sorted as MutableList<Stok>)
                layoutManager = LinearLayoutManager(this@LaporanStokActivity)
                setHasFixedSize(true)
            }
        } else {
            tvEmpty.visibility = View.VISIBLE
        }
    }
}
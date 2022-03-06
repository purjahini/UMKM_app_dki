package com.svtech.dhuwit.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.orm.SugarRecord
import com.svtech.dhuwit.Adapter.RclvUser
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.calculateNoOfColumns
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_menu_tambah_pegawai.*

class MenuTambahPegawaiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_tambah_pegawai)
        /*Setting toolbar*/
        setToolbar(this,"Tambah Pegawai")
        /*Menampilkan item pegawai*/
        setRecyclerView()

        btnAdd.setOnClickListener {
            startActivity(Intent(this,AddKaryawanActivity::class.java))
        }
    }

    private fun setRecyclerView() {
        val pegawai = SugarRecord.find(User::class.java,"role = ?",User.userAdmin)
        if(pegawai.isEmpty()){
            tvEmpty.visibility = View.VISIBLE
        }else{
            tvEmpty.visibility = View.GONE
            rclvPenjualan.apply {
                adapter = RclvUser(this@MenuTambahPegawaiActivity, pegawai)
                layoutManager = GridLayoutManager(this@MenuTambahPegawaiActivity, calculateNoOfColumns(this@MenuTambahPegawaiActivity,200F))
                setHasFixedSize(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setRecyclerView()
    }
}
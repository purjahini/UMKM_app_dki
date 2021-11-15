package com.svtech.dhuwit.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.orm.SugarRecord
import com.svtech.dhuwit.Adapter.RclvKategori
import com.svtech.dhuwit.Models.Kategori
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.calculateNoOfColumns
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.*


class MenuTambahKategoriActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_tambah_kategori)
        /*Setting toolbar*/
        setToolbar(this, "Kategori")
        /*Setting list item kategori*/
        setToRecyclerView()

        btnAdd.setOnClickListener {
            startActivity(Intent(this,AddKategoriActivity::class.java))
        }
    }


    fun setToRecyclerView(): Boolean {
        val listKategori = SugarRecord.listAll(Kategori::class.java)
        if (listKategori.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
            val rclvadapter = RclvKategori(this, listKategori)
            rclv.apply {
                adapter = rclvadapter
                layoutManager = GridLayoutManager(context, calculateNoOfColumns(context,200F))
                setHasFixedSize(true)
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        setToRecyclerView()
    }
}
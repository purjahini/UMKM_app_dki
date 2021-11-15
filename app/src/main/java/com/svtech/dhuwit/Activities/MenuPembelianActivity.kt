package com.svtech.dhuwit.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.orm.SugarRecord
import com.svtech.dhuwit.Adapter.RclvProduk
import com.svtech.dhuwit.Models.ItemTransaksi
import com.svtech.dhuwit.Models.Produk
import com.svtech.dhuwit.Models.Transaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.calculateNoOfColumns
import com.svtech.dhuwit.Utils.hideSoftKeyboard
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_menu_pembelian.*

class MenuPembelianActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_pembelian)
        /*Setting toolbar*/
        setToolbar(this, "Pembelian")
        /*Setting list item produk*/
        setToRecyclerView()
        /*Setting tampilan item dalam keranjang*/
        setBadgeKeranjang()
        /*Membuka menu keranjang*/
        btnKeranjang.setOnClickListener {
            startActivity(Intent(this, MenuKeranjangActivity::class.java))
            finish()
        }

        /*Sorting*/
        btnSort.setOnClickListener {
            if (btnSort.tag == null || btnSort.tag.equals("Dsc")) {
                btnSort.tag = "Asc"
                btnSort.icon = getDrawable(R.drawable.ic_arrow_downward_24)
                sortItem("Dsc")
            } else {
                btnSort.tag = "Dsc"
                btnSort.icon = getDrawable(R.drawable.ic_arrow_upward_24)
                sortItem("Asc")
            }

        }

        edtPencarian.setOnFocusChangeListener { view, b ->
            if (b) {
                guideline6.setGuidelinePercent(.8f)
            } else {
                guideline6.setGuidelinePercent(.5f)
            }
        }
        /*Fungsi Pencarian*/
        edtPencarian.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                searchItem(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    private fun searchItem(search: String) {
        val adapter = rclv.adapter
        if(adapter != null){
            adapter as RclvProduk
            adapter.searchItem(search)
        }
    }

    private fun sortItem(sort: String) {
        val adapter = rclv.adapter
        if(adapter != null){
            adapter as RclvProduk
            adapter.sortItem(sort)
        }
    }

    fun setToRecyclerView(): Boolean {
        val listProduk = SugarRecord.listAll(Produk::class.java)
        if (listProduk.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
            val rclvadapter = RclvProduk(this, listProduk, true, true)
            rclv.apply {
                adapter = rclvadapter
                layoutManager = GridLayoutManager(context, calculateNoOfColumns(context, 180F))
                setHasFixedSize(true)
            }
        }
        return true
    }

    open fun setBadgeKeranjang() {
        val transaksi = SugarRecord.find(Transaksi::class.java, "status =?", "1").firstOrNull()
        if (transaksi != null) {
            val itemTransaksi = SugarRecord.find(
                ItemTransaksi::class.java,
                "id_transaksi = ?",
                transaksi.id.toString()
            )
            var count = 0
            for (item in itemTransaksi) {
                count += item.jumlah!!
            }
            btnKeranjang.count = count
        } else {
            btnKeranjang.count = 0
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideSoftKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }
}
package com.svtech.dhuwit.Activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.SugarRecord
import com.svtech.dhuwit.Adapter.RclvItemTransaksi
import com.svtech.dhuwit.Models.ItemTransaksi
import com.svtech.dhuwit.Models.Transaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.checkInput
import com.svtech.dhuwit.Utils.hideSoftKeyboard
import com.svtech.dhuwit.Utils.numberToCurrency
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_menu_keranjang.*
import kotlinx.android.synthetic.main.layout_input_dialog.view.*
import kotlinx.android.synthetic.main.sheet.view.*
import java.text.SimpleDateFormat
import java.util.*

class MenuKeranjangActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_keranjang)
        /*Setting toolbar*/
        setToolbar(this, "Keranjang")

        /*Menapilkan list item produk di keranjang*/
        setRecyclerView(SugarRecord.find(Transaksi::class.java, "status = ?", "1").firstOrNull())

        btnCheckout.setOnClickListener {
            val transaksi = SugarRecord.find(Transaksi::class.java, "status = ?", "1").firstOrNull()
            if (transaksi != null) {
                /*Mengupdate tanggal transaksi */
                transaksi.tanggalTrasaksi =
                    android.text.format.DateFormat.format("dd/MM/yyyy hh:mm:ss", Date().time)
                        .toString()
                transaksi.save()
            }


            Log.d("total", transaksi?.totalPembayaran.toString())

            val btnSheet = layoutInflater.inflate(R.layout.sheet, null)
            val dialog = BottomSheetDialog(this)
            val btnCash = btnSheet.btnCash
            val btnCashless = btnSheet.btnCashless
            btnCash.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
            }
            btnCashless.setOnClickListener {
            val intent = Intent(this, CashActivity::class.java)
            intent.putExtra("Total", transaksi?.totalPembayaran)
            startActivity(intent)
            }
            dialog.setContentView(btnSheet)
            dialog.show()



        }

        /*Setting nama pembeli*/
        layoutNamaPembeli.setOnClickListener {
            val transaksi =
                SugarRecord.find(Transaksi::class.java, "status = ?", "1").firstOrNull()
            val layout =
                LayoutInflater.from(this).inflate(R.layout.layout_input_dialog, null, false)
            layout.textInput.hint = "Nama Pembeli"
            layout.textInput.editText?.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            layout.textInput.editText?.setText(transaksi?.namaPembeli.toString())
            MaterialAlertDialogBuilder(this)
                .setView(layout)
                .setNegativeButton(
                    "Batal",
                    DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .setPositiveButton("Oke", DialogInterface.OnClickListener { dialogInterface, i ->
                    if (checkInput(layout.textInput)) {
                        if (transaksi != null) {
                            tvNamaPembeli.setText(layout.textInput.editText?.text)
                            transaksi.namaPembeli = layout.textInput.editText?.text.toString()
                            transaksi.save()
                        }
                    }
                }).show()
        }
        /*Setting nilai bayar*/
        layoutBayar.setOnClickListener {
            val transaksi =
                SugarRecord.find(Transaksi::class.java, "status = ?", "1").firstOrNull()
            val layout =
                LayoutInflater.from(this).inflate(R.layout.layout_input_dialog, null, false)
            layout.textInput.hint = "Bayar"
            layout.textInput.prefixText = "Rp. "
            layout.textInput.editText?.inputType = InputType.TYPE_CLASS_NUMBER
            layout.textInput.editText?.setText(transaksi?.bayar?.toInt().toString())
            MaterialAlertDialogBuilder(this)
                .setView(layout)
                .setNegativeButton(
                    "Batal",
                    DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .setPositiveButton("Oke", DialogInterface.OnClickListener { dialogInterface, i ->
                    if (checkInput(layout.textInput)) {
                        if (transaksi != null) {
                            tvBayar.setText(
                                numberToCurrency(
                                    layout.textInput.editText?.text.toString().toDouble()
                                )
                            )
                            transaksi.bayar = layout.textInput.editText?.text.toString().toDouble()
                            transaksi.save()
                            tvKembalian.setText(
                                numberToCurrency(transaksi.bayar!! - transaksi.totalPembayaran!!)
                            )
                        }
                    }
                }).show()
        }

        rclv.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                setTotalPembayaran()
                setDiskon()
            }
        })
    }

    /*Fungsi untuk melakukan update data total pembayaran*/
    fun setTotalPembayaran() {
        val transaksi = SugarRecord.find(Transaksi::class.java, "status = ?", "1").firstOrNull()
        val itemTransaksi =
            SugarRecord.find(ItemTransaksi::class.java, "id_transaksi = ?", "${transaksi?.id}")

        if (transaksi != null) {
            var totalPembayaran = 0.0
            for (item in itemTransaksi) {
                totalPembayaran += (item.jumlah!! * item.hargaProduk!!)
                /*potong diskon*/
                if (item.jumlah!! >= item.minimalPembelianProduk!!) {
                    totalPembayaran -= (item.jumlah!! * item.hargaProduk!!) * (item.diskonProduk!! / 100)
                }
            }
            transaksi.totalPembayaran = totalPembayaran
            transaksi.save()
        }
        setInfoTransaksi(transaksi)
    }

    /*Fungsi untuk melakukan update data diskon*/
    fun setDiskon() {
        val transaksi = SugarRecord.find(Transaksi::class.java, "status = ?", "1").firstOrNull()
        val itemTransaksi =
            SugarRecord.find(ItemTransaksi::class.java, "id_transaksi = ?", "${transaksi?.id}")
        if (transaksi != null) {
            var diskon = 0.0
            for (item in itemTransaksi) {
                if (item.jumlah!! >= item.minimalPembelianProduk!!) {
                    diskon += (item.jumlah!! * item.hargaProduk!!) * (item.diskonProduk!! / 100)
                }
            }
            transaksi.diskon = diskon
            transaksi.save()
        }
        setInfoTransaksi(transaksi)

    }


    /*Fungsi untuk setting data transaksi*/
    fun setInfoTransaksi(transaksi: Transaksi?) {
        if (transaksi != null) {
            btnCheckout.isEnabled = true
            tvNoitem.visibility = View.GONE
            tvTanggal.setText(SimpleDateFormat("dd-MM-yyyy").format(Date().time))
            tvNamaPembeli.setText(transaksi.namaPembeli)
            tvDiskon.setText(numberToCurrency(transaksi.diskon!!))
            tvTotalBayar.setText(
                numberToCurrency(transaksi.totalPembayaran!!)
            )
            tvBayar.setText(
                numberToCurrency(transaksi.bayar!!)
            )
            tvKembalian.setText(
                numberToCurrency(transaksi.bayar!! - transaksi.totalPembayaran!!)
            )
            val item =
                SugarRecord.find(ItemTransaksi::class.java, "id_transaksi = ?", "${transaksi.id}")
                    .firstOrNull()
            if (item == null) {
                btnCheckout.isEnabled = false
                tvNoitem.visibility = View.VISIBLE
            } else {
                btnCheckout.isEnabled = true
                tvNoitem.visibility = View.GONE
            }
        } else {
            btnCheckout.isEnabled = false
            tvNoitem.visibility = View.VISIBLE
        }
    }

    fun setRecyclerView(transaksi: Transaksi?) {
        if (transaksi != null) {
            val itemTransaksi =
                SugarRecord.find(ItemTransaksi::class.java, "id_transaksi = ?", "${transaksi?.id}")
            rclv.apply {
                adapter = RclvItemTransaksi(context, itemTransaksi)
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                setHasFixedSize(true)
            }
        }
        setInfoTransaksi(transaksi)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            this.hideSoftKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }
}
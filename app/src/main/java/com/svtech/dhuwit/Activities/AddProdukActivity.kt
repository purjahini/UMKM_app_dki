package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.Kategori
import com.svtech.dhuwit.Models.Produk
import com.svtech.dhuwit.Models.Stok
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import kotlinx.android.synthetic.main.activity_add_produk.*
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

lateinit var adapterSpinner: ArrayAdapter<Kategori>
lateinit var adapterSpinnerSatuan: ArrayAdapter<String>

class AddProdukActivity : AppCompatActivity() {
    var token = ""
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_produk)
        token = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        See.log("token addProduk : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        /*Setting tombol back dan title*/
        setToolbar(this, "Tambah Produk")
        /*Setting adapter spinner*/
        adapterSpinner = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            SugarRecord.listAll(Kategori::class.java)
        )
        val listSatuan = mutableListOf<String>()
        listSatuan.add("Pcs")
        listSatuan.add("Karton")
        listSatuan.add("Rtg")
        listSatuan.add("Pack")
        listSatuan.add("Qty")
        adapterSpinnerSatuan = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listSatuan
        )
        spnKategori.adapter = adapterSpinner
        spnSatuan.adapter = adapterSpinnerSatuan

        btnLoadImage.setOnClickListener {
            /*Membuka galeri*/
            pickImage(this, imgFoto,"Produk")
        }
        val update = intent.getBooleanExtra("update", false)
        val produkId = intent.getLongExtra("produk", -1)
        val produk = SugarRecord.findById(Produk::class.java, produkId)

        btnSimpan.setOnClickListener {
            /*Simpan dengan diskon*/
            if (cbDiskon.isChecked) {
                if (checkInput(textInputNamaProduk) && checkInput(spnKategori)
                    && checkInput(textInputHargaProduk) && checkInput(spnSatuan) && checkInput(
                        textInputDiskon
                    )
                    && checkInput(textInputMinimalPembelian) && checkInput(textInputStok)
                ) {
                    if (update) {
                        /*Update produk*/
                        updateProduk(produk)
                    } else {
                        /*Insert produk*/
                        insertProduk()
                    }
                    Toast.makeText(this, "Produk berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                /*Simpan tanpa diskon*/
                if (checkInput(textInputNamaProduk) && checkInput(spnKategori)
                    && checkInput(textInputHargaProduk) && checkInput(textInputStok) && checkInput(
                        spnSatuan
                    )
                ) {
                    if (update) {
                        /*Update produk*/
                        updateProduk(produk)
                    } else {
                        /*Insert produk*/
                        insertProduk()
                    }
                    Toast.makeText(this, "Produk berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

        }

        if (produk != null) {
            Glide.with(this).load(Base64.decode(produk.foto, Base64.DEFAULT)).fitCenter()
                .into(imgFoto)
            textInputNamaProduk.editText?.setText(produk.nama)
            textInputHargaProduk.editText?.setText(produk.harga?.toInt().toString())
            spnKategori.selection = adapterSpinner.getPosition(produk.kategori)
            textInputStok.editText?.setText(produk.stok.toString())
            spnSatuan.selection = adapterSpinnerSatuan.getPosition(produk.satuan)
            if (produk.diskon != null) {
                showDiskonInput()
                cbDiskon.isChecked = true
                textInputDiskon.editText?.setText(produk.diskon.toString())
                textInputMinimalPembelian.editText?.setText(produk.minimalPembelian.toString())
            }
        }

        /*CheckBox Diskon Listener*/
        cbDiskon.setOnCheckedChangeListener { _, isChecked ->
            /*Tampilkan diskon input*/
            if (isChecked) showDiskonInput()
            /*Sembunyikan diskon input*/
            else hideDiskonInput()
        }
    }

    fun insertProduk(): Boolean {
        val byteArray = ImageViewToByteArray(imgFoto)
        val produk: Produk
        /*insert dengan diskon*/
        if (cbDiskon.isChecked) {
            AndroidNetworking.post(MyConstant.urlToko)
                .addHeaders("Authorization", "Bearer$token")
                .addBodyParameter("NAMA_TOKO", textInputNamaToko.editText?.text.toString().trim())
                .addBodyParameter("ALAMAT_TOKO", textInputAlamatToko.editText?.text.toString().trim())
                .addBodyParameter("USERNAME", textInputUsername.editText?.text.toString().trim())

                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        val respon = response?.toString()
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt(MyConstant.API_STATUS)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiStatus.equals(1)) {
                            progressDialog!!.dismiss()

                            val intent = Intent(applicationContext, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        } else {
                            progressDialog!!.dismiss()
                            val snackbar = Snackbar.make(
                                findViewById(android.R.id.content),
                                apiMessage,
                                Snackbar.LENGTH_SHORT
                            )
                            snackbar.view.setBackgroundColor(
                                ContextCompat.getColor(
                                    applicationContext,
                                    R.color.primary
                                )
                            )
                            snackbar.show()
                        }

                    }

                    override fun onError(anError: ANError?) {
                        progressDialog?.dismiss()
                        See.log("onError errorCode register toko : ${anError?.errorCode}")
                        See.log("onError errorBody register toko: ${anError?.errorBody}")
                        See.log("onError errorDetail register toko: ${anError?.errorDetail}")
                    }

                })
            produk = Produk(
                nama = textInputNamaProduk.editText?.text.toString(),
                harga = textInputHargaProduk.editText?.text.toString().toDouble(),
                foto = Base64.encodeToString(byteArray, Base64.DEFAULT),
                kategori = spnKategori.selectedItem as Kategori,
                diskon = textInputDiskon.editText?.text.toString().toDouble(),
                minimalPembelian = textInputMinimalPembelian.editText?.text.toString().toInt(),
                stok = textInputStok.editText?.text.toString().toInt(),
                satuan = spnSatuan.selectedItem as String
            )
        } else {
            /*insert tanpa diskon*/
            produk = Produk(
                nama = textInputNamaProduk.editText?.text.toString(),
                harga = textInputHargaProduk.editText?.text.toString().toDouble(),
                foto = Base64.encodeToString(byteArray, Base64.DEFAULT),
                kategori = spnKategori.selectedItem as Kategori,
                diskon = null,
                minimalPembelian = null,
                stok = textInputStok.editText?.text.toString().toInt(),
                satuan = spnSatuan.selectedItem as String
            )
        }
        produk.save()
        /*Update Stok*/
        val stok = Stok(
            jumlah = produk.stok,
            isTambah = true,
            idProduk = produk.id,
            tanggal = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
                Date().time
            )
        )
        stok.save()
        return true
    }

    fun updateProduk(produk: Produk): Boolean {
        val byteArray = ImageViewToByteArray(imgFoto)
        val tempStok = produk.stok
        produk.nama = textInputNamaProduk.editText?.text.toString()
        produk.harga = textInputHargaProduk.editText?.text.toString().toDouble()
        produk.foto = Base64.encodeToString(byteArray, Base64.DEFAULT)
        produk.kategori = spnKategori.selectedItem as Kategori
        produk.stok = textInputStok.editText?.text.toString().toInt()
        produk.satuan = spnSatuan.selectedItem as String
        /*jika ada diskon*/
        if (cbDiskon.isChecked) {
            produk.diskon = textInputDiskon.editText?.text.toString().toDouble()
            produk.minimalPembelian = textInputMinimalPembelian.editText?.text.toString().toInt()
        } else {
            produk.diskon = null
            produk.minimalPembelian = null
        }
        produk.save()

        /*Update Stok*/
        val stok: Stok
        if (produk.stok!! > tempStok!!) {
            stok = Stok(
                jumlah = produk.stok,
                isTambah = true,
                idProduk = produk.id,
                tanggal = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
                    Date().time
                )
            )
        } else {
            stok = Stok(
                jumlah = produk.stok,
                isTambah = false,
                idProduk = produk.id,
                tanggal = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
                    Date().time
                )
            )
        }
        stok.save()
        return true
    }

    fun showDiskonInput() {
        textInputDiskon.visibility = View.VISIBLE
        textInputMinimalPembelian.visibility = View.VISIBLE
        /*Change focus to diskon input*/
        textInputDiskon.requestFocus()
    }

    fun hideDiskonInput() {
        textInputDiskon.visibility = View.GONE
        textInputMinimalPembelian.visibility = View.GONE
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideSoftKeyboard()
        }
        return super.onTouchEvent(event)
    }
}
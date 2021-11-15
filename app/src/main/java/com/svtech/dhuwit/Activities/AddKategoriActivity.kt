package com.svtech.dhuwit.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import com.bumptech.glide.Glide
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.Kategori
import com.svtech.dhuwit.Models.Produk
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.ImageViewToByteArray
import com.svtech.dhuwit.Utils.checkInput
import com.svtech.dhuwit.Utils.pickImage
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_add_kategori.*
import kotlinx.android.synthetic.main.activity_add_kategori.btnLoadImage
import kotlinx.android.synthetic.main.activity_add_kategori.btnSimpan
import kotlinx.android.synthetic.main.activity_add_kategori.imgFoto

class AddKategoriActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_kategori)
        /*Setting tombol back dan title*/
        setToolbar(this, "Tambah Kategori")

        btnLoadImage.setOnClickListener {
            /*Membuka galeri*/
            pickImage(this, imgFoto)
        }
        val update = intent.getBooleanExtra("update", false)
        val idKategori = intent.getLongExtra("kategori",-1)
        val kategori = SugarRecord.findById(Kategori::class.java,idKategori)
        btnSimpan.setOnClickListener {
            if(checkInput(textInputNamaKategori)){
                if(update){
                    /*Update kategori*/
                    updateKategori(kategori)
                }else{
                    /*Insert kategori*/
                    insertKategori()
                }
                Toast.makeText(this, "Kategori berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            }

        }
        if(kategori != null){
            Glide.with(this).load(Base64.decode(kategori.gambar, Base64.DEFAULT)).fitCenter().into(imgFoto)
            textInputNamaKategori.editText?.setText(kategori.nama)
        }

    }

    fun insertKategori():Boolean{
        val byteArray = ImageViewToByteArray(imgFoto)
        val kategori = Kategori(
            nama = textInputNamaKategori.editText?.text.toString(),
            gambar = Base64.encodeToString(byteArray, Base64.DEFAULT)
        )
        kategori.save()
        return true
    }

    fun updateKategori(kategori: Kategori):Boolean{
        val byteArray = ImageViewToByteArray(imgFoto)
        val count = SugarRecord.listAll(Produk::class.java).count()
        kategori.nama = textInputNamaKategori.editText?.text.toString()
        kategori.gambar = Base64.encodeToString(byteArray, Base64.DEFAULT)
        kategori.save()
        return true
    }
}
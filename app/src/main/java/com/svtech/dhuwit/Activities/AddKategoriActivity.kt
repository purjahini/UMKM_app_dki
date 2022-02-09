package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.Kategori
import com.svtech.dhuwit.Models.Produk
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import kotlinx.android.synthetic.main.activity_add_kategori.*
import org.json.JSONObject
import java.util.*

class AddKategoriActivity : AppCompatActivity() {
    var token = ""
    var username = ""
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_kategori)
        /*Setting tombol back dan title*/
        setToolbar(this, "Tambah Kategori")
        token =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username =
            com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        See.log("token addProduk : $token")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        btnLoadImage.setOnClickListener {
            /*Membuka galeri*/
            var data = pickImage(this, imgFoto,"Kategori")
            See.log("kategori return uri $data")
        }
        val update = intent.getBooleanExtra("update", false)
        val idKategori = intent.getLongExtra("kategori", -1)
        val kategori = SugarRecord.findById(Kategori::class.java, idKategori)


        btnSimpan.setOnClickListener {
            if (checkInput(textInputNamaKategori)) {
                if (update) {
                    /*Update kategori*/
                    updateKategori(kategori)
                } else {
                    /*Insert kategori*/
                    insertServerKategori()

                }
//                Toast.makeText(this, "Kategori berhasil disimpan!", Toast.LENGTH_SHORT).show()
//                finish()
            }

        }
        if (kategori != null) {
            Glide.with(this).load(Base64.decode(kategori.gambar, Base64.DEFAULT)).fitCenter()
                .into(imgFoto)
            textInputNamaKategori.editText?.setText(kategori.nama)
        }

    }

    fun insertServerKategori() {
        progressDialog!!.show()
        val  byteArray = ImageViewToByteArray(imgFoto)
//        val image = Base64.decode(byteArray,Base64.DEFAULT)
//        fun writeBytesAsPdf(bytes : ByteArray) {
//            val path = applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
//            var file = File.createTempFile("kategori",".png", path)
//            var os = FileOutputStream(file);
//            os.write(bytes);
//            os.close();
//        }
//        writeBytesAsPdf(byteArray)

        AndroidNetworking.post(MyConstant.UrlInputKategori)
            .addHeaders("Authorization", "Bearer$token")
            .addBodyParameter("GAMBAR", Base64.encodeToString(byteArray, Base64.DEFAULT).trim())
            .addBodyParameter("NAMA", textInputNamaKategori.editText?.text.toString().trim())
            .addBodyParameter("USERNAME", username.trim())
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    See.log("respon insertKategori : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        progressDialog!!.dismiss()
                        insertLocalKategori()
                        See.toast(this@AddKategoriActivity, "Upload Kategori to Server $apiMessage")
                        finish()
                    } else {
                        progressDialog!!.dismiss()
                        See.toast(this@AddKategoriActivity, "Upload Kategori to Server $apiMessage")

                    }

                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    See.log("onError errorCode insertKategori : ${anError?.errorCode}")
                    See.log("onError errorBody insertKategori: ${anError?.errorBody}")
                    See.log("onError errorDetail insertKategori: ${anError?.errorDetail}")
                }

            })


    }

    fun insertLocalKategori(): Boolean {
      val  byteArrays = ImageViewToByteArray(imgFoto)
        val kategori = Kategori(
            nama = textInputNamaKategori.editText?.text.toString(),
            gambar = Base64.encodeToString(byteArrays, Base64.DEFAULT)
        )
        kategori.save()

        return true

    }

    fun updateKategori(kategori: Kategori): Boolean {
        val byteArray = ImageViewToByteArray(imgFoto)
        val count = SugarRecord.listAll(Produk::class.java).count()
        kategori.nama = textInputNamaKategori.editText?.text.toString()
        kategori.gambar = Base64.encodeToString(byteArray, Base64.DEFAULT)
        kategori.save()
        return true
    }
}
package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.orm.SugarRecord
import com.svtech.mandiri.Models.User
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_add_kategori.textInputNamaKategori
import kotlinx.android.synthetic.main.activity_edit_profile.textUid
import org.json.JSONObject

class AddbankActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    private lateinit var etNamaBank: EditText
    private lateinit var etNoRek: EditText
    private lateinit var etAtasNama: EditText
    private lateinit var etUsername: EditText
    private lateinit var btnSubmit: Button
    var token  = ""
    var username = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addbank)

        setToolbar(this, "Tambah Bank")

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        username = com.svtech.mandiri.Utils.getPreferences(this)
            .getString(MyConstant.CURRENT_USER,"").toString()
        token = com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN,"").toString()

        etNamaBank = findViewById(R.id.etNamaBank)
        etNoRek = findViewById(R.id.etNoRek)
        etAtasNama = findViewById(R.id.etAtasNama)

        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            sendBankData()
        }


    }

    private fun sendBankData() {

        val namaBank = etNamaBank.text.toString().trim()
        val noRek = etNoRek.text.toString().trim()
        val atasNama = etAtasNama.text.toString().trim()

        if (namaBank.isEmpty() || noRek.isEmpty() || atasNama.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        AndroidNetworking.post(MyConstant.Urltambahbank)
            .addHeaders("Authorization", "Bearer$token")
            .addBodyParameter("nama_bank", namaBank)
            .addBodyParameter("no_rek", noRek)
            .addBodyParameter("atas_nama", atasNama)
            .addBodyParameter("username", username)
            .setTag("POST_BANK")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val respon = response?.toString()
                    See.log("respon insertBank : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus.equals(1)) {
                        progressDialog!!.dismiss()
//                        insertLocalKategori()
                        See.toast(this@AddbankActivity,apiMessage)
                        finish()
                    } else {
                        progressDialog!!.dismiss()
                        See.toast(this@AddbankActivity, apiMessage)

                    }

                }

                override fun onError(anError: ANError?) {

                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage != null) {
                        if (apiMessage.equals(MyConstant.FORBIDDEN)) {
                            getToken(this@AddbankActivity)
                        }
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

    }
}
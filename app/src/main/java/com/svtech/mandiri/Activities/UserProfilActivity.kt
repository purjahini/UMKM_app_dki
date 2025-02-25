package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.*
import org.json.JSONObject
import java.io.File

class UserProfilActivity : AppCompatActivity(), ImagePickerCallback {
    private var progressDialog: ProgressDialog? = null
    private var token = ""
    private var username = ""
    private var kontak = ""
    private var name = ""
    private var id = ""
    private var email = ""
    private var foto = ""

    private lateinit var imagePickerHelper: ImagePickerHelper
    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null

    private lateinit var btnLoadImage: ImageButton
    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etKontak: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profil)

        imagePickerHelper = ImagePickerHelper(this, this)
        setToolbar(this, "Profil")
        imageView = findViewById(R.id.imgProfil)
        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etKontak = findViewById(R.id.etKontak)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnLoadImage = findViewById(R.id.btnLoadImage)

        // Ambil data dari Intent
        id = intent.getStringExtra(MyConstant.ID) ?: ""
        name = intent.getStringExtra(MyConstant.NAMA) ?: ""
        foto = intent.getStringExtra(MyConstant.FOTO) ?: ""
        email = intent.getStringExtra(MyConstant.EMAIL) ?: ""

        // Ambil data dari SharedPreferences
        token = getPreferences(this).getString(MyConstant.TOKEN, "").toString()
        username = getPreferences(this).getString(MyConstant.CURRENT_TOKO, "").toString()
        kontak = getPreferences(this).getString(MyConstant.CURRENT_USER, "").toString()

        progressDialog = ProgressDialog(this).apply {
            setTitle("Proses")
            setMessage("Mohon Menunggu...")
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(false)
            isIndeterminate = true
        }

        // Set data ke form
        etKontak.setText(kontak)
        etNama.setText(name)
        etEmail.setText(email.ifEmpty { "-" })

        Glide.with(this)
            .load(foto)
            .apply(RequestOptions.circleCropTransform())
            .error(R.drawable.admin)
            .into(imageView)

        // Tombol untuk memilih gambar
        btnLoadImage.setOnClickListener {
            imagePickerHelper.showImagePickerDialog()
        }

        // Tombol untuk mengupdate profil
        btnSubmit.setOnClickListener {
            updateUserProfile()
        }
    }

    override fun onImagePicked(imageUri: Uri) {
        selectedImageUri = imageUri
//        imageView.setImageURI(imageUri)

        Glide.with(this)
            .load(imageUri)
            .apply(RequestOptions.circleCropTransform())
            .error(R.drawable.admin)
            .into(imageView)

    }

    private fun updateUserProfile() {
        val namaBaru = etNama.text.toString().trim()
        val emailBaru = etEmail.text.toString().trim()
        val kontakBaru = etKontak.text.toString().trim()

        if (namaBaru.isEmpty() || emailBaru.isEmpty() || kontakBaru.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog?.show()

        val request = AndroidNetworking.upload(MyConstant.Urlupdateuser)
            .addHeaders("Authorization", "Bearer $token")
            .addMultipartParameter("id", id)
            .addMultipartParameter("nama", namaBaru)
            .addMultipartParameter("email", emailBaru)
            .addMultipartParameter("kontak", kontakBaru)
            .addMultipartParameter("username", username)

        // Jika user memilih gambar baru, tambahkan gambar ke request
        selectedImageUri?.let { uri ->
            val file = File(FileUtils.getPath(this, uri))
            request.addMultipartFile("foto", file)
        }

        request.setTag("updateProfile")
            .setPriority(com.androidnetworking.common.Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    See.Companion.log("response update profil : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(Cons.API_STATUS)
                    val apiMessage = json.getString(Cons.API_MESSAGE)
                    if (apiStatus.equals(Cons.INT_STATUS)) {
                        Toast.makeText(this@UserProfilActivity, apiMessage, Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@UserProfilActivity, apiMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage == MyConstant.FORBIDDEN) {
                        getToken(this@UserProfilActivity)
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")   }
            })
    }
}

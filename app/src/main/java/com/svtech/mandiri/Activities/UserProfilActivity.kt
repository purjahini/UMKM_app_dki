package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.ImagePickerHelper
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.setToolbar

class UserProfilActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    var kontak = ""

    private lateinit var imagePickerHelper: ImagePickerHelper
    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null

    lateinit var btnLoadImage : Button
    lateinit var etNama : EditText
    lateinit var etEmail : EditText
    lateinit var etKontak : EditText
    lateinit var btnSubmit : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_user_profil)
        setToolbar(this, "Profil")
        imageView = findViewById(R.id.imgProfil)


        token =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()

        username =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_TOKO, "")
                .toString()

        kontak =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

    }
}
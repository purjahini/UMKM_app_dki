package com.svtech.mandiri.Activities

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.text.InputType
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.orm.SugarRecord
import com.svtech.mandiri.Models.Profile
import com.svtech.mandiri.Models.User
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.*
import kotlinx.android.synthetic.main.activity_edit_profile.*


class  EditProfileActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        setToolbar(this, "Ubah Profil")

        textUid.text = "UID : "+getDeviceId(this)

        imageView = findViewById(R.id.imgFoto)
        /*Setting data profile*/
        val username = getPreferences(this).getString(MyConstant.CURRENT_USER,"")
        val token = getPreferences(this).getString(MyConstant.TOKEN,"")
        val user = SugarRecord.find(User::class.java,"USERNAME = ?",username).firstOrNull()
        initProfile(user)

//        getDataUser(token,username)


        btnLoadImage.setOnClickListener {
            /*Membuka galeri*/
//            pickImage(this, imgFoto,"Profil")
            showImagePickerDialog()

        }

        /*Logout*/
        btnLogOut.setOnClickListener {
            if (username != null) {
                deletePreferences(this,username)
                See.log("username deletePref $username")
            }

            if (token != null) {
                deletePreferences(this,token)
                See.log("token deletePref $token")
            }

            val users = SugarRecord.find(User::class.java, "USERNAME =?",username).firstOrNull()
            See.log("respon user ${users}")
            users?.delete()

            val profil = SugarRecord.find(Profile::class.java,"USERNAME=?",username).firstOrNull()
            See.log("respon profil ${profil}")
            profil?.delete()



            startActivity(Intent(this,SplashScreenActivity::class.java))
            finishAffinity()
            finish()
        }

        btnSave.setOnClickListener {
            /*Melakukan update data profile dan data user*/
            val bitmapdata = ImageViewToByteArray(imgFoto)
            if(checkInput(textInputNamaToko) && checkInput(textInputAlamat) && checkInputUsername(textInputUsernameAdmin) && checkInputPassword(textInputPasswordAdmin)){
                if(user?.role.equals(User.userSysAdmin)){
                    val profile = SugarRecord.listAll(Profile::class.java).firstOrNull()
                    if (profile != null) {
                        profile.namaToko = textInputNamaToko.editText?.text.toString()
                        profile.alamatToko = textInputAlamat.editText?.text.toString()
                        profile.logoToko = Base64.encodeToString(bitmapdata, Base64.DEFAULT)
                        profile.save()


                        Toast.makeText(this, "Profil berhasil diubah!", Toast.LENGTH_SHORT).show()
                    }
                    user?.username = textInputUsernameAdmin.editText?.text.toString()
                    user?.password = textInputPasswordAdmin.editText?.text.toString()
                    user?.save()
                }else{
                    user?.foto = Base64.encodeToString(bitmapdata,Base64.DEFAULT)
                    user?.nama = textInputNamaToko.editText?.text.toString()
                    user?.kontak = textInputAlamat.editText?.text.toString()
                    user?.username = textInputUsernameAdmin.editText?.text.toString()
                    user?.password = textInputPasswordAdmin.editText?.text.toString()
                    user?.save()
                }
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Pilih dari Galeri", "Ambil Foto")
        AlertDialog.Builder(this)
            .setTitle("Pilih Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
            .show()
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageView.setImageURI(imageUri)
        }
    }

    private fun pickImageFromGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (checkPermission(permission)) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        } else {
            requestPermission(permission, REQUEST_GALLERY_PERMISSION)
        }
    }

    private fun takePhotoFromCamera() {
        if (checkPermission(Manifest.permission.CAMERA)) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "New Picture")
                put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            }
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            cameraLauncher.launch(imageUri!!)
        } else {
            requestPermission(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showPermissionExplanationDialog(permission, requestCode)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    private fun showPermissionExplanationDialog(permission: String, requestCode: Int) {
        AlertDialog.Builder(this)
            .setTitle("Izin Diperlukan")
            .setMessage("Aplikasi memerlukan izin ini untuk berfungsi dengan baik. Silakan izinkan akses.")
            .setPositiveButton("Izinkan") { _, _ ->
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_GALLERY_PERMISSION -> pickImageFromGallery()
                REQUEST_CAMERA_PERMISSION -> takePhotoFromCamera()
            }
        } else {
            showPermissionDeniedDialog()
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Izin Ditolak")
            .setMessage("Akses ditolak. Anda dapat mengaktifkan izin di Pengaturan.")
            .setPositiveButton("Buka Pengaturan") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    fun initProfile(user:User?) {
        val profile = SugarRecord.listAll(Profile::class.java).firstOrNull()

        if (profile != null) {
            if(user != null){
                if(user.role.equals(User.userSysAdmin)){
                    if (profile.logoToko != null) {
                        Glide.with(this).load(Base64.decode(profile.logoToko, Base64.DEFAULT)).fitCenter()
                            .into(imgFoto)
                    }
                    textInputAlamat.hint = "Alamat"
                    textInputNamaToko.hint = "Nama Toko"
                    textInputAlamat.editText?.setText(profile.alamatToko)
                    textInputNamaToko.editText?.setText(profile.namaToko)

                }else{
                    textInputAtasNamaToko.visibility = View.INVISIBLE
                    textInputNoRekeningToko.visibility = View.INVISIBLE

                    if (user.foto != null) {
                        Glide.with(this).load(Base64.decode(user.foto, Base64.DEFAULT)).fitCenter()
                            .into(imgFoto)
                    }else{
                        imgFoto.setImageDrawable(getDrawable(R.drawable.admin))
                    }

                    textInputNamaToko.hint = "Nama Pegawai"
                    textInputAlamat.hint = "Kontak"
                    textInputAlamat.editText?.maxLines = 1
                    textInputAlamat.editText?.inputType = InputType.TYPE_CLASS_PHONE
                    textInputNamaToko.editText?.setText(user.nama)
                    textInputAlamat.editText?.setText(user.kontak)
                }
                textInputUsernameAdmin.editText?.setText(user.username)
                textInputPasswordAdmin.editText?.setText(user.password)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(currentFocus != null){
            hideSoftKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        private const val REQUEST_GALLERY_PERMISSION = 101
        private const val REQUEST_CAMERA_PERMISSION = 102
    }

}
package com.svtech.dhuwit.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.widget.Toast
import com.bumptech.glide.Glide
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import kotlinx.android.synthetic.main.activity_add_karyawan.*
import java.util.*

class AddKaryawanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_karyawan)

        /*Setting tombol back dan title*/
        setToolbar(this, "Tambah Pegawai")

        btnLoadImage.setOnClickListener {
            /*Membuka galeri*/
            pickImage(this, imgFoto,"Pegawai")
        }

        val update = intent.getBooleanExtra("update", false)
        val idUser = intent.getLongExtra("user", -1)
        val user = SugarRecord.findById(User::class.java, idUser)
        btnSimpan.setOnClickListener {
            if (textInputPasswordAdmin.editText?.text.toString().length == 5 && checkInput(textInputNamaPegawai)  && checkInputUsername(
                    textInputUsernameAdmin
                ) && checkInputPassword(textInputPasswordAdmin)
            ) {
                if (update) {
                    /*Update karyawan*/
                    updateKaryawan(user)
                } else {
                    /*Insert karyawan*/
                    insertKaryawan()
                }
            } else {
                Toast.makeText(this, "Password Harus 5 Karakter", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

        }
        if (user != null) {
            Glide.with(this).load(Base64.decode(user.foto, Base64.DEFAULT)).fitCenter()
                .into(imgFoto)
            textInputNamaPegawai.editText?.setText(user.nama)
//            textInputKontak.editText?.setText(user.kontak)
            textInputUsernameAdmin.editText?.setText(user.username)
            textInputPasswordAdmin.editText?.setText(user.password)
        }
    }

    private fun insertKaryawan() {

        val byteArray = ImageViewToByteArray(imgFoto)
        val checkUser = SugarRecord.find(User::class.java, "username = ?", textInputUsernameAdmin.editText?.text.toString()).firstOrNull()
        if (checkUser == null) {
            User(
                nama = textInputNamaPegawai.editText?.text.toString(),
//                kontak = textInputKontak.editText?.text.toString(),
                username = textInputUsernameAdmin.editText?.text.toString(),
                password = textInputPasswordAdmin.editText?.text.toString(),
                role = User.userAdmin,
                foto = Base64.encodeToString(byteArray, Base64.DEFAULT)
            ).save()
            Toast.makeText(this, "Pegawai berhasil disimpan!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Username sudah terdaftar!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateKaryawan(user: User?) {
        val byteArray = ImageViewToByteArray(imgFoto)

        user?.nama = textInputNamaPegawai.editText?.text.toString()
//        user?.kontak = textInputKontak.editText?.text.toString()
        user?.username = textInputUsernameAdmin.editText?.text.toString()
        user?.password = textInputPasswordAdmin.editText?.text.toString()
        user?.foto = Base64.encodeToString(byteArray, Base64.DEFAULT)
        user?.save()
        Toast.makeText(this, "Pegawai berhasil disimpan!", Toast.LENGTH_SHORT).show()
        finish()

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideSoftKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }
}
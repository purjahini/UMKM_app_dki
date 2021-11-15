package com.svtech.dhuwit.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.Profile
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.checkInput
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        TvToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }



        btnDaftar.setOnClickListener {
            ProsesDaftar()
        }
    }

    private fun ProsesDaftar() {
        pbLoadingRegister.visibility = View.VISIBLE
        var tipeBolean = false
        var register = false
        var userNameToko = false

        if (textInputPassword.editText?.text.toString().length != 5) {
            pbLoadingRegister.visibility = View.GONE
            Toast.makeText(this, "Password Harus 5 Karakter", Toast.LENGTH_SHORT).show();
            return
        }

            if (textInputPassword.editText?.text.toString().length == 5 && checkInput(textInputNamaToko) && checkInput(
                textInputAlamatToko
            )
            && checkInput(textInputNamaPengguna) && checkInput(textInputUsername) && checkInput(
                textInputPassword
            )
        ) {
            tipeBolean = true
            pbLoadingRegister.visibility = View.GONE
        }
        val user = SugarRecord.find(
            User::class.java,
            "username = ?",
            textInputUsername.editText?.text.toString()
        ).firstOrNull()
        if (user != null) {
            pbLoadingRegister.visibility = View.GONE
            Toast.makeText(this, "Nomor handphone anda sudah terdaftar", Toast.LENGTH_SHORT).show();
            return

        } else {
            userNameToko = true
        }

        val randomUUID = UUID.randomUUID().toString()
        Log.d("REGISTER", "random uuid $randomUUID")
        if (randomUUID != null && tipeBolean == true && userNameToko == true) {
            Profile(
                kode = randomUUID,
                namaToko = textInputNamaToko.editText?.text.toString(),
                alamatToko = textInputAlamatToko.editText?.text.toString()
            ).save()

            User(
                nama = textInputNamaPengguna.editText?.text.toString(),
                username = textInputUsername.editText?.text.toString(),
                password = textInputPassword.editText?.text.toString(),
                role = User.userSysAdmin
            ).save()
            register = true


        } else {
            pbLoadingRegister.visibility = View.GONE
            Toast.makeText(this, "Register Gagal, Password harus 5 Karakter , Semua form tidak boleh kosong", Toast.LENGTH_LONG).show();
        }
        if (register == true) {
            pbLoadingRegister.visibility = View.GONE
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else {
            pbLoadingRegister.visibility = View.GONE
            Toast.makeText(this, "Register Gagal", Toast.LENGTH_SHORT).show();
        }

    }
}
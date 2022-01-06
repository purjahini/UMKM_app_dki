package com.svtech.dhuwit.Activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.Profile
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import kotlinx.android.synthetic.main.activity_edit_profile.*


class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        setToolbar(this, "Ubah Profil")
        /*Setting data profile*/
        val username = com.svtech.dhuwit.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER,"")!!
        val user = SugarRecord.find(User::class.java,"USERNAME = ?",username).firstOrNull()
        initProfile(user)


        btnLoadImage.setOnClickListener {
            /*Membuka galeri*/
            pickImage(this, imgFoto)
        }

        /*Logout*/
        btnLogOut.setOnClickListener {
            savePreferences(this,MyConstant.CURRENT_USER,"")
            val users = SugarRecord.find(User::class.java, "USERNAME =?",username).firstOrNull()
            See.log("respon user ${users}")
            users?.delete()

            val profil = SugarRecord.find(Profile::class.java,"USERNAME=?",username).firstOrNull()
            See.log("respon profil ${profil}")
            profil?.delete()

            startActivity(Intent(this,LoginActivity::class.java))
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

}
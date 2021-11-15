package com.svtech.dhuwit.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import com.orm.SugarRecord
import com.svtech.dhuwit.Models.User
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        TvToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        btnMasuk.setOnClickListener {
            if(checkInputUsername(textInputUsername) && checkInputPassword(textInputPassword)){
                val username = textInputUsername.editText?.text.toString().trim()
                val password = textInputPassword.editText?.text.toString().trim()
                val user = SugarRecord.find(User::class.java,"username = ? and password = ?", username, password).firstOrNull()
                if(user != null){
                    savePreferences(this,MyConstant.CURRENT_USER,user.id.toString())
                    startActivity(Intent(this,DashboardActivity::class.java))
                    finish()
                }else{
                    Toast.makeText(this, "Login gagal user / password salah!", Toast.LENGTH_SHORT).show()
                }

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
package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.orm.SugarRecord
import com.svtech.mandiri.Models.Profile
import com.svtech.mandiri.Models.User
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.deletePreferences
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.setToolbar
import com.svtech.mandiri.modelOnline.UserOnline
import kotlinx.android.synthetic.main.activity_dashboard.imgLogo
import org.json.JSONObject

class ProfilNewActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    var kontak = ""
    var id = 0
    var nama = ""
    var email = ""
    var foto = ""

    lateinit var imgFoto: ImageView
    lateinit var tvNama: TextView
    lateinit var tvUsernameUser: TextView
    lateinit var tvNohp: TextView
    lateinit var cvSetting: CardView
    lateinit var llAturAkun: LinearLayout
    lateinit var llAturToko: LinearLayout
    lateinit var llAturNoRek: LinearLayout
    lateinit var llHubKami: LinearLayout
    lateinit var llSyaratKetentuan: LinearLayout
    lateinit var llKebijakanPrivasi: LinearLayout
    lateinit var BtnKeluar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil_new)
        setToolbar(this, "Profil")

        tvNama = findViewById(R.id.tvNama)
        tvNohp = findViewById(R.id.tvNohp)
        tvUsernameUser = findViewById(R.id.tvUsernameUser)
        cvSetting = findViewById(R.id.cvSetting)
        llAturAkun = findViewById(R.id.llAturAkun)
        llAturToko = findViewById(R.id.llAturToko)
        llAturNoRek = findViewById(R.id.llAturNoRek)
        llHubKami = findViewById(R.id.llHubKami)
        llSyaratKetentuan = findViewById(R.id.llSyaratKetentuan)
        llKebijakanPrivasi = findViewById(R.id.llKebijakanPrivasi)
        BtnKeluar = findViewById(R.id.BtnKeluar)
        imgFoto = findViewById(R.id.imgFoto)

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

        loadUser(username, token, kontak)

        llAturToko.setOnClickListener {
            val intent = Intent(this, RegisterTokoActivity::class.java)
            intent.putExtra("update", true)

            startActivity(intent)

        }
        llAturAkun.setOnClickListener {
            val intent = Intent(this, UserProfilActivity::class.java)
            intent.putExtra("update", true)
            intent.putExtra(MyConstant.ID, id.toString())
            intent.putExtra(MyConstant.NAMA, nama)
            intent.putExtra(MyConstant.FOTO,foto)
            intent.putExtra(MyConstant.EMAIL,email)

            startActivity(intent)
        }
        llAturNoRek.setOnClickListener {
            val intent = Intent(this, WalletActivity::class.java)
            startActivity(intent)
        }
        llHubKami.setOnClickListener {

        }
        llSyaratKetentuan.setOnClickListener {

        }
        llKebijakanPrivasi.setOnClickListener {

        }

        BtnKeluar.setOnClickListener {
            if (username != null) {
                deletePreferences(this, username)
                See.log("username deletePref $username")
            }

            if (token != null) {
                deletePreferences(this, token)
                See.log("token deletePref $token")
            }

            if (kontak != null) {
                deletePreferences(this, kontak)
                See.log("kontak deletePref $kontak")
            }

            val users = SugarRecord.find(User::class.java, "USERNAME =?", username).firstOrNull()
            See.log("respon user ${users}")
            users?.delete()

            val profil = SugarRecord.find(Profile::class.java, "USERNAME=?", username).firstOrNull()
            See.log("respon profil ${profil}")
            profil?.delete()



            startActivity(Intent(this, WelcomeActivity::class.java))
            finishAffinity()
            finish()
        }


    }

    private fun loadUser(username: String, token: String, kontak: String) {
        progressDialog?.show()
        AndroidNetworking.post(MyConstant.Urlgetusercurrent)
            .addHeaders(MyConstant.AUTHORIZATION, MyConstant.BEARER + token)
            .addBodyParameter(MyConstant.USERNAME, username)
            .addBodyParameter(MyConstant.kontak, kontak)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    See.log("respon getLoginUser: \n $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus == 1) {
                        val data = Gson().fromJson(respon, UserOnline::class.java)
                        val list = data.data

                        if (list != null) {
                            Glide.with(this@ProfilNewActivity)
                                .load(list.foto)
                                .apply(RequestOptions.circleCropTransform())
                                .into(imgLogo)

                            id = list.id!!
                            nama = list.nama.toString()
                            email = list.email.toString()
                            foto = list.foto.toString()

                            tvNama.setText("Nama : " + list.nama)
                            tvNohp.setText("NoHp : " + list.kontak + " \nEmail: " + list.email)
                            tvUsernameUser.setText("Owner : " + list.username + "\nRole: " + list.role)

                            if (list.role == User.userSysAdmin) {
                                cvSetting.visibility = View.VISIBLE
                            } else {
                                cvSetting.visibility = View.GONE
                            }


                        }

                    } else {
                        if (apiMessage != null) {
                            See.toast(this@ProfilNewActivity, apiMessage)
                        }

                    }

                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage == MyConstant.FORBIDDEN) {
                        getToken(this@ProfilNewActivity)
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

    }
}
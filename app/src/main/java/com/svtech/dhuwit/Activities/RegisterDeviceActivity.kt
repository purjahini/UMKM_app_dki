package com.svtech.dhuwit.Activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.pattra.pattracardsdk.PattraCardConfig
import com.svtech.dhuwit.Adapter.RclvItemBank
import com.svtech.dhuwit.Models.DataBank
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.Cons
import com.svtech.dhuwit.Utils.See
import com.svtech.dhuwit.Utils.getDeviceId
import com.svtech.dhuwit.Utils.setToolbar
import com.svtech.dhuwit.modelOnline.ResponseConfig
import kotlinx.android.synthetic.main.activity_register_device.*
import org.json.JSONObject

class RegisterDeviceActivity : AppCompatActivity() {
    var pattraCardConfig: PattraCardConfig? = null
    var progressDialog : ProgressDialog? = null
    lateinit var rclvItemBank : RclvItemBank
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_device)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Loading...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        setToolbar(this, "Device Status")

        TvImei.text = ": "+getDeviceId(this)

        pattraCardConfig = PattraCardConfig(object : PattraCardConfig.ConfigCallback {
            override fun ConfigError(s: String) {
                See.log("Config Error $s")

            }

            override fun ConfigSuccess(person: JSONObject) {
                progressDialog!!.show()
                val respon = person.toString()
                See.log("ConfigSuccess $respon")
                val json = JSONObject(respon)
                val apiStatus = json.getBoolean(Cons.STATUS)
                val apiCode = json.getString(Cons.CODE)
                if (apiStatus && apiCode.equals("00")) {
                    progressDialog!!.dismiss()
                    val data = Gson().fromJson(respon, ResponseConfig::class.java)
                    See.log("response apitrue dan 00 ${data.message}")
                    TvMessageStatus.text = ": "+data.message

                    val listConfig = data.data_config
//                    Tvdevice_id.text = ": "+listConfig.device_id
//                    Tvip_sam.text = ": "+listConfig.ip_sam
//                    Tvport.text = ": "+listConfig.port

                    val listinfoUmum = data.info_umum

//                    Tvalamatlokasi.text = ": "+listinfoUmum.alamatlokasi
//                    Tvidhost.text = ": "+listinfoUmum.idhost
//                    Tvkodelokasi.text  = ": "+listinfoUmum.kodelokasi
//                    Tvlokasi.text = ": "+listinfoUmum.lokasi
//                    Tvpesankeluar.text = ": "+listinfoUmum.pesankeluar
//                    Tvpesanmasuk.text = ": "+listinfoUmum.pesanmasuk
//                    Tvshift.text = ": "+listinfoUmum.shift


                    var listLastTransaction = data?.data_bank

                    if (listLastTransaction != null){

                        rclvItemBank = RclvItemBank(
                            this@RegisterDeviceActivity,
                            listLastTransaction
                        )

                        RvBankConfig.apply {
                            adapter = rclvItemBank
                            layoutManager = GridLayoutManager(this@RegisterDeviceActivity,2)
                            setHasFixedSize(true)
                        }
                    }




                }

                if (apiStatus && apiCode.equals("11")) {
                    progressDialog!!.dismiss()
                    val data = Gson().fromJson(respon, ResponseConfig::class.java)
                    See.log("response apitrue dan 11 ${data.message}")
                    TvMessageStatus.text  = ": "+data.message
                }

                if (apiStatus == false && apiCode.equals("08")) {
                    progressDialog!!.dismiss()
                    val data = Gson().fromJson(respon, ResponseConfig::class.java)
                    See.log("response apifalse dan 08 ${data.message}")
                    TvMessageStatus.text  = ": "+data.message
                }


            }

            override fun LogSuccess(jsonObject: JSONObject) {
                val respon = jsonObject.toString()
                See.log("LogSuccess $respon")

            }

            override fun LogError(jsonObject: JSONObject) {
                val respon = jsonObject.toString()
                See.log("LogError $respon")
            }
        })

        pattraCardConfig!!.getConfig("203.210.87.98", getDeviceId(this))




    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
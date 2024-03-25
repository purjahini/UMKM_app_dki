package com.svtech.mandiri.Adapter

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.svtech.mandiri.Activities.DetailLaporanHarianActivity
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.numberToCurrency
import com.svtech.mandiri.modelOnline.ResponseTransaksi
import kotlinx.android.synthetic.main.activity_laporan_harian.*
import kotlinx.android.synthetic.main.layout_item_laporan_harian.view.*
import kotlinx.android.synthetic.main.layout_item_laporan_harian.view.btnDetail
import kotlinx.android.synthetic.main.layout_item_laporan_harian.view.tvTotalPendapatan
import org.json.JSONObject
import java.text.SimpleDateFormat

/*Adapter recycler view untuk menapilkan item laporan penjualan*/
class RclvLaporanHarian(var context: Context, var listTransaksi: MutableList<ResponseTransaksi.Data>) :
    RecyclerView.Adapter<RclvLaporanHarian.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var progressDialog: ProgressDialog? = null
        var token = ""
        var username = ""

        @SuppressLint("NewApi")
        @RequiresApi(Build.VERSION_CODES.N)
        fun bind(transaksi: ResponseTransaksi.Data, context:Context) {

            val date = SimpleDateFormat("YYYY-MM-DD hh:mm:ss").parse(transaksi.tanggal_transaksi)

            token =
                com.svtech.mandiri.Utils.getPreferences(context).getString(MyConstant.TOKEN, "").toString()
            username =
                com.svtech.mandiri.Utils.getPreferences(context).getString(MyConstant.CURRENT_USER, "")
                    .toString()
            See.log("token add Karyawan :  $token")
            progressDialog = ProgressDialog(context)
            progressDialog!!.setTitle("Proses")
            progressDialog!!.setMessage("Mohon Menunggu...")
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog!!.setCancelable(false)
            progressDialog!!.isIndeterminate = true
            AndroidNetworking.post(MyConstant.Urltransaksigetstatus)
                .addHeaders("Authorization", "Bearer$token")
                .addBodyParameter("status", "0")
                .addBodyParameter("username", username.trim())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        val respon = response?.toString()
                        See.log("respon get Transaksi Status : $respon")
                        val json = JSONObject(respon)
                        val apiStatus = json.getInt(MyConstant.API_STATUS)
                        val apiMessage = json.getString(MyConstant.API_MESSAGE)
                        if (apiStatus.equals(1)) {
                            val data = Gson().fromJson(respon, ResponseTransaksi::class.java)
                            val list = data.data
                            if (list != null) {


                                val iTransaksi = Gson().fromJson(respon, ResponseTransaksi::class.java).data.filter { l ->
                                    l.status == 0 && l.tanggal_transaksi?.substring(
                                        0,
                                        l.tanggal_transaksi?.indexOf(" ")!!
                                    ).equals(
                                        transaksi.tanggal_transaksi?.substring(
                                            0,
                                            transaksi.tanggal_transaksi?.indexOf(" ")!!
                                        )!!
                                    )
                                }
                                var total = 0.0
                                if (iTransaksi.isNotEmpty()) {
                                    for (item in iTransaksi) {
                                        total += item.total_pembayaran!!
                                    }
                                }
                                itemView.tvTglPenjualan.text = SimpleDateFormat("dd MMMM yyyy").format(date).toString()
                                itemView.tvTotalPendapatan.text = numberToCurrency(total)


                            }



                            progressDialog!!.dismiss()

                            See.toast(context, "Insert update Karyawan $apiMessage")

                        } else {
                            progressDialog!!.dismiss()
                            See.toast(context, "Insert update Karyawan $apiMessage")

                        }

                    }

                    override fun onError(anError: ANError?) {
                        progressDialog?.dismiss()
                        See.toast(
                            context,
                            "Error Code update karyawan ${anError?.errorCode}"
                        )
                        See.log("onError update karyawan errorCode trx : ${anError?.errorCode}")
                        See.log("onError update karyawan errorBody trx: ${anError?.errorBody}")
                        See.log("onError update karyawan errorDetail trx: ${anError?.errorDetail}")
                    }

                })



        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_laporan_harian, parent, false)
        )
    }

    override fun getItemCount(): Int = listTransaksi.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaksi = listTransaksi[position]
        holder.bind(transaksi, context)
        holder.itemView.btnDetail.setOnClickListener {
            val inten = Intent(context, DetailLaporanHarianActivity::class.java)
            inten.putExtra(
                "tanggal",
                transaksi.tanggal_transaksi?.substring(0, transaksi.tanggal_transaksi?.indexOf(" ")!!)
            )
            context.startActivity(inten)
        }

//        holder.itemView.btnSavePdf.setOnClickListener {
//            (context as LaporanHarianActivity).savePDF(transaksi)
//        }
    }
}
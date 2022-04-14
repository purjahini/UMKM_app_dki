package com.svtech.dhuwit.AdapterOnline

import android.app.ProgressDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.svtech.dhuwit.Activities.*

import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.*
import com.svtech.dhuwit.modelOnline.ResponseDetailInvoice
import com.svtech.dhuwit.modelOnline.ResponseTransaksiTodays
import kotlinx.android.synthetic.main.activity_menu_tambah_kategori.*
import kotlinx.android.synthetic.main.layout_item_lap_detail.view.*
import org.json.JSONObject


/*Adapter recycler view untuk menu dashboard*/
class RclvLaporanTodaysDetail(val context: Context, var listItemMenu: List<ResponseTransaksiTodays.Data>) :
    RecyclerView.Adapter<RclvLaporanTodaysDetail.ViewHolder>() {




    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun onBind(dataTodays: ResponseTransaksiTodays.Data, context: Context) {
            itemView.tvTotal.text = numberToCurrency(dataTodays.total.toInt())
            itemView.tvTanggal.text = dataTodays.tanggal
            itemView.tvTrx.text = dataTodays.jumlah_trx+" Invoice"
            var nilai = 0
            itemView.llTodaysDetail.setOnClickListener {
                if (nilai.equals(0)) {
                    itemView.rclvLapTodaysDetailInvoice.visibility = View.VISIBLE
                    itemView.TvDetailInvoiceBtn.animate().rotation(180F).start()
                    nilai = 1
                    getDataInvoice(dataTodays.tanggal)

                } else {
                    itemView.rclvLapTodaysDetailInvoice.visibility = View.GONE
                    itemView.TvDetailInvoiceBtn.animate().rotation(0F).start()
                    nilai = 0
                }

            }
        }

        fun getDataInvoice(tanggal: String) {
            var progressDialog: ProgressDialog? = null
            var token = ""
            var username = ""
            progressDialog = ProgressDialog(itemView.context)
            progressDialog!!.setTitle("Proses")
            progressDialog!!.setMessage("Mohon Menunggu...")
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog!!.setCancelable(false)
            progressDialog!!.isIndeterminate = true

            token =
                com.svtech.dhuwit.Utils.getPreferences(itemView.context).getString(MyConstant.TOKEN, "").toString()
            username =
                com.svtech.dhuwit.Utils.getPreferences(itemView.context).getString(MyConstant.CURRENT_USER, "")
                    .toString()
            See.log("token getlapTodayDetailInvoice : $token tanggalInvoice : ${tanggal}")
            progressDialog?.show()
            AndroidNetworking.post(MyConstant.Urllaporantodaydetail)
                .addHeaders(MyConstant.AUTHORIZATION , MyConstant.BEARER+token)
                .addBodyParameter(MyConstant.USERNAME, username)
                .addBodyParameter("tanggal_transaksi", tanggal)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object :JSONObjectRequestListener{
                    override fun onResponse(response: JSONObject?) {
                        progressDialog?.dismiss()
                        val respon = response?.toString()
                        See.log("response lapInvoice $respon")
                        val json = JSONObject(respon)
                        val apiMessage = json.getString(Cons.API_MESSAGE)
                        val apiStatus = json.getInt(Cons.API_STATUS)
                        if (apiStatus.equals(1)) {
                            val data = Gson().fromJson(respon, ResponseDetailInvoice::class.java)


                            val rclvadapter = RclvLaporanTodaysDetailInvoice(itemView.context,
                                data.data as MutableList<ResponseDetailInvoice.Data>
                            )
                            itemView.rclvLapTodaysDetailInvoice.apply {
                                adapter = rclvadapter
                                layoutManager = LinearLayoutManager(itemView.context)
                                setHasFixedSize(true)
                            }

                        } else {
                            See.toast(itemView.context, "response api message $apiMessage")
                        }

                    }

                    override fun onError(anError: ANError?) {
                        progressDialog?.dismiss()
                        See.log("response error ${anError?.errorCode}")
                        See.toast(itemView.context, "Mohon check Koneksi internet anda ... Error  Code ${anError?.errorCode}")
                    }

                })

        }



    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RclvLaporanTodaysDetail.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_lap_detail, parent, false)
        )
    }

    override fun getItemCount(): Int = listItemMenu.size

    override fun onBindViewHolder(holder: RclvLaporanTodaysDetail.ViewHolder, position: Int) {
        val item = listItemMenu[position]
        holder.onBind(item, context)
        var nilai = 0
        holder.itemView.setOnClickListener {
            if (nilai.equals(0)) {

                nilai = 1



            } else {

                nilai = 0
            }
        }


    }


}
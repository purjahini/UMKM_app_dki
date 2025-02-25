package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.Gson
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.getToken
import com.svtech.mandiri.Utils.numberToCurrency
import com.svtech.mandiri.Utils.setToolbar
import com.svtech.mandiri.modelOnline.ResponseKeuangan
import org.json.JSONObject

class KeunganNewActivity : AppCompatActivity() {
    private lateinit var btnToday: TextView
    private lateinit var btnYesterday: TextView
    private lateinit var btnWeek: TextView
    private lateinit var btnMonth: TextView
    private lateinit var tvTotalKeuangan: TextView
    private lateinit var barChart: BarChart
    private lateinit var tvTotalTransaksi: TextView
    private lateinit var tvTransaksiPending: TextView
    private lateinit var tvTotalTransaksiDetail: TextView
    private lateinit var tvTransaksiPendingDetail: TextView
    private lateinit var llChart: LinearLayout

    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_keungan_new)
        setToolbar(this, "Keuangan")

        token =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.TOKEN, "").toString()

        username =
            com.svtech.mandiri.Utils.getPreferences(this).getString(MyConstant.CURRENT_USER, "")
                .toString()
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Proses")
        progressDialog!!.setMessage("Mohon Menunggu...")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCancelable(false)
        progressDialog!!.isIndeterminate = true

        // Inisialisasi ID
        btnToday = findViewById(R.id.btn_today)
        btnYesterday = findViewById(R.id.btn_yesterday)
        btnWeek = findViewById(R.id.btn_week)
        btnMonth = findViewById(R.id.btn_month)
        tvTotalKeuangan = findViewById(R.id.tv_total_keuangan)
        barChart = findViewById(R.id.barChart)
        tvTotalTransaksi = findViewById(R.id.tv_total_transaksi)
        tvTransaksiPending = findViewById(R.id.tv_transaksi_pending)
        tvTotalTransaksiDetail = findViewById(R.id.tv_total_transaksi_detail)
        tvTransaksiPendingDetail = findViewById(R.id.tv_transaksi_pending_detail)
        llChart = findViewById(R.id.llchart)

        btnToday.setOnClickListener { setActiveFilter(btnToday, "today") }
        btnYesterday.setOnClickListener { setActiveFilter(btnYesterday, "yesterday") }
        btnWeek.setOnClickListener { setActiveFilter(btnWeek, "week") }
        btnMonth.setOnClickListener { setActiveFilter(btnMonth, "month") }

        FetchDataFromServer("today")


    }

    private fun setActiveFilter(activeButton: TextView, filter: String) {
        // Daftar semua tombol filter
        val buttons = listOf(btnToday, btnYesterday, btnWeek, btnMonth)

        // Atur warna untuk setiap tombol
        for (button in buttons) {
            if (button == activeButton) {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.primary)) // Warna aktif
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundColor(Color.LTGRAY) // Warna abu-abu untuk tombol yang tidak aktif
                button.setTextColor(Color.BLACK)
            }
        }

            FetchDataFromServer(filter)
    }

    private fun FetchDataFromServer(filter: String) {
        if (filter == "today") {
            btnToday.setBackgroundColor(ContextCompat.getColor(this, R.color.primary)) // Warna aktif
            btnToday.setTextColor(Color.WHITE)
        } else {
            btnToday.setBackgroundColor(Color.LTGRAY) // Warna abu-abu untuk tombol yang tidak aktif
            btnToday.setTextColor(Color.BLACK)
        }
        progressDialog?.show()

        AndroidNetworking.post(MyConstant.Urlkeuangan)
            .addHeaders(MyConstant.AUTHORIZATION,MyConstant.BEARER+token)
            .addBodyParameter(MyConstant.USERNAME,username)
            .addBodyParameter(MyConstant.FILTER,filter)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object :JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    See.log("respon $this: \n $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(MyConstant.API_STATUS)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiStatus == 1) {
                        val data = Gson().fromJson(respon, ResponseKeuangan::class.java)
                        if (data != null) {

                            tvTotalKeuangan.setText(numberToCurrency(data.transaksi_sukses))
                            tvTotalTransaksi.setText(numberToCurrency(data.transaksi_sukses))
                            tvTransaksiPending.setText(numberToCurrency(data.transaksi_pending))
                            tvTotalTransaksiDetail.setText(numberToCurrency(data.transaksi_suskes_hari_ini))
                            tvTransaksiPendingDetail.setText(numberToCurrency(data.transaksi_pending_hari_ini))


                            /// Ambil data transaksi untuk chart
                            val transaksiData = data.data
                            val entriesTotal = ArrayList<BarEntry>()
                            val entriesPending = ArrayList<BarEntry>()
                            val entriesSukses = ArrayList<BarEntry>()
                            val labels = ArrayList<String>()

                            for (i in transaksiData.indices) {
                                val transaksi = transaksiData[i] // Ubah dari JSONObject ke Object langsung
                                val tanggal = transaksi.tanggal_transaksi
                                val total = transaksi.total_pembayaran
                                val trx_pending = transaksi.transaksi_pending
                                val trx_sukses = transaksi.transaksi_sukses

                                // Tambahkan data ke BarChart
                                entriesTotal.add(BarEntry(i.toFloat(), total.toFloat()))
                                entriesSukses.add(BarEntry(i.toFloat(), trx_sukses.toFloat()))
                                entriesPending.add(BarEntry(i.toFloat(),trx_pending.toFloat()))
                                labels.add(tanggal)
                            }

                            // Set data ke BarChart
//                            setupBarChart(entries, labels)
                            setupBarChart(entriesTotal,entriesPending,entriesSukses,labels)

                        }

                    } else {
                        if (apiMessage != null) {
                            See.toast(this@KeunganNewActivity, apiMessage)
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    val json = JSONObject(anError?.errorBody)
                    val apiMessage = json.getString(MyConstant.API_MESSAGE)
                    if (apiMessage == MyConstant.FORBIDDEN) {
                        getToken(this@KeunganNewActivity)
                    }

                    See.log("onError getProduk errorCode : ${anError?.errorCode}")
                    See.log("onError getProduk errorBody : ${anError?.errorBody}")
                    See.log("onError getProduk errorDetail : ${anError?.errorDetail}")
                }

            })

    }

    private fun setupBarChart(entriesTotal: ArrayList<BarEntry>, entriesPending: ArrayList<BarEntry>, entriesSukses: ArrayList<BarEntry>, labels: ArrayList<String>) {
//        val barDataSetTotal = BarDataSet(entriesTotal, "Total Pembayaran")
//        barDataSetTotal.color = ContextCompat.getColor(this, R.color.teal_700)

        val barDataSetPending = BarDataSet(entriesPending, "Transaksi Pending")
        barDataSetPending.color = ContextCompat.getColor(this, R.color.accent)

        val barDataSetSukses = BarDataSet(entriesSukses, "Transaksi Sukses")
        barDataSetSukses.color = ContextCompat.getColor(this, R.color.primary)

        val barData = BarData( barDataSetPending, barDataSetSukses)
        barData.setDrawValues(true)
        barData.barWidth = 0.4f // Lebar batang grafik

        // Konfigurasi BarChart
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.invalidate()

        // Konfigurasi sumbu X
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setLabelRotationAngle(45f)
        xAxis.setCenterAxisLabels(true) // Memastikan label berada di tengah kelompok batang

        // Konfigurasi sumbu Y
        val yAxis = barChart.axisLeft
        yAxis.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false

        // Atur posisi grup batang
        barChart.barData.barWidth = 0.3f
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = labels.size.toFloat()
        barChart.groupBars(0f, 0.2f, 0.02f) // (Start X, space antara grup, space antara batang)

        // Animasi
        barChart.animateY(1000, Easing.EaseInOutQuad)
    }

}
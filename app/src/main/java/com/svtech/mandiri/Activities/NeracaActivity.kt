package com.svtech.mandiri.Activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.components.XAxis
import com.svtech.mandiri.R
import com.svtech.mandiri.Utils.Cons
import com.svtech.mandiri.Utils.MyConstant
import com.svtech.mandiri.Utils.See
import com.svtech.mandiri.Utils.setToolbar
import com.svtech.mandiri.modelOnline.ResponseMenuNeraca
import org.json.JSONObject
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.transition.platform.MaterialSharedAxis.Axis
import com.google.gson.Gson
import com.svtech.mandiri.AdapterOnline.RclvNeraca
import com.svtech.mandiri.AdapterOnline.RclvUserOnline
import com.svtech.mandiri.Utils.AxisDateFormatters
import com.svtech.mandiri.Utils.LineChartMarkerView
import com.svtech.mandiri.Utils.numberToCurrency
import com.svtech.mandiri.modelOnline.ResponsePegawai
import kotlinx.android.synthetic.main.activity_menu_tambah_pegawai.rclvPenjualan
import kotlinx.android.synthetic.main.activity_neraca.RvNeraca
import kotlinx.android.synthetic.main.activity_neraca.lineChart
import kotlinx.android.synthetic.main.activity_neraca.tv_debet_month
import kotlinx.android.synthetic.main.activity_neraca.tv_debet_prev_month
import kotlinx.android.synthetic.main.activity_neraca.tv_kredit_month
import kotlinx.android.synthetic.main.activity_neraca.tv_kredit_prev_month
import java.text.SimpleDateFormat
import java.util.Calendar

class NeracaActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null
    var token = ""
    var username = ""
    lateinit var list: List<ResponseMenuNeraca.Data>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neraca)
        setToolbar(this, "Neraca")
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

        LoadNeraca()
    }

    private fun LoadNeraca() {
        progressDialog?.show()


        val calendar = Calendar.getInstance()

        // Tanggal hari ini
        val to = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)

        // Tanggal awal bulan ini
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val from = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)

//        See.log("url : ${MyConstant.Urlmenuneraca}, Authorization:Bearer${token}, username$username , to $to,from $from ")

        AndroidNetworking.post(MyConstant.Urlmenuneraca)
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "Bearer${token}")
            .addBodyParameter("username",username)
            .addBodyParameter("to",to)
            .addBodyParameter("from", from)
            .build()
            .getAsJSONObject(object :JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    progressDialog?.dismiss()
                    val respon = response?.toString()
                    See.Companion.log("response grafik : $respon")
                    val json = JSONObject(respon)
                    val apiStatus = json.getInt(Cons.API_STATUS)
                    val apiMessage = json.getString(Cons.API_MESSAGE)
                    if (apiStatus.equals(Cons.INT_STATUS)){
                        val dataS = Gson().fromJson(respon, ResponseMenuNeraca::class.java)
                        val historyNeraca = dataS.history_neraca
                        val debet_current = dataS.debet_current
                        val kredit_current = dataS.kredit_current
                        val debet_prev = dataS.debet_prev
                        val kredit_prev = dataS.kredit_prev

                        if (!kredit_current.isNullOrEmpty()) tv_kredit_month.setText(numberToCurrency(kredit_current.toInt())) else tv_kredit_month.setText(numberToCurrency(0))
                        if (!debet_current.isNullOrEmpty()) tv_debet_month.setText(numberToCurrency(debet_current.toInt()))  else tv_debet_month.setText(numberToCurrency(0))
                        if (!kredit_prev.isNullOrEmpty()) tv_kredit_prev_month.setText(numberToCurrency(kredit_prev.toInt())) else tv_kredit_prev_month.setText(numberToCurrency(0))
                        if (!debet_prev.isNullOrEmpty()) tv_debet_prev_month.setText(numberToCurrency(debet_prev.toInt())) else  tv_debet_prev_month.setText(numberToCurrency(0))




                        if (historyNeraca.isNotEmpty()) {
                            RvNeraca.apply {
                                adapter = RclvNeraca(this@NeracaActivity, historyNeraca)
                                layoutManager = LinearLayoutManager(this@NeracaActivity)
                                setHasFixedSize(true)
                            }
                        }



                        list = dataS.data
                        if (list.isNotEmpty()) {
                            val entriesLineChart: ArrayList<Entry> = ArrayList()
                            for (i in list!!.indices) {
                                val petugas = list[i]
                                entriesLineChart.add(Entry(i.toFloat(), petugas?.debet!!.toFloat()))
                            }

                            val lineDateSet = LineDataSet(entriesLineChart, "Diagram Line All Neraca")
                            lineDateSet.setColors(R.color.colorPrimary)
                            lineDateSet.fillAlpha = 65

                            lineDateSet.valueTextColor = ColorTemplate.COLOR_NONE
                            lineDateSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

                            val lineData = LineData(lineDateSet)
                            lineChart.data = lineData

                            var statusView = true

                            lineChart.setOnClickListener {
                                if (statusView) {
                                    lineDateSet.valueTextColor = ColorTemplate.rgb("#000000")
                                    statusView = false
                                } else {
                                    lineDateSet.valueTextColor = ColorTemplate.COLOR_NONE
                                    statusView = true
                                }

                            }

                                var date = ArrayList<String>();
                                for (a in list!!.indices) {
                                    val tanggal = list[a]
                                    date.add(tanggal?.tanggal.toString())
                                }

                                val tanggal = AxisDateFormatters(date.toArray(arrayOfNulls<String>(date.size)))
                                val marker : IMarker = LineChartMarkerView(this@NeracaActivity, lineChart,R.layout.item_maker_line_chart,tanggal)
                                lineChart.marker = marker



                                lineChart.setExtraOffsets(20F,20F, 20F, 40F)

                                //hide grid lines
                                lineChart.axisLeft.setDrawGridLines(true)
                                lineChart.xAxis.setDrawGridLines(false)
                                lineChart.xAxis.setDrawAxisLine(true)

                                //remove right y-axis
                                lineChart.axisRight.isEnabled = false

                                //remove legend
                                lineChart.legend.isEnabled = false

                                // enable touch gestures
                                lineChart.setTouchEnabled(true)
                                // enable scaling and dragging
                                lineChart.setDragEnabled(true)
                                lineChart.setScaleEnabled(true)
                                // if disabled, scaling can be done on x- and y-axis separately
                                lineChart.setPinchZoom(false)

                                lineChart.setDrawBorders(true)

                                //remove description label
                                lineChart.description.isEnabled = false


                                //add animation
                                lineChart.animateXY(3000, 3000);

                                val xAxisLine: XAxis = lineChart.xAxis
                                xAxisLine.setDrawGridLines(false)
                                xAxisLine.setDrawAxisLine(false)



//                         to draw label on xAxis
                                xAxisLine.position = XAxis.XAxisPosition.BOTTOM
                                lineChart.xAxis?.setValueFormatter(tanggal);
                                xAxisLine.setDrawLabels(true)
                                xAxisLine.granularity = 1F
                                xAxisLine.labelRotationAngle = +45F

                                //draw chart
                                lineChart.invalidate()


                        } else {
                            See.toast(this@NeracaActivity, apiMessage)
                        }



                    } else {
                        See.toast(this@NeracaActivity, apiMessage)

                    }



                }

                override fun onError(anError: ANError?) {
                    progressDialog?.dismiss()
                    See.toast(this@NeracaActivity,
                        "Mohon Check Koneksi Internet Anda.. \nCode Error :  ${anError?.errorCode}"
                    )
                    See.log("anError NeracaActivity errorCode : ${anError?.errorCode}")
                    See.log("anError NeracaActivity errorBody : ${anError?.errorBody}")
                    See.log("anError NeracaActivity errorDetail : ${anError?.errorDetail}")

                }

            })

    }
}
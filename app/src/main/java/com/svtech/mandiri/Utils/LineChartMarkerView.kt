package com.svtech.mandiri.Utils

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.svtech.mandiri.R

class LineChartMarkerView(
    context: Context?,
    private val lineChart: LineChart,
    layoutResource: Int,
    axisX: AxisDateFormatters
) : MarkerView(context, layoutResource), IMarker {

    private val item1: TextView = findViewById(R.id.item1)
    private val Title: TextView = findViewById(R.id.txtTitle)
    private val XAxis: AxisDateFormatters = axisX

    override fun refreshContent(e: Entry, highlight: Highlight) {
        try {

            Title.text = XAxis.getFormattedValue(e.x).toString()
            val val1 = lineChart.data.getDataSetByIndex(0)
                .getEntryForXValue(e.x, Float.NaN, DataSet.Rounding.CLOSEST) as Entry
            item1.text = String.format("%,.0f", val1.y)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        super.refreshContent(e, highlight)
    }

    private var mOffset: MPPointF? = null
    override fun getOffset(): MPPointF {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }
        return mOffset!!
    }
}
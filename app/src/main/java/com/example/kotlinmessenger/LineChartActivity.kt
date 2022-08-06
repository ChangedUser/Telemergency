package com.example.kotlinmessenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_main.*

class LineChartActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.line_chart)

        setLineChartData()
    }

    fun setLineChartData()
    {
        /*val xvalues = ArrayList<String>();
        xvalues.add("10:00am")
        xvalues.add("11:00am")
        xvalues.add("1:00pm")
        xvalues.add("7:00pm")
        xvalues.add("10:00pm")
*/
        val lineentry = ArrayList<Entry>();
        lineentry.add(Entry(0f,20f))
        lineentry.add(Entry(1f,50f))
        lineentry.add(Entry(2f,60f))
        lineentry.add(Entry(3f,30f))
        lineentry.add(Entry(4f,10f))

        val linedataset = LineDataSet(lineentry, "First")

        linedataset.color = resources.getColor(R.color.darkorange)
        linedataset.setDrawFilled(true)
        linedataset.fillColor=resources.getColor(R.color.purple_700)
        linedataset.fillColor=resources.getColor(R.color.common_google_signin_btn_text_light)
        linedataset.fillAlpha = 30


        val data = LineData(linedataset)

        lineChart.data = data
        lineChart.setBackgroundColor(resources.getColor(R.color.white))
        lineChart.animateXY(3000, 3000)

    /*

        var xvalues = ArrayList<LineEntry>()

        xvalues.add(BarEntry(1f,0f))
        xvalues.add(BarEntry(2f,0f))
        xvalues.add(BarEntry(3f,0f))
        xvalues.add(BarEntry(4f,0f))
        xvalues.add(BarEntry(5f,0f))
        xvalues.add(BarEntry(6f,0f))
        xvalues.add(BarEntry(7f,0f))*/

    }


}
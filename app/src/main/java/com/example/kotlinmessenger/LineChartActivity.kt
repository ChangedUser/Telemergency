package com.example.kotlinmessenger

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import com.example.kotlinmessenger.HourAxisValueFormatter
import kotlinx.android.synthetic.main.line_chart.view.*


class LineChartActivity: AppCompatActivity() {
    var mHandler: Handler? = null
    var min: Float = 0f

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.line_chart)
        mHandler = Handler()
        m_Runnable.run()

        getBpmData()

        //setLineChartData()

    }

    fun setLineChartData(lineentrybpm: ArrayList<Entry> )
    {
/*
        val lineentry = ArrayList<Entry>();
        lineentry.add(Entry(0f,20f))
        lineentry.add(Entry(1f,50f))
        lineentry.add(Entry(2f,60f))
        lineentry.add(Entry(3f,30f))
        lineentry.add(Entry(4f,10f))
        */

        val linedataset = LineDataSet(lineentrybpm, "First")

        linedataset.color = resources.getColor(R.color.darkorange)
        linedataset.setDrawFilled(true)
        linedataset.fillColor=resources.getColor(R.color.purple_700)
        linedataset.fillColor=resources.getColor(R.color.common_google_signin_btn_text_light)
        linedataset.fillAlpha = 30


        val data = LineData(linedataset)

        lineChart.data = data
        var xAxis :XAxis = lineChart.xAxis
        lineChart.setBackgroundColor(resources.getColor(R.color.white))
        xAxis.valueFormatter = (HourAxisValueFormatter(min.toLong()))

        var mv : MyMarkerView  = MyMarkerView(this, R.layout.marker_view, min.toLong())
        lineChart.marker = mv

        //lineChart.animateXY(3000, 3000)
        lineChart.invalidate()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBpmData()
    {
        val db = FirebaseFirestore.getInstance()
        var lineentry = ArrayList<Entry>();
        var xvals = ArrayList<Float>();
/*
        lineentry.add(Entry(0f,20f))
        lineentry.add(Entry(1f,50f))
        lineentry.add(Entry(2f,60f))
        lineentry.add(Entry(3f,30f))
        lineentry.add(Entry(4f,10f))*/

        //      val list= mutableListOf<String>()

        var fl : Float = 0f


        db.collection("BpMs")
            .get()
            .addOnSuccessListener {

                result ->
                for (document in result) {

                                        var bpmlive: String? = null

                                        bpmlive = document["bpmlive"].toString().trim()

                                        if(bpmlive.isNullOrBlank() or bpmlive.equals("null")) {
                                                    }
                                        else
                                                    {

                                                    var chunks = bpmlive.split("|")
                                                        for (chunk in chunks) {
                                                            println(chunk)
                                                        }


                                                    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz")
                                                    var dat = LocalDateTime.parse(chunks[2], pattern)

                                                     //   lineentry.add(Entry(fl, fl))
                                                       // lineentry.add(Entry(fl, bpmlive.toFloat()))
                                                        xvals.add(dat.toEpochSecond(ZoneOffset.UTC).toFloat())
                                                        lineentry.add(Entry(dat.toEpochSecond(ZoneOffset.UTC).toFloat(), chunks[1].toFloat()))
                                                    fl += 1f
                                                    }//Log.d(TAG, "ID: ${id} BPMLIVE: ${bpmlive}")
                                        //Log.d(TAG, "${document.id} => ${document.data}")
                                         }

                                        min=getMinXValue(xvals)

                                        var j : Int =0
                                        for(j in lineentry.indices)
                                                {
                                                lineentry.get(j).x=lineentry.get(j).x-min;
                                                }


                                        setLineChartData(lineentry)

                                    }

            .addOnFailureListener { exception ->
                                     Log.d(TAG, "Error getting documents: ", exception)
                                  }


    }

    private fun getMinXValue(xvals: ArrayList<Float>): Float {
        var i : Int = 0
        var m : Float = 0f

        while (i < xvals.size)
                        {
                        if(i==0)
                            m=xvals.get(0)
                        else if (xvals.get(i)<m)
                            m=xvals.get(i)
                        i++;
                        }
        return m
    }

    private val m_Runnable: Runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            //Toast.makeText(this@LineChartActivity, "in runnable", Toast.LENGTH_SHORT).show()
            getBpmData()
            this@LineChartActivity.mHandler?.postDelayed(this, 20000)
        }
    }
}
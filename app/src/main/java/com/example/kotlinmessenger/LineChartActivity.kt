package com.example.kotlinmessenger

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.line_chart.*
import kotlinx.android.synthetic.main.line_chart.view.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class LineChartActivity: AppCompatActivity() {
    var mHandler: Handler? = null
    var min: Float = 0f

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        val userRole = sharePref.getString("role", "defaultRole")!!
        if (userRole.toString() == "Patient") {
            setTheme(R.style.Theme_TelemergencyPatient)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.line_chart)
        mHandler = Handler()
        m_Runnable.run()

        getBpmData()

        //setLineChartData()

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
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

        val linedataset = LineDataSet(lineentrybpm, "BpM")

        linedataset.color = resources.getColor(R.color.darkorange)
        linedataset.setDrawFilled(true)
        linedataset.fillColor=resources.getColor(R.color.purple_700)
        linedataset.fillColor=resources.getColor(R.color.common_google_signin_btn_text_light)
        linedataset.fillAlpha = 30


        val data = LineData(linedataset)

        lineChart.data = data
        lineChart.setBackgroundColor(resources.getColor(R.color.white))
        //var valueFormatter = (HourAxisValueFormatter(min.toLong()))
        lineChart.xAxis.valueFormatter = LineChartXAxisValueFormatter(min.toLong())

        /*var mv : MyMarkerView  = MyMarkerView(this, R.layout.marker_view, min.toLong())
        lineChart.marker = mv */
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
                        else
                            {}
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
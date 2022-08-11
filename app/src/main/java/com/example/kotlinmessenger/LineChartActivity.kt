package com.example.kotlinmessenger

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern


class LineChartActivity: AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.line_chart)

        setLineChartData()

    }

    fun setLineChartData()
    {

        val lineentrybpm = getBpmData()
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
        lineChart.setBackgroundColor(resources.getColor(R.color.white))
        lineChart.animateXY(3000, 3000)
        lineChart.invalidate()


    }

    fun getBpmData() :ArrayList<Entry>
    {
        val db = FirebaseFirestore.getInstance()
        var lineentry = ArrayList<Entry>();
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


                                                    lineentry.add(Entry(fl, bpmlive.toFloat()))

                                                    fl += 1f
                                                    }//Log.d(TAG, "ID: ${id} BPMLIVE: ${bpmlive}")
                                        //Log.d(TAG, "${document.id} => ${document.data}")
                                         }



                                    }

            .addOnFailureListener { exception ->
                                     Log.d(TAG, "Error getting documents: ", exception)
                                  }


        return lineentry
    }


}
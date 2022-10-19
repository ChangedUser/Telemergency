package com.example.kotlinmessenger

import android.content.Context
import android.view.View
import com.github.mikephil.charting.components.MarkerView
import android.widget.TextView
import com.example.kotlinmessenger.R
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MyMarkerView(context: Context?, layoutResource: Int, referenceTimestamp: Long) :
    MarkerView(context, layoutResource) {
    private val tvContent: TextView
    private val referenceTimestamp // minimum timestamp in your data set
            : Long
    private val mDataFormat: DateFormat
    private val mDate: Date

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight) {
        val currentTimestamp = e.x.toInt() + referenceTimestamp
        tvContent.text =
            e.y.toString() + "% at " + getTimedate(currentTimestamp) // set the entry-value as the display text
    }

    fun getXOffset(xpos: Float): Int {
        // this will center the marker-view horizontally
        return -(width / 2)
    }

    fun getYOffset(ypos: Float): Int {
        // this will cause the marker-view to be above the selected value
        return -height
    }

    private fun getTimedate(timestamp: Long): String {
        return try {
            mDate.time = timestamp * 1000
            mDataFormat.format(mDate)
        } catch (ex: Exception) {
            "xx"
        }
    }

    init {
        // this markerview only displays a textview
        tvContent = findViewById<View>(R.id.tvContent) as TextView
        this.referenceTimestamp = referenceTimestamp
        mDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        mDate = Date()
    }
}
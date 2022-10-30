package com.example.kotlinmessenger

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class LineChartXAxisValueFormatter ( private val referenceTimestamp: Long)
    : IndexAxisValueFormatter() {
    private val mDataFormat: DateFormat
    private val mDate: Date


    override fun getFormattedValue(value: Float): String? {

        // Convert float value to date string
        // Convert from seconds back to milliseconds to format time  to show to the user
        //val emissionsMilliSince1970Time = value.toLong() * 1000
        val emissionsMilliSince1970Time = referenceTimestamp+value.toLong()

        // Show time in local version
        /*val timeMilliseconds = Date(emissionsMilliSince1970Time)
        val dateTimeFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return dateTimeFormat.format(timeMilliseconds)*/

        return getHour(emissionsMilliSince1970Time)
}

    //@Override
    val decimalDigits: Int
        get() = 0

    private fun getHour(timestamp: Long): String {
        return try {
            mDate.time = timestamp * 1000
            mDataFormat.format(mDate)
        } catch (ex: Exception) {
            "xx"
        }
    }

    init {
        mDataFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        mDate = Date()
    }

}
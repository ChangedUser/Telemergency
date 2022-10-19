package com.example.kotlinmessenger

import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.AxisBase
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Yasir on 02/06/16.
 */
class HourAxisValueFormatter(  // minimum timestamp in your data set
    private val referenceTimestamp: Long
) : ValueFormatter() {
    private val mDataFormat: DateFormat
    private val mDate: Date

    /**
     * Called when a value from an axis is to be formatted
     * before being drawn. For performance reasons, avoid excessive calculations
     * and memory allocations inside this method.
     *
     * @param value the value to be formatted
     * @param axis  the axis the value belongs to
     * @return
     */
    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        // convertedTimestamp = originalTimestamp - referenceTimestamp
        val convertedTimestamp = value.toLong()

        // Retrieve original timestamp
        val originalTimestamp = referenceTimestamp + convertedTimestamp

        // Convert timestamp to hour:minute
        return getHour(originalTimestamp)
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
        mDataFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        mDate = Date()
    }
}
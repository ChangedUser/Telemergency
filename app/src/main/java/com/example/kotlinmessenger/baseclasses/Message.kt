package com.example.kotlinmessenger.baseclasses

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Timestamp

class Message(val fromId: String, val id: String, val text: String, val timestamp: Timestamp, val toId: String):
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        TODO("timestamp"),
        parcel.readString().toString()
    ) {
    }

    constructor(): this("", "", "", java.sql.Timestamp(0) , "")

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}
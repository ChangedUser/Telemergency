package com.example.kotlinmessenger.baseclasses

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class User(val uid : String, val username : String, val role : String, val name: String): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    constructor(): this("", "", "", "")

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(this.uid)
        dest?.writeString(this.username)
        dest?.writeString(this.role)
        dest?.writeString(this.name)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
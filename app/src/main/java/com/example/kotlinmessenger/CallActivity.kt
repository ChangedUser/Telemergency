package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.phone_start.*
import com.example.kotlinmessenger.webrtc.Constants
import com.example.kotlinmessenger.webrtc.RTCActivity

class CallActivity : AppCompatActivity() {

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_start)
        Constants.isIntiatedNow = true
        Constants.isCallEnded = true
        start_meeting.setOnClickListener {
            if (meeting_id.text.toString().trim().isNullOrEmpty()) {
                meeting_id.error = "Please enter meeting id"
            } else {
                db.collection("calls")
                        .document(meeting_id.text.toString())
                        .get()
                        .addOnSuccessListener {
                            if (it["type"]=="OFFER" || it["type"]=="ANSWER" || it["type"]=="END_CALL") {
                                meeting_id.error = "Please enter another meeting ID"
                            } else {
                                val intent = Intent(this@CallActivity, RTCActivity::class.java)
                                intent.putExtra("meetingID",meeting_id.text.toString())
                                intent.putExtra("isJoin",false)
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener {
                            meeting_id.error = "Please enter a new meeting ID"
                        }
            }
        }
        join_meeting.setOnClickListener {
            if (meeting_id.text.toString().trim().isNullOrEmpty())
                meeting_id.error = "Please enter meeting id"
            else {
                val intent = Intent(this@CallActivity, RTCActivity::class.java)
                intent.putExtra("meetingID",meeting_id.text.toString())
                intent.putExtra("isJoin",true)
                startActivity(intent)
            }
        }
    }
}
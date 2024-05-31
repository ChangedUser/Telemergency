package com.example.kotlinmessenger

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.phone_start.*
import com.example.kotlinmessenger.webrtc.Constants
import com.example.kotlinmessenger.webrtc.RTCActivity
import com.google.firebase.auth.FirebaseAuth

class CallActivity : AppCompatActivity() {

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        val userRole = sharePref.getString("role", "defaultRole")!!
        if (userRole.toString() == "Patient") {
            setTheme(R.style.Theme_TelemergencyPatient)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_start)
        Constants.isIntiatedNow = true
        Constants.isCallEnded = true
        start_meeting.setOnClickListener {

            if (userRole == "Patient") {
                Log.i("Patient Connection", userRole)
                if (meeting_id_end.text.toString().trim().isNullOrEmpty())
                    meeting_id_end.error = "Please enter meeting id"
                else {
                    val intent = Intent(this@CallActivity, RTCActivity::class.java)
                    intent.putExtra("meetingID",meeting_id_end.text.toString())
                    intent.putExtra("isJoin",false)
                    startActivity(intent)
                }
            } else if (userRole == "Healthcare Professional") {
                Log.i("Professional Connection", userRole)
                if (meeting_id_end.text.toString().trim().isNullOrEmpty()) {
                    meeting_id_end.error = "Please enter meeting id"
                } else {
                    db.collection("calls")
                        .document(meeting_id_end.text.toString())
                        .get()
                        .addOnSuccessListener {
                            if (it["type"]=="OFFER" || it["type"]=="ANSWER" || it["type"]=="END_CALL") {
                                meeting_id_end.error = "Please enter another meeting ID"
                            } else {
                                val intent = Intent(this@CallActivity, RTCActivity::class.java)
                                intent.putExtra("meetingID",meeting_id_end.text.toString())
                                intent.putExtra("isJoin",false)
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener {
                            meeting_id_end.error = "Please enter a new meeting ID"
                        }
                }

            }

        }
        join_meeting.setOnClickListener {
            if (meeting_id_end.text.toString().trim().isNullOrEmpty())
                meeting_id_end.error = "Please enter meeting id"
            else {
                val intent = Intent(this@CallActivity, RTCActivity::class.java)
                intent.putExtra("meetingID",meeting_id_end.text.toString())
                intent.putExtra("isJoin",true)
                startActivity(intent)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.menu_overview-> {
                val intent = Intent(this, OverviewPage::class.java)
                //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
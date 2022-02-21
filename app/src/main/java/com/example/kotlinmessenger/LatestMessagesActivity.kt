package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.kotlinmessenger.webrtc.RTCActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        verifyUserIsLoggedIn()
        emergency_button.setOnClickListener {
            val intent = Intent(this@LatestMessagesActivity, RequestEmergency::class.java)
            // intent.putExtra("meetingID",meeting_id.text.toString())
            // intent.putExtra("isJoin",false)
            startActivity(intent)
        }
    }

    //verify is user is logged in, otherwise go to register screen
    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
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
package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_overview_page.*

class OverviewPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview_page)

        button_chat.setOnClickListener {
            val intent = Intent(this, NewMessageActivity::class.java)
            startActivity(intent)
        }

        button_monitoring.setOnClickListener {
            val intent = Intent(this, OverviewPage::class.java)
            startActivity(intent)
        }

        button_profile.setOnClickListener {
            val intent = Intent(this, LatestMessagesActivity::class.java)
            startActivity(intent)
        }

        buttong_emergenncy.setOnClickListener {
            val intent = Intent(this, RequestEmergency::class.java)
            startActivity(intent)
        }
    }


}
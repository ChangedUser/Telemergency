package com.example.kotlinmessenger

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_overview_page.*
import java.util.ArrayList

class OverviewPage : AppCompatActivity() {
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        val userRole = sharePref.getString("role", "defaultRole")!!
        if (userRole.toString() == "Patient") {
            setTheme(R.style.Theme_TelemergencyPatient)
        }
        super.onCreate(savedInstanceState)

        val adapter = GroupAdapter<ViewHolder>()
        val adapterActive = GroupAdapter<ViewHolder>()
        setContentView(R.layout.activity_overview_page)
        setInterfaceForRole(userRole)


    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setInterfaceForRole(userRole: String) {
        if (userRole == "Patient") {
            // setTheme(R.style.Theme_TelemergencyPatient)
            button_chat.setOnClickListener {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            button_monitoring.visibility = View.VISIBLE
            button_monitoring.setOnClickListener {
                val intent = Intent(this, LineChartActivity::class.java)
                startActivity(intent)
            }

            button_profile.setOnClickListener {
                val intent = Intent(this, LatestMessagesActivity::class.java)
                startActivity(intent)
            }

            buttong_emergenncy.visibility = View.VISIBLE
            buttong_emergenncy.setOnClickListener {
                val intent = Intent(this, RequestEmergency::class.java)
                startActivity(intent)
            }
        }else if (userRole == "Healthcare Professional") {
            button_chat.setOnClickListener {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            button_profile.setOnClickListener {
                val intent = Intent(this, LatestMessagesActivity::class.java)
                startActivity(intent)
            }
        }

    }

}
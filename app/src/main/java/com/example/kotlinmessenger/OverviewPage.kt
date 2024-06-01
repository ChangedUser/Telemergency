package com.example.kotlinmessenger

import android.content.ClipData.Item
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_overview_page.*
import kotlinx.android.synthetic.main.emergency_call.form_address
import kotlinx.android.synthetic.main.emergency_call.form_allergies
import kotlinx.android.synthetic.main.emergency_call.form_blood
import kotlinx.android.synthetic.main.emergency_call.form_drugs
import kotlinx.android.synthetic.main.emergency_call.form_illnesses
import kotlinx.android.synthetic.main.emergency_call.form_name
import kotlinx.android.synthetic.main.emergency_call.form_phone

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
    override fun onStart() {
        super.onStart()
        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        val userRole = sharePref.getString("role", "defaultRole")!!
        updateNotification(userRole)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_overview, menu)
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

    private fun updateNotification(userRole: String) {
        val docRef = Firebase.firestore.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                var showNotification = true

                val all_map = document.data?.get("allergies") as? Map<String, String> ?: emptyMap()
                val ill_map = document.data?.get("illnesses") as? Map<String, String> ?: emptyMap()
                val drug_map = document.data?.get("drugs") as? Map<String, String> ?: emptyMap()

                if (all_map.isEmpty() &&
                    ill_map.isEmpty() &&
                    drug_map.isEmpty() &&
                    document.data?.get("bloodtype") == "" ) {
                    button_notification.isVisible = true
                }else {
                    button_notification.isVisible = false
                }
            }
    }

    private fun setInterfaceForRole(userRole: String) {
        if (userRole == "Patient") {
            updateNotification(userRole)

            button_notification.setOnClickListener {
                Toast.makeText(getApplicationContext(),"Please fill out missing profile information!",Toast.LENGTH_LONG).show();
            }

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
                val intent = Intent(this, ProfileActivity::class.java)
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
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }

    }

}
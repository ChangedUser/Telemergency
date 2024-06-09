package com.example.kotlinmessenger

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.example.kotlinmessenger.icdapi.ICDAPI
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_profile.address_field
import kotlinx.android.synthetic.main.activity_profile.birth_field
import kotlinx.android.synthetic.main.activity_profile.blood_type_field
import kotlinx.android.synthetic.main.activity_profile.geolocation
import kotlinx.android.synthetic.main.activity_profile.mail__field
import kotlinx.android.synthetic.main.activity_profile.medical_info_field
import kotlinx.android.synthetic.main.activity_profile.name__field
import kotlinx.android.synthetic.main.activity_profile.phone_field
import kotlinx.android.synthetic.main.activity_profile.role__field
import kotlinx.android.synthetic.main.activity_profile.show_allergies
import kotlinx.android.synthetic.main.activity_profile.show_drugs
import kotlinx.android.synthetic.main.activity_profile.show_illness
import kotlinx.android.synthetic.main.activity_profile.username_field
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.HashMap
import java.util.Random

class OnCallActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    // TODO - For a future impl. -> Make it a service that runs in the background
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = "notification_channel" // getString()
            val descriptionText =  "Notification Description" // getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("EMERGENCY_CHANNEL", name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun sendEmergencyInformation(emergencyid: String, fromID: String, toID: String, intent: Intent) {

        // Maybe one day add it to an emergency class converted ...
        var message = ""
        db.collection("emergencies").document(emergencyid).get().addOnSuccessListener { it ->
            message = "Patient: ${it.get("name").toString()}_n" +
                      "Phone: ${it.get("phone").toString()}_n" +
                      "Address: ${it.get("address").toString()}_n" +
                      "Blood Type: ${it.get("blood_type").toString()}_n" +
                      "Allergies: ${it.get("allergies").toString()}_n" +
                      "Illnesses: ${it.get("illnesses").toString()}_n" +
                      "Location: ${it.get("currentAddress").toString()}_n _n" +
                      " =================== _n" +
                      "What happened: ${it.get("happened").toString()}_n" +
                      "Anybody hurt: ${it.get("hurt").toString()}_n" +
                      "Care needed:: ${it.get("care").toString()}"

            val newChatMessage: MutableMap<String,Any> = HashMap()
            newChatMessage["imagePath"] = ""
            newChatMessage["fromId"] = fromID
            newChatMessage["toId"] = toID
            newChatMessage["text"] = message
            newChatMessage["timeStamp"] = System.currentTimeMillis()/1000
            db.collection("messages").add(newChatMessage).addOnSuccessListener { documentReference ->
                Log.d("ChatLogActivity", "DocumentSnapshot written with ID: ${documentReference.id}")
                // delete emergency?
                startActivity(intent)
            }
        }



    }


    private fun createDynamicButtons(name: String, whatHappened: String, id: String, layout: LinearLayout, emergencyid: String) {
        val tv_name = TextView(this@OnCallActivity).apply {
            text = name
            typeface = Typeface.create("sans-serif-bold", Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        val tv_happened = TextView(this@OnCallActivity).apply {
            text = whatHappened
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            gravity = Gravity.CENTER
        }
        val button = Button(this@OnCallActivity).apply {
            text = id
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        layout.addView(tv_name)
        layout.addView(tv_happened)
        layout.addView(button)

        button.setOnClickListener {
            // TODO Make db call in order to update the entry ...
            val random = Random()
            val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

            val sb = StringBuilder(20)
            for (i in 0 until 20) {
                val randomIndex = random.nextInt(characters.length)
                sb.append(characters[randomIndex])
            }


            db.collection("users").document(FirebaseAuth.getInstance().uid.toString()).get().addOnSuccessListener { document ->
                val intent = Intent(this, ChatLogActivity::class.java)
                // intent.putExtra("chatID", button.text.toString())
                val b = Bundle()
                b.putString("chatID", sb.toString())
                b.putString("patientID", id)
                b.putString("patientName", name)
                db.collection("emergencies").document(emergencyid).update("toId", FirebaseAuth.getInstance().uid,
                    "chatID", sb.toString(),
                                      "drName", document.get("name")
                ).addOnSuccessListener{
                    intent.putExtras(b)
                    sendEmergencyInformation(emergencyid, id,  FirebaseAuth.getInstance().uid.toString(), intent)
                }


            }.addOnFailureListener{e -> Log.e("ONCALL", "Failed to call doc ${FirebaseAuth.getInstance().uid.toString()}")}
        }

    }

    private fun refreshOnCall() {
        val pageLayout = findViewById<LinearLayout>(R.id.linearlayout)
        pageLayout.removeAllViews()
        val docRef = db.collection("emergencies")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty()) {
                pageLayout.removeAllViews()
                createNotificationChannel()
                var builder = NotificationCompat.Builder(this, "EMERGENCY_CHANNEL")
                    .setSmallIcon(R.drawable.tm_logo_transparent)
                    .setContentTitle("New Emergency!")
                    // .setContentText("A new emergency for user ${snap.data.get("name")}")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)



                for (snap in snapshot ){
                    Log.d("ONCALL", "Current data: ${snap.data.get("name")}")


                    if (snap.data.get("isNew") != null && snap.data.get("isNew") == true) {
                        Log.d("ONCALL", "Send Notifcation!")

                        with(NotificationManagerCompat.from(this)) {
                            // if (ActivityCompat.checkSelfPermission( this@OnCallActivity, Manifest.permission.POST_NOTIFICATION) != PackageManager.PERMISSION_GRANTED ) {
                            // // notificationId is a unique int for each notification that you must define.
                            notify(1, builder.build())
                        }

                        db.collection("emergencies").document(snap.id).update("isNew", false)

                    }

                    createDynamicButtons(snap.data.get("name").toString(),snap.data.get("happened").toString() , snap.data.get("callerID").toString(), pageLayout, snap.id)

                }

            } else {
                // oncall_noitems.visibility = View.VISIBLE
                // oncall_hasitems.visibility = View.INVISIBLE
                Log.d("ONCALL", "Current data is empty")
            }
        }
    }
    override fun onRestart() {
        super.onRestart()
        refreshOnCall()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_call)
        refreshOnCall()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.menu_overview-> {
                val intent = Intent(this, OverviewPage::class.java)
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

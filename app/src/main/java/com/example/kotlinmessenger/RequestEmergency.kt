package com.example.kotlinmessenger

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.kotlinmessenger.baseclasses.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_profile.address_field
import kotlinx.android.synthetic.main.activity_profile.birth_field
import kotlinx.android.synthetic.main.activity_profile.blood_type_field
import kotlinx.android.synthetic.main.activity_profile.illness_container
import kotlinx.android.synthetic.main.activity_profile.mail__field
import kotlinx.android.synthetic.main.activity_profile.name__field
import kotlinx.android.synthetic.main.activity_profile.phone_field
import kotlinx.android.synthetic.main.activity_profile.role__field
import kotlinx.android.synthetic.main.activity_profile.username_field
import kotlinx.android.synthetic.main.emergency_call.*
import java.util.ArrayList
import java.util.HashMap

class RequestEmergency: AppCompatActivity() {
    private var user = User() ?: null
    private var fromId = String()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        val userRole = sharePref.getString("role", "defaultRole")!!
        if (userRole.toString() == "Patient") {
            setTheme(R.style.Theme_TelemergencyPatient)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.emergency_call)
        user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        fromId = FirebaseAuth.getInstance().uid ?: ""
        loadInformation()

        if (this.intent.getStringExtra("drID") != null && this.intent.getStringExtra("drID").toString() != "") {
            // var userName = ""
            val id = this.intent.getStringExtra("drID").toString()
            val name = this.intent.getStringExtra("drName").toString()
            val userName = this.intent.getStringExtra("drUser").toString()
            var finName = userName
            if (name != "null") {
                finName = name
            }
            // Make sure that username is first checked before actual name
            // if (this.intent?.getStringExtra("drUser") != null && this.intent?.getStringExtra("drUser").toString() != "") {
            //     userName = this.intent.getStringExtra("drUser").toString()
            // }
            // if (this.intent?.getStringExtra("drName") != null && this.intent?.getStringExtra("drName").toString() != "") {
            //     userName = this.intent.getStringExtra("drName").toString()
            // }
            findViewById<TextView>(R.id.emergency_doc).visibility = View.VISIBLE
            findViewById<TextView>(R.id.emergency_doc).text = "Emergency Contacting: " + userName

            send_request_button.setOnClickListener {
                val name = form_name.text.toString()
                val phone = form_phone.text.toString()
                val address = form_address.text.toString()
                val blood = form_blood.text.toString()
                val allergies = form_allergies.text.toString()
                val illnesses = form_illnesses.text.toString()
                val currentAddress = form_current.text.toString()
                val someoneHurt = form_hurt.text.toString()
                val whatHappened = form_whatHappened.text.toString()
                val needed = form_needed.text.toString()
                performDirectedCall(name, phone, address, blood, allergies, illnesses, currentAddress, someoneHurt, whatHappened, needed, fromId, id, finName)
            }

            this.intent.removeExtra("drID")
            this.intent.removeExtra("drUser")
            this.intent.removeExtra("drName")

        }else {
            send_request_button.setOnClickListener {
                val name = form_name.text.toString()
                val phone = form_phone.text.toString()
                val address = form_address.text.toString()
                val blood = form_blood.text.toString()
                val allergies = form_allergies.text.toString()
                val illnesses = form_illnesses.text.toString()
                val currentAddress = form_current.text.toString()
                val someoneHurt = form_hurt.text.toString()
                val whatHappened = form_whatHappened.text.toString()
                val needed = form_needed.text.toString()
                // performSendMessage(name, phone, address, blood, allergies, illnesses, currentAddress, someoneHurt, whatHappened, needed, fromId, user?.uid ?: "")
                performEmergencyCall(name, phone, address, blood, allergies, illnesses, currentAddress, someoneHurt, whatHappened, needed, fromId, user?.uid ?: "")
            }
        }


    }

    private fun loadCheckboxInformation(map: Map<String, String>): String {
        var ret: String= ""
        var count = 0

        for (item in map){
            if (count == map.size -1) {
                ret += item.value.toString()
            }else {
                ret += item.value.toString() + ", "

            }
            count ++
        }

        return ret
    }

    private fun loadInformation() {
        val docRef = Firebase.firestore.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                this.form_name.setText(document.data?.get("name")?.toString().orEmpty())
                this.form_phone.setText(document.data?.get("phone")?.toString().orEmpty())
                this.form_address.setText(document.data?.get("address")?.toString().orEmpty())
                this.form_blood.setText(document.data?.get("bloodtype")?.toString().orEmpty())


                if (document.data?.get("allergies") != null) {
                    this.form_allergies.setText(loadCheckboxInformation(document.data?.get("allergies") as Map<String, String>))
                }
                if (document.data?.get("illnesses") != null) {
                    this.form_illnesses.setText(loadCheckboxInformation(document.data?.get("illnesses") as Map<String, String>))
                }
                if (document.data?.get("drugs") != null) {
                    this.form_drugs.setText(loadCheckboxInformation(document.data?.get("drugs") as Map<String, String>))
                }

            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, " DID NOT RECIEVE USER INFORMATION: ", exception)
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

    private fun performDirectedCall(name: String, phone: String, address: String, blood: String, allergies: String, illnesses: String,
                                     currentAddress: String, someoneHurt: String, whatHappened: String, needed: String, fromId: String, toId: String, drName: String) {


        // val emergency: MutableMap<String,Any> = HashMap()
        // if (fromId != "") emergency["callerID"] = fromId
        // if (name != "") emergency["name"] = name
        // if (address!= "") emergency["address"] = address
        // if (allergies != "") emergency["allergies"] = allergies
        // if (illnesses !=  "") emergency["illnesses"] = illnesses
        // if (blood !=  "") emergency["blood_type"] = blood
        // if (whatHappened !=  "") emergency["happened"] = whatHappened
        // if (someoneHurt !=  "") emergency["hurt"] = someoneHurt
        // if (needed !=  "") emergency["care"] = needed // is needed = care?
        // if (currentAddress != "") emergency["location"] = currentAddress
        // if (phone!= "") emergency["phone"] = phone
        // emergency["isNew"] = true
        // emergency["toId"] = ""

        val message = "Patient: ${name}_n" +
                "Phone: ${phone}_n" +
                "Address: ${address}_n" +
                "Blood Type: ${blood}_n" +
                "Allergies: ${allergies}_n" +
                "Illnesses: ${illnesses}_n" +
                "Location: ${currentAddress}_n _n" +
                " =================== _n" +
                "What happened: ${someoneHurt}_n" +
                "Anybody hurt: ${whatHappened}_n" +
                "Care needed:: ${needed}"

        val newChatMessage: MutableMap<String,Any> = HashMap()
        newChatMessage["imagePath"] = ""
        newChatMessage["fromId"] = fromId
        newChatMessage["toId"] = toId
        newChatMessage["text"] = message
        newChatMessage["timeStamp"] = System.currentTimeMillis()/1000
        db.collection("messages").add(newChatMessage).addOnSuccessListener { documentReference ->
            Log.d("ChatLogActivity", "DocumentSnapshot written with ID: ${documentReference.id}")
            // todo - might need this one? - addToActiveChats(fromId,toId)
            addToActiveChats(fromId,toId)
            addToActiveChats(toId,fromId)
            val intent = Intent(this, ChatLogActivity::class.java)
            val b = Bundle()
            b.putString("drUID", toId)
            b.putString("drName", drName)
            // b.putString("chatID", snapshot.data?.get("chatID").toString())
            intent.putExtras(b)
            intent.putExtra(NewMessageActivity.USER_KEY, user)
            startActivity(intent)
        }

    }

    private fun performEmergencyCall(name: String, phone: String, address: String, blood: String, allergies: String, illnesses: String,
                                   currentAddress: String, someoneHurt: String, whatHappened: String, needed: String, fromId: String, toId: String) {


        val emergency: MutableMap<String,Any> = HashMap()
        if (fromId != "") emergency["callerID"] = fromId
        if (name != "") emergency["name"] = name
        if (address!= "") emergency["address"] = address
        if (allergies != "") emergency["allergies"] = allergies
        if (illnesses !=  "") emergency["illnesses"] = illnesses
        if (blood !=  "") emergency["blood_type"] = blood
        if (whatHappened !=  "") emergency["happened"] = whatHappened
        if (someoneHurt !=  "") emergency["hurt"] = someoneHurt
        if (needed !=  "") emergency["care"] = needed // is needed = care?
        if (currentAddress != "") emergency["location"] = currentAddress
        if (phone!= "") emergency["phone"] = phone
        emergency["isNew"] = true
        emergency["toId"] = ""

        db.collection("emergencies")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .set(emergency, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("RequestEmergency", "Added Emergency")
                // TODO make the user wait until Dr connects ...
                // Toast.makeText(this, "Profile edited successfully", Toast.LENGTH_LONG).show()

                // findViewById<ScrollView>(R.id.emergencyscroll).(findViewById<LinearLayout>(R.id.emergencylayout))
                findViewById<ScrollView>(R.id.emergencyscroll).visibility = View.GONE
                findViewById<LinearLayout>(R.id.progresslayout).visibility = View.VISIBLE


                // Perform emergency request check
                val docRef = db.collection("emergencies").document(FirebaseAuth.getInstance().currentUser!!.uid)
                docRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        // Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    // We confirm that the emergency has been updated
                    if (snapshot != null && snapshot.data?.get("toId") != null && snapshot.data?.get("toId").toString().isNotEmpty()) {
                        val intent = Intent(this, ChatLogActivity::class.java)
                        val b = Bundle()
                        b.putString("drUID", snapshot.data?.get("toId").toString())
                        b.putString("drName", snapshot.data?.get("drName").toString())
                        b.putString("chatID", snapshot.data?.get("chatID").toString())
                        // b.putString("drRole", documents.documents.get(0).getString("role")) // might as well be obvious
                        intent.putExtras(b)
                        // TODO Cleanup loading screen + delete emergency
                        findViewById<ScrollView>(R.id.emergencyscroll).visibility = View.VISIBLE
                        findViewById<LinearLayout>(R.id.progresslayout).visibility = View.GONE

                        startActivity(intent)
                    }
                }

            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to register emergency: ${it.message}", Toast.LENGTH_LONG).show()
            }

    }

    private fun performSendMessage(name: String, phone: String, address: String, blood: String, allergies: String, illnesses: String,
    currentAddress: String, someoneHurt: String, whatHappened: String, needed: String, fromId: String, toId: String) {
        val text1 = "Patient: " + name
        val text2 = "Phone: " + phone
        val text3 = "Address: " + address
        val text4 = "Blood Type: " + blood
        val text5 = "Allergies: " + allergies
        val text6 = "Illnesses: " + illnesses
        val text7 = "Location: " + currentAddress
        val text8 = "Someone Hurt: " + someoneHurt
        val text9 = "What Happened: " + whatHappened
        val text10 = "Care Needed: " + needed
        val fullText = text1 + "_n" + text2 + "_n" + text3 + "_n" + text4 + "_n" + text5 + "_n" + text6 + "_n" + text7  + "_n" + text8 + "_n" + text9 + "_n" + text10

        val newChatMessage: MutableMap<String,Any> = HashMap()
        newChatMessage["imagePath"] = ""
        newChatMessage["fromId"] = fromId
        newChatMessage["toId"] = toId
        newChatMessage["text"] = fullText
        newChatMessage["timeStamp"] = System.currentTimeMillis()/1000
        db.collection("messages").add(newChatMessage).addOnSuccessListener { documentReference ->
            Log.d("ChatLogActivity", "DocumentSnapshot written with ID: ${documentReference.id}")
            addToActiveChats(fromId,toId)
            //addToActiveChats(fromId, toId)
            //addToActiveChats(toId, fromId)
            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_KEY, user)
            startActivity(intent)
        }

    }

    //add to the active chats of the user
    private fun addToActiveChats(fromId : String, toId: String) {
        //gets current user
        val docRef = db.collection("users").document(fromId)
        //find active chats of user, add the id if not existing already
        docRef.get().addOnSuccessListener { user ->
            val activeChats = user.data?.get("activeChats") as ArrayList<String>
            if(!(activeChats.contains(toId))) activeChats.add(toId)
            val data = hashMapOf("activeChats" to activeChats)
            db.collection("users").document(fromId)
                .set(data, SetOptions.merge())
        }
    }
}
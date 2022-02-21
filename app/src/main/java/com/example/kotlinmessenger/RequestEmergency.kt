package com.example.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.emergency_call.*

class RequestEmergency: AppCompatActivity() {
    private var user = User() ?: null
    private var fromId = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emergency_call)
        user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        fromId = FirebaseAuth.getInstance().uid ?: ""

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
            performSendMessage(name, phone, address, blood, allergies, illnesses, currentAddress, someoneHurt, whatHappened, needed, fromId, user?.uid ?: "")
        }
    }

    private fun performSendMessage(name: String, phone: String, address: String, blood: String, allergies: String, illnesses: String,
    currentAddress: String, someoneHurt: String, whatHappened: String, needed: String, fromId: String, toId: String) {
        val text1 = "Patient: " + name + "; " + phone + "; " + address + "; " + blood
        val text2 = "Allergies: " + allergies + "; Illnesses: " + illnesses
        val text3 = "Location: " + currentAddress + " with: " + someoneHurt
        val text4 = "Accident:" + whatHappened + "; Needed: "
        val firebaseDb = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages")
        val reference1 = firebaseDb.push()
        val reference2 = firebaseDb.push()
        val reference3 = firebaseDb.push()
        val reference4 = firebaseDb.push()

        val chatMessage1 = ChatLogActivity.ChatMessage(reference1?.key ?: "", text1, null, fromId, toId, System.currentTimeMillis() / 1000)
        val chatMessage2 = ChatLogActivity.ChatMessage(reference2?.key ?: "", text2, null, fromId, toId, System.currentTimeMillis() / 1000)
        val chatMessage3 = ChatLogActivity.ChatMessage(reference3?.key ?: "", text3, null, fromId, toId, System.currentTimeMillis() / 1000)
        val chatMessage4 = ChatLogActivity.ChatMessage(reference4?.key ?: "", text4, null, fromId, toId, System.currentTimeMillis() / 1000)
        reference1.setValue(chatMessage1)
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "Saved our chat message ${reference1.key}")
                //val intent = Intent(this, ChatLogActivity::class.java)
                //intent.putExtra(NewMessageActivity.USER_KEY, user)
                //startActivity(intent)
            }
        reference2.setValue(chatMessage2)
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "Saved our chat message ${reference2.key}")
                //val intent = Intent(this, ChatLogActivity::class.java)
                //intent.putExtra(NewMessageActivity.USER_KEY, user)
                //startActivity(intent)
            }
        reference3.setValue(chatMessage3)
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "Saved our chat message ${reference3.key}")
                //val intent = Intent(this, ChatLogActivity::class.java)
                //intent.putExtra(NewMessageActivity.USER_KEY, user)
                //startActivity(intent)
            }
        reference4.setValue(chatMessage4)
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "Saved our chat message ${reference4.key}")
                val intent = Intent(this, ChatLogActivity::class.java)
                intent.putExtra(NewMessageActivity.USER_KEY, user)
                startActivity(intent)
            }
    }
}
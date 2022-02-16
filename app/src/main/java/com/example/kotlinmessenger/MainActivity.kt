package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    //private lateinit var mDbRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseMessaging.getInstance().subscribeToTopic("bpmAlert")
        register_button_register.setOnClickListener {
            val email = email_edittext_registration.text.toString()
            val password = password_edittext_registration.text.toString()

            //check if email/password empty
            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter text in email/password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    // else if successful
                    val uid = it.result?.user?.uid ?: ""
                    Log.d("MainActivity", "Successfully created user with uid: $uid" )
                    saveUserToFirebaseDatabase(uid)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        already_have_account_text_view.setOnClickListener {
            //launch login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUserToFirebaseDatabase(uid : String){
        val user = User(uid, username_edittext_registration.text.toString())
        val mDbRef = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/users/$uid")
        mDbRef.setValue(user)
            .addOnSuccessListener {
                Log.d("MainActivity", "User saved to database")
                //user created, go to messages screen
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to register user: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}

@Parcelize
class User(val uid : String, val username : String): Parcelable {
    constructor(): this("", "")
}
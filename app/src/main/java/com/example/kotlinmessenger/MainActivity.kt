package com.example.kotlinmessenger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.sql.Timestamp
import java.util.HashMap
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.firestore.FirebaseFirestore
//import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    //private lateinit var mDbRef: DatabaseReference
    val db = Firebase.firestore

    private val avatar = "https://firebasestorage.googleapis.com/v0/b/telemedizinproject.appspot.com/o/images%2F426c243e-824f-424c-b068-871e4ad07bbd?alt=media&token=64df72f9-2927-4ae3-a2d9-ee76cb54d4ff"
    var radioGroup: RadioGroup? = null
    var radioButton: RadioButton? = null
    var role = "Patient";

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("BUTTON_SETTER", " MAIN ACTIVITY CALLED")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        radioGroup = findViewById(R.id.radioGroup);

        FirebaseMessaging.getInstance().subscribeToTopic("bpmAlert")

        // Log.e("ICDAPI", "Started Service")
        // val client = ICDAPI();
        // GlobalScope.launch {
        //     client.getAllergens()
        // }

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
                    //saveUserToFirebaseDatabase(uid)

                    saveUserToFirestoreDatabase(uid,username_edittext_registration.text.toString(),role,email_edittext_registration.text.toString())
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

    //saves the created user to firestore
    private fun saveUserToFirestoreDatabase(uid : String, username: String, role: String, email: String){
        val db = Firebase.firestore
        val user: MutableMap<String,Any> = HashMap()
        user["uid"] = uid
        user["username"] = username
        user["role"] = role
        user["email"] = email
        user["avatar"] = avatar //give user a default avatar
        user["activeChats"] = arrayListOf("")
        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("MainActivity", "User saved to database") //user created
                val intent = Intent(this, ProfileActivity::class.java)

                // Setting User Role Pref ...
                val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
                var editor = sharePref.edit()
                editor.putString("role", role)
                editor.apply()

                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to register user: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    fun checkButton(v: View?) {
        val radioId = radioGroup!!.checkedRadioButtonId
        radioButton = findViewById(radioId)
        role = radioButton?.getText() as String
    }
}




package com.example.kotlinmessenger

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinmessenger.icdapi.ICDAPI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        Log.e("ICDAPI", "Started Service")
        val client = ICDAPI();
        GlobalScope.launch {
            client.baseInformation()
        }

        back_to_register_text_view.setOnClickListener {
            finish()
        }

        login_button.setOnClickListener {
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()

            //check if email/password empty
            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter text in email/password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    // else if successful
                    Log.d("MainActivity", "Successfully logged in user with uid: ${it.result?.user?.uid}" )
                    val docRef = Firebase.firestore.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            val userRole = document.data?.get("role")
                            // Setting User Role Pref ...
                            val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
                            var editor = sharePref.edit()
                            editor.putString("role", userRole.toString())
                            editor.apply()


                            val intent = Intent(this, OverviewPage::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        .addOnFailureListener { exception ->
                            Log.d(ContentValues.TAG, "get failed with ", exception)
                        }


                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to log in: ${it.message}", Toast.LENGTH_LONG).show()
                                 }
        }
    }


}

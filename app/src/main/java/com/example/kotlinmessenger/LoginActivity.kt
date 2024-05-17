package com.example.kotlinmessenger

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinmessenger.icdapi.ICDAPI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/*import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging*/

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // TODO Remove - Testing API Stuff



        // Log.d(ContentValues.TAG, "We be loading my API!") //document
        val client = ICDAPI();
        // client.testAPI();
        // client.testCall()
        GlobalScope.launch {
            client.baseInformation()
        }
        // val listOfentries = CoroutineScope(Dispatchers.Main).launch {
        //     client.testCall()

        // }



        // val TOKEN_ENPOINT = "https://icdaccessmanagement.who.int/connect/token"
        // val CLIENT_ID = "b51415dc-e89a-42c3-af5f-840d0c4bd957_439fec54-d0e8-4d42-8d5c-dff5a62a52ce"
        // val CLIENT_SECRET = "fOjTfkXfPdsH2ZUMqHaDzAXA/4erw0F0Zw08cNuvEn4=" // change needed

        // val SCOPE = "icdapi_access"
        // val GRANT_TYPE = "client_credentials"

        // val test = Account("apiClient", "API")

        // val am: AccountManager = AccountManager.get(this)
        // val options = Bundle()

        // if (am.addAccountExplicitly(test, null, null)) {
        //     am.setUserData(test, "client_id", CLIENT_ID)
        //     am.setUserData(test, "client_secret", CLIENT_SECRET)
        // }



        // am.getAuthToken(
        //     test,                     // Account retrieved using getAccountsByType()
        //     "Manage your tasks",            // Auth scope
        //     options,                        // Authenticator-specific options
        //     this,                           // Your activity
        //     OnTokenAcquired(),              // Callback called when a token is successfully acquired
        //     Handler(OnError())              // Callback called if an error occurs
        // )
        // Log.d(ContentValues.TAG, "API Should have loaded") //document

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

                    val intent = Intent(this, LatestMessagesActivity::class.java)

                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to log in: ${it.message}", Toast.LENGTH_LONG).show()
                                 }
        }
    }


}

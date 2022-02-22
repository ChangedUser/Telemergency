package com.example.kotlinmessenger

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*

class LatestMessagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        verifyUserIsLoggedIn()

        getLastCallForUser()
        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        val sendFrom = sharePref.getString("last_message_send_from", "defaultFrom")!!

        // instead of Get it rather sets these users but oh well
        getUserByUID(FirebaseAuth.getInstance().currentUser!!.uid, "current_user")
        getUserByUID(sendFrom, "send_from_user")

        val loggedInUserID = sharePref.getString("current_user_id", "defaultFrom")!!
        val loggedInUserRole = sharePref.getString("current_user_role", "defaultFrom")!!
        val loggedInUserUserName = sharePref.getString("current_user_username", "defaultFrom")!!
        val mainUser = User(loggedInUserID, loggedInUserRole, loggedInUserUserName)

        if (loggedInUserRole != null) {
            role__field.setText(loggedInUserRole)
        }

        if (loggedInUserUserName != null) {
            username_field.setText(loggedInUserUserName)
        }

        if (loggedInUserRole == "Healthcare Professional") {
            // do something
            evaluate_emergency_btn.setVisibility(View.INVISIBLE)
            allergies_field.setVisibility(View.INVISIBLE)
            allergies_textfield.setVisibility(View.INVISIBLE)
            drug_field.setVisibility(View.INVISIBLE)
            drug_textfield.setVisibility(View.INVISIBLE)
            illness__textfield.setVisibility(View.INVISIBLE)
            illness_field.setVisibility(View.INVISIBLE)

        }

        val sendID = sharePref.getString("send_from_user_id", "defaultFrom")!!
        val sendRole = sharePref.getString("send_from_user_role", "defaultFrom")!!
        val sendUsername = sharePref.getString("send_from_user_username", "defaultFrom")!!
        val sendUser = User(sendID, sendRole, sendUsername)
        if (sendUsername != null) {
            last_msg_field.setText(sendUsername)
        }

        evaluate_emergency_btn.setOnClickListener {
            val intent = Intent(this@LatestMessagesActivity, RequestEmergency::class.java)
            // intent.putExtra("meetingID",meeting_id.text.toString())
            // intent.putExtra("isJoin",false)
            startActivity(intent)
        }

        last_msg_field.setOnClickListener {
            if (sendUser != null) {
                val intent = Intent(this, ChatLogActivity::class.java)
                intent.putExtra(NewMessageActivity.USER_KEY, sendUser)
                startActivity(intent)
            }
        }
    }

    private fun getLastCallForUser(){
        val ref = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages")
            .orderByChild("/timeStamp")
            // .limitToLast(3)

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                var arrayMessages = arrayOf<Message?>()
                var currentuser=  FirebaseAuth.getInstance().currentUser
                // Log.i("Debugging user", currentuser!!.uid)
                snapshot.children.forEach{
                    val message = it.getValue(Message::class.java)
                    if (message!!.toId == currentuser!!.uid) {
                        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
                        var editor = sharePref.edit()
                        editor.remove("last_message_send_from")
                        editor.remove("last_message_send_text")
                        editor.remove("last_message_send_id")
                        // editor.putString("role", snapshot.getValue().toString())
                        editor.putString("last_message_send_from", message!!.fromId)
                        editor.putString("last_message_send_text", message!!.text)
                        editor.putString("last_message_send_id", message!!.id)
                        editor.commit()

                    }

                }


            }
        })
    }

    private fun getUserByUID(uid: String, user_type: String){
        // val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        // val sendFrom = sharePref.getString("last_message_send_from", "defaultFrom")!!

        val ref = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/users")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    // Log.d("NewMessageActivity", it.toString())
                    val user = it.getValue(User::class.java)
                    //checks that user is not null and not the one logged in

                    if (user!=null && user.uid == uid) {
                        // userRole = user.role
                        // Setting User Role Pref ...
                        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
                        var editor = sharePref.edit()
                        var temp_id =  user_type + "_id"
                        var temp_role =  user_type + "_role"
                        var temp_username =  user_type + "_username"
                        editor.remove(temp_id)
                        editor.remove(temp_role)
                        editor.remove(temp_username)

                        editor.putString(temp_id, user.uid)
                        editor.putString(temp_role, user.role)
                        editor.putString(temp_username , user.username)
                        editor.commit()
                    }
                }
            }
        })


    }

    //verify is user is logged in, otherwise go to register screen
    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
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
}
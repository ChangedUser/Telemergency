package com.example.kotlinmessenger

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_latest_messages.*
import java.util.*

class LatestMessagesActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private var selectedPhotoUri: Uri? = null
    var photoChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        //verifies that the user is logged in before continuing further
        verifyUserIsLoggedIn()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        //finds the currently logged user in the firestore db
        val docRef = db.collection("users").document(uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}") //document
                    //find role and username and add them to the text fields
                    val role = document.data?.get("role")
                    val username = document.data?.get("username")
                    val email = document.data?.get("email")
                    val avatar = document.data?.get("avatar")
                    val birthdate = document.data?.get("birthdate")
                    val name = document.data?.get("name")

                    //fills generic attributes
                    username_field.setText(username.toString())
                    role__field.setText(role.toString())
                    if(avatar!=null) loadAvatarImage(avatar as String)
                    if(email!=null) mail__field.setText(email.toString())
                    if(birthdate!=null) birth_field.setText(birthdate.toString())
                    if(name!=null) name__field.setText(name.toString())

                    //hide fields if role is healthcare professional
                    if (role == "Healthcare Professional") {
                        evaluate_emergency_btn.setVisibility(View.INVISIBLE)
                        allergies_field.setVisibility(View.INVISIBLE)
                        allergies_textfield.setVisibility(View.INVISIBLE)
                        drug_field.setVisibility(View.INVISIBLE)
                        drug_textfield.setVisibility(View.INVISIBLE)
                        illness__textfield.setVisibility(View.INVISIBLE)
                        illness_field.setVisibility(View.INVISIBLE)
                    } else { //fill fields if role is Patient
                        val allergies = document.data?.get("allergies")
                        val drugs = document.data?.get("drugs")
                        val illnesses = document.data?.get("illnesses")
                        allergies_field.setText(allergies.toString())
                        drug_field.setText(drugs.toString())
                        illness_field.setText(illnesses.toString())
                    }
                } else {
                    Log.d(TAG, "No such user")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        /*
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
        }*/

        //button for selecting avatar
        avatar_button.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0) //method starts selecting the photo
        }

        edit_profile_btn.setOnClickListener {
            if (photoChanged && selectedPhotoUri != null) {
                uploadImageAndEditUser(selectedPhotoUri!!, uid)
            }
        }

        bpmbutton.setOnClickListener {
            val intent = Intent(this, LineChartActivity::class.java)
            startActivity(intent)
        }
    }

    //selects the photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data!=null) {
            //proceed to check selected image
            Log.d("LatestMessagesActivity", "Photo was selected")

            selectedPhotoUri = data.data
            Picasso.get().load(selectedPhotoUri).resize(140, 140).centerCrop().into(avatar_button)
            photoChanged = true
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

    /*private fun getUserByUID(uid: String, user_type: String){
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
 */

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

    //function uploads image to storage
    private fun uploadImageAndEditUser(selectedPhotoUri : Uri, uid : String) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri)
            .addOnSuccessListener {
                Log.d("LatestMessageActivity", "Successfully uploaded image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirestoreDatabase(it.toString(), uid) //it.toString() is the url of the uploaded image
                    photoChanged = false
                }
            }
    }

    //updates user in database
    private fun saveUserToFirestoreDatabase(avatar : String, uid : String) {
        val username = username_field.text.toString()
        val name = name__field.text.toString()
        val email = mail__field.text.toString()
        val role = role__field.text.toString()
        val birthdate = birth_field.text.toString()

        val user: MutableMap<String,Any> = HashMap()
        if (username != "") user["username"] = username
        if (name != "") user["name"] = name
        if (birthdate != "") user["birthdate"] = birthdate
        //extra fields for patient
        if (role == "Patient") {
            val allergies = allergies_field.text.toString()
            val drugs = drug_field.text.toString()
            val illnesses = illness_field.text.toString()
            if (allergies != "") user["allergies"] = allergies
            if (drugs != "") user["drugs"] = drugs
            if (illnesses != "") user["illnesses"] = illnesses
        }
        user["avatar"] = avatar
        user["email"] = email
        user["role"] = role
        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("MainActivity", "User edited in database") //user edited
                Toast.makeText(this, "Profile edited successfully", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to register user: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadAvatarImage(imagePath: String) {
        Picasso.get().load(imagePath).resize(140, 140).centerCrop().into(avatar_button)
    }
}
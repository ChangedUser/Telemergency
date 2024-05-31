package com.example.kotlinmessenger

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*
import android.content.ContentValues.TAG
import android.view.Menu
import android.view.MenuItem
import java.util.ArrayList
/*import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import android.content.SharedPreferences*/


class NewMessageActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        val userRole = sharePref.getString("role", "defaultRole")!!
        if (userRole.toString() == "Patient") {
            setTheme(R.style.Theme_TelemergencyPatient)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"

        //adapter needed for the recycler view, will contain all the users
        val adapter = GroupAdapter<ViewHolder>()
        val adapterActive = GroupAdapter<ViewHolder>()

        getCurrentUser(adapterActive,adapter)

        //open emergency log when you click the adapter for the new chat
        adapter.setOnItemClickListener { item, view ->
            val userItem = item as UserItem

            if (userRole == "Patient") {
                val intent = Intent(this, RequestEmergency::class.java)
                intent.putExtra(USER_KEY, userItem.user)
                startActivity(intent)
            } else if (userRole == "Healthcare Professional") {
                val intent = Intent(this, ChatLogActivity::class.java)
                intent.putExtra(USER_KEY, userItem.user)
                startActivity(intent)
            }
        }

        //open chat log when you click the adapter for the active chats
        adapterActive.setOnItemClickListener { item, view ->
            val userItem = item as UserItem
            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(USER_KEY, userItem.user)
            startActivity(intent)
        }

        recyclerview_newmessage2.adapter = adapter
        recyclerview_newmessage1.adapter = adapterActive
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
    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun getCurrentUser(adapterActive: GroupAdapter<ViewHolder>,adapter: GroupAdapter<ViewHolder>){
        val docRef = db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                val userRole = document.data?.get("role")
                val activeChats = document.data?.get("activeChats") as ArrayList<String>

                //fill the user lists
                getActiveChats(adapterActive,activeChats)
                getUsersFirestore(adapter,activeChats)

                // Setting User Role Pref ...
                // TODO - potentially not needed?
                val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
                var editor = sharePref.edit()
                editor.remove("role")
                // editor.putString("role", snapshot.getValue().toString())
                editor.putString("role", userRole.toString())
                editor.commit()
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    //displays list of users where role is Healthcare Professional to start a new chat
    private fun getUsersFirestore(adapter: GroupAdapter<ViewHolder>,activeChats:ArrayList<String>) {
        db.collection("users")
            .whereEqualTo("role", "Healthcare Professional")
            .get()
            .addOnSuccessListener { users ->
                for (dbUser in users) {
                    Log.d(TAG, "${dbUser.id} => ${dbUser.data}")
                    var uid = dbUser.data?.get("uid")
                    var username = dbUser.data?.get("username")
                    var role = dbUser.data?.get("role")
                    var user = User(uid.toString(),username.toString(),role.toString())
                    Log.d(TAG, "Creating User: " + user.username + " with uid " +  user.uid + " and role " + user.role)
                    //appends
                    if (user.uid != FirebaseAuth.getInstance().currentUser?.uid && !(activeChats.contains(user.uid))) {
                        adapter.add(UserItem(user))
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun getActiveChats(adapterActive: GroupAdapter<ViewHolder>, activeChats:ArrayList<String>) {
        Log.d("ActiveChat", "Active chats")
        for (item in activeChats) {
            Log.d("ActiveChat", item)

            db.collection("users")
                .whereEqualTo("uid", item)
                .get()
                .addOnSuccessListener { users ->
                    for (dbUser in users) {
                        Log.d(TAG, "${dbUser.id} => ${dbUser.data}")
                        var uid = dbUser.data?.get("uid")
                        var username = dbUser.data?.get("username")
                        var role = dbUser.data?.get("role")
                        var user = User(uid.toString(),username.toString(),role.toString())
                        Log.d(TAG, "Creating User: " + user.username + " with uid " +  user.uid + " and role " + user.role)
                        //appends
                        if (user.uid != FirebaseAuth.getInstance().currentUser?.uid) {
                            adapterActive.add(UserItem(user))
                        }

                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }



        }
    }

    /*
    //get all users in list
    private fun getUsers(adapter: GroupAdapter<ViewHolder>){
    val ref = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/users")
    ref.addListenerForSingleValueEvent(object: ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
        }
        override fun onDataChange(snapshot: DataSnapshot) {
            var arrayHealthcare = arrayOf<User?>()
            var arrayPatient = arrayOf<User?>()
            var userRole = ""
            //iterates through the users in the database
            snapshot.children.forEach{
                // Log.d("NewMessageActivity", it.toString())
                val user = it.getValue(User::class.java)
                //checks that user is not null and not the one logged in
                if (user!=null && user.role=="Healthcare Professional" && user.uid != FirebaseAuth.getInstance().currentUser?.uid) {
                    //adapter.add(UserItem(user))
                    arrayHealthcare = append(arrayHealthcare, user)
                } else if (user!=null && user.role=="Patient" && user.uid != FirebaseAuth.getInstance().currentUser?.uid) {
                    arrayPatient = append(arrayPatient, user)
                } else if (user!=null && user.uid == FirebaseAuth.getInstance().currentUser?.uid) {
                    userRole = user.role

                    // Setting User Role Pref ...
                    val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
                    var editor = sharePref.edit()
                    editor.remove("role")
                    // editor.putString("role", snapshot.getValue().toString())
                    editor.putString("role", userRole)
                    editor.commit()
                }
            }

            if (userRole == "Patient") {
                for (userItem in arrayHealthcare) adapter.add(UserItem(userItem))
            } else if (userRole == "Healthcare Professional") {
                for (userItem in arrayPatient) adapter.add(UserItem(userItem))
            }

        }
    })
}
*/

    class UserItem(val user: User?): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.username_textview_newmessage.text = user?.username ?: null
        }
        override fun getLayout(): Int {
            return R.layout.user_row_new_message
        }
    }
    //helper function: append item to array
    fun <T> append(arr: Array<T>, element: T): Array<T?> {
        val array = arr.copyOf(arr.size + 1)
        array[arr.size] = element
        return array
    }
}
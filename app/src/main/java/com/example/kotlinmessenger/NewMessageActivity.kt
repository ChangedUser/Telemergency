package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"

        //adapter needed for the recycler view, will contain all the users
        val adapter = GroupAdapter<ViewHolder>()
        getUsers(adapter)
        //open chat log when you click the adapter for the user
        adapter.setOnItemClickListener { item, view ->
            val userItem = item as UserItem
            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(USER_KEY, userItem.user)
            startActivity(intent)
        }
        recyclerview_newmessage.adapter = adapter
    }
    companion object {
        val USER_KEY = "USER_KEY"
    }
    private fun getUsers(adapter: GroupAdapter<ViewHolder>){
        val ref = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                //iterates through the users in the database
                snapshot.children.forEach{
                    Log.d("NewMessageActivity", it.toString())
                    val user = it.getValue(User::class.java)
                    //checks that user is not null and not the one logged in
                    if (user!=null && user.uid != FirebaseAuth.getInstance().currentUser?.uid) adapter.add(UserItem(user))
                }
            }
        })
    }
    class UserItem(val user: User): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.username_textview_newmessage.text = user.username
        }
        override fun getLayout(): Int {
            return R.layout.user_row_new_message
        }
    }
}
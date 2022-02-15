package com.example.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_image_from_row.view.*
import kotlinx.android.synthetic.main.chat_image_to_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.util.*

class ChatLogActivity : AppCompatActivity() {
    private var user = User() ?: null
    private var fromId = String()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        fromId = FirebaseAuth.getInstance().uid ?: ""
        supportActionBar?.title = user?.username

        val adapter = GroupAdapter<ViewHolder>()
        //get the messages that will be added to adapter
        getMessages(adapter, fromId, user?.uid ?: "")
        recyclerview_chat_log.adapter = adapter

        //send new message when button is clicked
        send_button_chat_log.setOnClickListener {
            performSendMessage(fromId, user?.uid ?: "")
        }
        imagebutton_chat_log.setOnClickListener {
            performSendImage()
        }
    }
    //send message with sender id, receiver id, text and timestamp
    private fun performSendMessage(fromId: String, toId: String) {
        val text = edittext_chat_log.text.toString()
        val reference = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages").push()
        val chatMessage = ChatMessage(reference?.key ?: "", text, null, fromId, toId, System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "Saved our chat message ${reference.key}")
                edittext_chat_log.text.clear()
            }
    }
    //starts the process of sending an image in the chat
    private fun performSendImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }
    //overridden function when image is selected
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("ChatLogActivity", "Photo Selected")
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            if(uri != null) uploadImageToFirebaseStorage(uri)
        }
    }
    //function uploads image to storage
    private fun uploadImageToFirebaseStorage(selectedPhotoUri : Uri) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri)
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "Successfully uploaded image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    performSendImageChat(it.toString())
                }
            }
    }
    //sends image as a chat message
    private fun performSendImageChat(imagePath:String) {
        val reference = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages").push()
        val chatImage = ChatMessage(reference?.key ?: "", null, imagePath, fromId, user?.uid!!, System.currentTimeMillis()/1000)
        reference.setValue(chatImage)
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "Saved our chat image ${reference.key}")
            }
    }
    //get messages to display on adapter
    private fun getMessages(adapter: GroupAdapter<ViewHolder>, fromId: String, toId: String) {
        val reference = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages")
        reference.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    if (chatMessage.text!=null && chatMessage.text!="") {
                        Log.d("ChatLogActivity", chatMessage.text)
                        if(chatMessage.fromId == fromId && chatMessage.toId == toId) adapter.add(ChatToItem(chatMessage.text))
                        if(chatMessage.fromId == toId && chatMessage.toId == fromId) adapter.add(ChatFromItem(chatMessage.text))
                    } else if (chatMessage.imagePath != null && chatMessage.imagePath != "") {
                        Log.d("ChatLogActivity", chatMessage.imagePath)
                        if(chatMessage.fromId == fromId && chatMessage.toId == toId) adapter.add(ChatToImage(chatMessage.imagePath))
                        if(chatMessage.fromId == toId && chatMessage.toId == fromId) adapter.add(ChatFromImage(chatMessage.imagePath))
                    }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    class ChatFromItem(val text: String): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textview_from_row.text = text
        }
        override fun getLayout(): Int {
            return R.layout.chat_from_row
        }
    }
    class ChatFromImage(val imagePath: String): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            val targetImageView = viewHolder.itemView.imageview_from_row
            Picasso.get().load(imagePath).into(targetImageView)
        }
        override fun getLayout(): Int {
            return R.layout.chat_image_from_row
        }
    }
    class ChatToItem(val text: String): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textview_to_row.text = text
        }
        override fun getLayout(): Int {
            return R.layout.chat_to_row
        }
    }
    class ChatToImage(val imagePath: String): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            val targetImageView = viewHolder.itemView.imageview_to_row
            Picasso.get().load(imagePath).into(targetImageView)
        }
        override fun getLayout(): Int {
            return R.layout.chat_image_to_row
        }
    }
    class ChatMessage(val id: String, val text: String?, val imagePath: String?, val fromId: String, val toId: String, val timeStamp: Long) {
        constructor(): this("","", "","", "",-1)
    }
}
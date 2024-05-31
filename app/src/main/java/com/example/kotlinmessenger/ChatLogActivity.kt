package com.example.kotlinmessenger

// ### Call feature
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinmessenger.webrtc.Constants
import com.example.kotlinmessenger.webrtc.RTCActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
    val db = Firebase.firestore
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        val userRole = sharePref.getString("role", "defaultRole")!!
        if (userRole.toString() == "Patient") {
            setTheme(R.style.Theme_TelemergencyPatient)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        fromId = FirebaseAuth.getInstance().uid ?: ""
        supportActionBar?.title = user?.username

        // presetting some variables for the call feature
        // setContentView(R.layout.phone_start)
        Constants.isIntiatedNow = true
        Constants.isCallEnded = true

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

        call_button.setOnClickListener {
            val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
            val userRole = sharePref.getString("role", "defaultRole")!!
            if (userRole == "Patient") {
                Log.i("Patient Connection", userRole)
                if (meeting_id.text.toString().trim().isNullOrEmpty())
                    meeting_id.error = "Please enter meeting id"
                else {
                    val intent = Intent(this@ChatLogActivity, RTCActivity::class.java)
                    intent.putExtra("meetingID",meeting_id.text.toString())
                    intent.putExtra("isJoin",true)
                    startActivity(intent)
                }
            } else if (userRole == "Healthcare Professional") {
                Log.i("Professional Connection", userRole)
                if (meeting_id.text.toString().trim().isNullOrEmpty()) {
                    meeting_id.error = "Please enter meeting id"
                } else {
                    db.collection("calls")
                        .document(meeting_id.text.toString())
                        .get()
                        .addOnSuccessListener {
                            if (it["type"]=="OFFER" || it["type"]=="ANSWER" || it["type"]=="END_CALL") {
                                meeting_id.error = "Please enter another meeting ID"
                            } else {
                                val intent = Intent(this@ChatLogActivity, RTCActivity::class.java)
                                intent.putExtra("meetingID",meeting_id.text.toString())
                                intent.putExtra("isJoin",false)
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener {
                            meeting_id.error = "Please enter a new meeting ID"
                        }
                }

            }

            /*
            db.collection("calls")
                .document(meeting_id.text.toString())
                .get()
                .addOnSuccessListener {
                    if (it["type"]=="OFFER" || it["type"]=="ANSWER" || it["type"]=="END_CALL") {
                        meeting_id.error = "Please enter another meeting ID"
                    } else {
                        val intent = Intent(this@ChatLogActivity, RTCActivity::class.java)
                        intent.putExtra("meetingID",meeting_id.text.toString())
                        intent.putExtra("isJoin",false)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    meeting_id.error = "Please enter a new meeting ID"
                }

             */

        }
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

    //send message with sender id, receiver id, text and timestamp
    private fun performSendMessage(fromId: String, toId: String) {
        val text = edittext_chat_log.text.toString()

        val newChatMessage: MutableMap<String,Any> = HashMap()
        newChatMessage["imagePath"] = ""
        newChatMessage["fromId"] = fromId
        newChatMessage["toId"] = toId
        newChatMessage["text"] = text
        newChatMessage["timeStamp"] = System.currentTimeMillis()/1000
        db.collection("messages").add(newChatMessage).addOnSuccessListener { documentReference ->
            Log.d("ChatLogActivity", "DocumentSnapshot written with ID: ${documentReference.id}")
            edittext_chat_log.text.clear()
            adapter.add(ChatToItem(text))
            //addToActiveChats(fromId, toId)
            //addToActiveChats(toId, fromId)
        }

        /*val reference = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages").push()
        val chatMessage = ChatMessage(reference?.key ?: "", text, null, fromId, toId, System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "Saved our chat message ${reference.key}")
                edittext_chat_log.text.clear()
                addToActiveChats(fromId, toId)
                addToActiveChats(toId, fromId)
            }*/
    }

    //starts the process of sending an image in the chat
    private fun performSendImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    //add to the active chats of the user
    private fun addToActiveChats(fromId : String, toId: String) {
        //gets current user
        val docRef = db.collection("users").document(fromId)
        //find active chats of user, add the id if not existing already
        docRef.get().addOnSuccessListener { user ->
            val activeChats = user.data?.get("activeChats") as ArrayList<String>
            if(!(activeChats.contains(toId))) activeChats.add(toId)
            val data = hashMapOf("activeChats" to activeChats)
            db.collection("users").document(fromId)
                .set(data, SetOptions.merge())
        }
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
        val newChatMessage: MutableMap<String,Any> = HashMap()
        newChatMessage["imagePath"] = imagePath
        newChatMessage["fromId"] = fromId
        newChatMessage["toId"] = user?.uid!!
        newChatMessage["text"] = ""
        newChatMessage["timeStamp"] = System.currentTimeMillis()/1000


        db.collection("messages").add(newChatMessage).addOnSuccessListener { documentReference ->
            Log.d("ChatLogActivity", "DocumentSnapshot written with ID: ${documentReference.id}")
            //edittext_chat_log.text.clear()
            adapter.add(ChatToImage(imagePath))
        }

        /*val reference = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages").push()
        val chatImage = ChatMessage(reference?.key ?: "", null, imagePath, fromId, user?.uid!!, System.currentTimeMillis()/1000)
        reference.setValue(chatImage)
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "Saved our chat image ${reference.key}")
            } */
    }

    //get messages to display on adapter
    private fun getMessages(adapter: GroupAdapter<ViewHolder>, fromId: String, toId: String) {
        val messagesRef = db.collection("messages")

        // Query for messages from userId1 to userId2
        val query1 = messagesRef
            .whereEqualTo("fromId", fromId)
            .whereEqualTo("toId", toId)

        // Query for messages from userId2 to userId1
        val query2 = messagesRef
            .whereEqualTo("fromId", toId)
            .whereEqualTo("toId", fromId)

        query1.get().addOnSuccessListener { querySnapshot1 ->
            val messages1 = querySnapshot1.toObjects(ChatMessage::class.java)
            query2.get().addOnSuccessListener { querySnapshot2 ->
                val messages2 = querySnapshot2.toObjects(ChatMessage::class.java)
                val allMessages = messages1 + messages2
                val sortedMessages = allMessages.sortedBy { it.timeStamp }
                for (chatMessageDb in sortedMessages){
                    Log.d("ChatLogActivity", "${chatMessageDb.text}")
                    if (chatMessageDb.text!=null && chatMessageDb.text!="") {
                        if (chatMessageDb.text.toString().contains("_n")) {
                            var newText = chatMessageDb.text.toString().replace("_n",System.getProperty("line.separator"))
                            if(chatMessageDb.fromId == fromId && chatMessageDb.toId == toId) adapter.add(ChatToItem(newText))
                            if(chatMessageDb.fromId == toId && chatMessageDb.toId == fromId) adapter.add(ChatFromItem(newText))
                        }
                        else {
                            if(chatMessageDb.fromId == fromId && chatMessageDb.toId == toId) adapter.add(ChatToItem(chatMessageDb.text.toString()))
                            if(chatMessageDb.fromId == toId && chatMessageDb.toId == fromId) adapter.add(ChatFromItem(chatMessageDb.text.toString()))
                        }

                    }
                    if (chatMessageDb.imagePath!=null && chatMessageDb.imagePath!="") {
                        if(chatMessageDb.fromId == fromId && chatMessageDb.toId == toId) adapter.add(ChatToImage(chatMessageDb.imagePath.toString()))
                        if(chatMessageDb.fromId == toId && chatMessageDb.toId == fromId) adapter.add(ChatToImage(chatMessageDb.imagePath.toString()))
                    }
                }
            }
        }
        //old db method
        //val reference = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages")
        /*reference.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    var newText = ""
                    if (chatMessage.text!=null && chatMessage.text!="") {
                        Log.d("ChatLogActivity", chatMessage.text)
                        if (chatMessage.text.toString().contains("_n")) {
                            newText = chatMessage.text.toString().replace("_n",System.getProperty("line.separator"))
                        }

                        if(chatMessage.fromId == fromId && chatMessage.toId == toId) {
                            if (newText!="") {
                                adapter.add(ChatToItem(newText))
                                newText = ""
                            } else {
                                adapter.add(ChatToItem(chatMessage.text))
                            }
                        }
                        if(chatMessage.fromId == toId && chatMessage.toId == fromId) {
                            if (newText!="") {
                                adapter.add(ChatFromItem(newText))
                                newText = ""
                            } else {
                                adapter.add(ChatFromItem(chatMessage.text))
                            }
                        }
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
        } ) */
    }

    //helper methods to display the chat on the screen
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
    //chatmessage class
    class ChatMessage(val id: String, val text: String?, val imagePath: String?, val fromId: String, val toId: String, val timeStamp: Long) {
        constructor(): this("","", "","", "",-1)
    }
}
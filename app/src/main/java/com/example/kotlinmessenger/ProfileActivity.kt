package com.example.kotlinmessenger

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import com.example.kotlinmessenger.icdapi.ICDAPI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

//first activity after you log in, profile
class ProfileActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private var selectedPhotoUri: Uri? = null
    var photoChanged = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var tvlatitude: TextView

    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharePref = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE)
        val userRole = sharePref.getString("role", "defaultRole")!!
        if (userRole.toString() == "Patient") {
            setTheme(R.style.Theme_TelemergencyPatient)
        }

        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_latest_messages)
        setContentView(R.layout.activity_profile)
        //verifies that the user is logged in before continuing further
        // TODO - Should we verify in each activity?
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
                    val bloodtype = document.data?.get("bloodtype")
                    val phone = document.data?.get("phone")
                    val address = document.data?.get("address")

                    //val activeChats = document.data?.get("activeChats") as ArrayList<String>

                    //fills generic attributes
                    username_field.setText(username.toString())
                    role__field.setText(role.toString())
                    if(avatar!=null) loadAvatarImage(avatar as String)
                    if(email!=null) mail__field.setText(email.toString())
                    if(birthdate!=null) birth_field.setText(birthdate.toString())
                    if(name!=null) name__field.setText(name.toString())
                    if(phone!=null) phone_field.setText(phone.toString())
                    if(address!=null) address_field.setText(address.toString())

                    //hide fields if role is healthcare professional
                    if (role == "Healthcare Professional") {
                        show_allergies.setVisibility(View.GONE)
                        show_drugs.setVisibility(View.GONE)
                        show_illness.setVisibility(View.GONE)
                        medical_info_field.setVisibility(View.GONE)
                        blood_type_field.setVisibility(View.GONE)
                        geolocation.setVisibility(View.GONE)
                    } else {

                        val db_ret_allergies = document.data?.get("allergies") as? Map<String, String> ?: emptyMap()
                        val db_ret_drugs = document.data?.get("drugs") as? Map<String, String> ?: emptyMap()
                        val db_ret_illnesses = document.data?.get("illnesses") as? Map<String, String> ?: emptyMap()

                        if(bloodtype!=null) blood_type_field.setText(bloodtype.toString())

                        fillCheckboxes(db_ret_allergies,0, R.id.allergies_container)
                        fillCheckboxes(db_ret_illnesses, 1, R.id.illness_container)
                        fillCheckboxes(db_ret_drugs, 2, R.id.drugs_container)

                    }
                } else {
                    Log.d(TAG, "No such user")
                }



            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        //button for selecting avatar
        avatar_button.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0) //method starts selecting the photo
        }

        show_allergies.setOnClickListener{
            if (scroll_allergies.isGone){
                scroll_allergies.isGone = false
            }else  {
                scroll_allergies.isGone = true
            }
        }

        show_drugs.setOnClickListener{
            if (scroll_drugs.isGone){
                scroll_drugs.isGone = false
            }else  {
                scroll_drugs.isGone = true
            }
        }

        show_illness.setOnClickListener{
            if (scroll_ilnesses.isGone){
                scroll_ilnesses.isGone = false
            }else  {
                scroll_ilnesses.isGone = true
            }
        }

        edit_profile_btn.setOnClickListener {
            if (photoChanged && selectedPhotoUri != null) {
                uploadImageAndEditUser(selectedPhotoUri!!, uid)
            } else {
                saveUserToFirestoreDatabase(uid)
            }
        }

        // bpmbutton.setOnClickListener {
        //     val intent = Intent(this, LineChartActivity::class.java)
        //     startActivity(intent)
        // }

        geolocation.setOnClickListener{
           // val intent = Intent(this, Geolocation::class.java)
            val intent = Intent(this, OSMActivity::class.java)
            startActivity(intent)
        }


            GlobalScope.launch(Dispatchers.IO) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@ProfileActivity)
                //tvlatitude = findViewById(R.id.latitude)


            if (
                ActivityCompat.checkSelfPermission(this@ProfileActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this@ProfileActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                // TODO - No permission granted

            }else {
                fusedLocationProviderClient.lastLocation
                    .addOnCompleteListener(this@ProfileActivity) { task ->
                        var location: Location? = task.result
                        var loc = hashMapOf<String, String>()

                        if (location != null) {
                            //  tvlatitude.text = "" + location.latitude
                            loc.set("Email:", FirebaseAuth.getInstance().currentUser!!.email.toString())
                            loc.set("latitude", ""+location.latitude)
                            loc.set("longitude", ""+location.longitude)
                            db.collection("lastknownposition")
                                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                .set(loc)
                                .addOnSuccessListener{ Log.d(TAG, "DocumentSnapshot successfully written!") }
                                .addOnFailureListener{ e -> Log.d(TAG, "Error writing document!") }


                        }
                    }
            }


        }

    }

    private fun fillCheckboxes(map: Map<String, String>, type: Int, container: Int) {
        val client = ICDAPI();

        GlobalScope.launch {
            var res = mapOf<String, String>()
            // Allergies
            if (type == 0) {
                res = client.getAllergens()
                // Illnesses
            }else if (type == 1){
                res = client.getIllnessess()
            }else {
                res = client.getDrugs()
            }

            for (item in res) {
                var cb_checked = false
                if (map.containsKey(item.key)) {
                    cb_checked = true
                }
                val checkBox = CheckBox(this@ProfileActivity).apply {
                    text = item.value
                    tag = item.key
                    // value = allergy.key
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    isChecked = cb_checked
                }
                findViewById<LinearLayout>(container).addView(checkBox)
                // allergies_container.addView(checkBox)
            }
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
            // R.id.menu_new_message -> {
            //     val intent = Intent(this, NewMessageActivity::class.java)
            //     //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            //     startActivity(intent)
            // }
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

    //function uploads image to storage
    private fun uploadImageAndEditUser(selectedPhotoUri : Uri, uid : String) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri)
            .addOnSuccessListener {
                Log.d("LatestMessageActivity", "Successfully uploaded image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirestoreDatabaseWithAvatar(it.toString(), uid) //it.toString() is the url of the uploaded image
                    photoChanged = false
                }
            }
    }

    //updates user in database if Avatar is also changed
    private fun saveUserToFirestoreDatabaseWithAvatar(avatar : String, uid : String) {
        val username = username_field.text.toString()
        val name = name__field.text.toString()
        val email = mail__field.text.toString()
        val role = role__field.text.toString()
        val birthdate = birth_field.text.toString()
        val phone = phone_field.text.toString()
        val address = address_field.text.toString()

        val user: MutableMap<String,Any> = HashMap()
        if (username != "") user["username"] = username
        if (name != "") user["name"] = name
        if (birthdate != "") user["birthdate"] = birthdate
        if (phone!= "") user["phone"] = phone
        if (address!= "") user["address"] = address
        //extra fields for patient
        if (role == "Patient") {
            val bloodtype = blood_type_field.text.toString()
            if (bloodtype!= "") user["bloodtype"] = bloodtype

            user["allergies"] = createCBContent(R.id.allergies_container)
            user["drugs"] = createCBContent(R.id.drugs_container)
            user["illnesses"] = createCBContent(R.id.illness_container)
        }

        user["avatar"] = avatar
        user["email"] = email
        user["role"] = role
        db.collection("users")
            .document(uid)
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("MainActivity", "User edited in database") //user edited
                Toast.makeText(this, "Profile edited successfully", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to register user: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    //updates user in database without avatar
    private fun saveUserToFirestoreDatabase(uid : String) {
        val username = username_field.text.toString()
        val name = name__field.text.toString()
        val email = mail__field.text.toString()
        val role = role__field.text.toString()
        val birthdate = birth_field.text.toString()
        val phone = phone_field.text.toString()
        val address = address_field.text.toString()

        val user: MutableMap<String,Any> = HashMap()
        if (username != "") user["username"] = username
        if (name != "") user["name"] = name
        if (birthdate != "") user["birthdate"] = birthdate
        if (phone!= "") user["phone"] = phone
        if (address!= "") user["address"] = address
        //extra fields for patient
        if (role == "Patient") {
            val bloodtype = blood_type_field.text.toString()
            if (bloodtype!= "") {
                user["bloodtype"] = bloodtype
            }else {
                user["bloodtype"] = ""
            }

            user["allergies"] = createCBContent(R.id.allergies_container)
            user["drugs"] = createCBContent(R.id.drugs_container)
            user["illnesses"] = createCBContent(R.id.illness_container)
        }
        user["email"] = email
        user["role"] = role
        db.collection("users")
            .document(uid)
            .set(user, SetOptions.merge())
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

    private fun createCBContent(r_id: Int): Map<String, String> {
        // var layout = findViewById<LinearLayout>(R.id.allergies_container)
        var layout = findViewById<LinearLayout>(r_id)
        val map = mutableMapOf<String, String>()

        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            if (child is CheckBox) {
                if (child.isChecked){
                    // Log.i("CB_SELECTED", "ITEM " +child.text.toString() +  " HAS BEEN SELECTED" );
                    map.put(child.tag.toString()  , child.text.toString())
                }

            }
        }
        return map
    }


    //finds the user from the id provided and writes it in the field
    // DEPR.
    // private fun getLastChatUser(lastUserId: String) {
    //     Log.d(TAG, "Last user id is " + lastUserId)
    //     val docRef = db.collection("users").document(lastUserId)
    //     docRef.get()
    //         .addOnSuccessListener { document ->
    //             if (document != null) {
    //                 Log.d(TAG, "DocumentSnapshot data: ${document.data}") //document
    //                 val username = document.data?.get("username")
    //                 //val name = document.data?.get("name")
    //                 last_msg_field.setText(username.toString()) //fills field with the last user's username
    //             } else {
    //                 Log.d(TAG, "getLastChatUser: No such user")
    //             }
    //         }
    // }


}
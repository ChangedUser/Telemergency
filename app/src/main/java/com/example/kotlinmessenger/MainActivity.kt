package com.example.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.sql.Timestamp
import java.util.ArrayList
import java.util.HashMap

class MainActivity : AppCompatActivity() {
    //private lateinit var mDbRef: DatabaseReference
    var radioGroup: RadioGroup? = null
    var radioButton: RadioButton? = null
    var role = "Patient";


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        radioGroup = findViewById(R.id.radioGroup);

      FirebaseMessaging.getInstance().subscribeToTopic("bpmAlert")


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
                    saveUserToFirebaseDatabase(uid)
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

    private fun saveUserToFirebaseDatabase(uid : String){
        val user = User(uid, username_edittext_registration.text.toString(), role)
        val mDbRef = FirebaseDatabase.getInstance("https://telemedizinproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/users/$uid")
        mDbRef.setValue(user)
            .addOnSuccessListener {
                Log.d("MainActivity", "User saved to database")
                //user created, go to messages screen
                val intent = Intent(this, LatestMessagesActivity::class.java)
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

    /*
    fun setLineChartValues()
    {
        //x axis values

        /*
        val xvalues = ArrayList<String>()
        xvalues.add("Monday")
        xvalues.add("Tuesday")
        xvalues.add("Wednesday")
        xvalues.add("Thursday")
        xvalues.add("Friday")
        xvalues.add("Saturday")
        xvalues.add("Sunday")*/
        var xvalues = ArrayList<LineEntry>()

        xvalues.add(BarEntry(1f,0f))
        xvalues.add(BarEntry(2f,0f))
        xvalues.add(BarEntry(3f,0f))
        xvalues.add(BarEntry(4f,0f))
        xvalues.add(BarEntry(5f,0f))
        xvalues.add(BarEntry(6f,0f))
        xvalues.add(BarEntry(7f,0f))


        //y axis values or bar data

        var barentries = ArrayList<BarEntry>()

        barentries.add(BarEntry(0f, 4f))
        barentries.add(BarEntry(0f,3.5f))
        barentries.add(BarEntry(0f,8.9f))
        barentries.add(BarEntry(0f,10.2f))
        barentries.add(BarEntry(0f,4.4f))
        barentries.add(BarEntry(0f,5.3f))
        barentries.add(BarEntry(0f,7.9f))

        //bardata set
        val bardatasetx = BarDataSet(barentries, "First")
        val bardatasety = BarDataSet(barentries, "Second")
        bardatasetx.color = resources.getColor(R.color.darkorange)
        bardatasety.color = resources.getColor(R.color.colorPrimary)

        val data = BarData(bardatasetx,bardatasety)

        barChart.data = data
        barChart.setBackgroundColor(resources.getColor(R.color.colorAccent))
        barChart.animateXY(3000, 3000)



    }*/
}

@Parcelize
class User(val uid : String, val username : String, val role : String): Parcelable {
    constructor(): this("", "", "")
}

@Parcelize
class Message(val fromId: String, val id: String, val text: String, val timestamp: Timestamp, val toId: String): Parcelable {
    constructor(): this("", "", "", java.sql.Timestamp(0) , "")
}
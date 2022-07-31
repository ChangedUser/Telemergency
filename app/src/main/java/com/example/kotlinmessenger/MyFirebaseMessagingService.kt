package com.example.kotlinmessenger

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.OnSuccessListener
import java.util.HashMap

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.e("NEW_TOKEN", s)
        //Token: cLShjLq-Qg-m90vI75sJqt:APA91bGSuuDbGTxpFOlU8ymnE2BsFmwp_0eAE7qaJs8UJQ6x7spkGJ6gnHLwuhEoLZq0rnVnPWgQ9LKyQSF-7RlkKO9c4ZIKYDrw0CcMfaHOl-CLd7p4c8cjWYehwtC63FNEF2q-AYUA
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Access a Cloud Firestore instance from your Activity
        val db = FirebaseFirestore.getInstance()

        if (remoteMessage.data.isNotEmpty()) {

            val city: MutableMap<String, Any?> = HashMap()
            city["bpmlive"] = remoteMessage.data["text"]

            Log.e("BpMs", "Live Data Point")

            db.collection("BpMs").document(remoteMessage.messageId.toString()).set(city)
                .addOnSuccessListener { Log.e("BpM", "Data Message added") }
        }
        if (remoteMessage.notification != null) {
            val city: MutableMap<String, Any?> = HashMap()
            city["bpmalert"] = remoteMessage.notification!!.body
            db.collection("BpMs").document(remoteMessage.messageId.toString()).set(city)
                .addOnSuccessListener { Log.e("BpM", "Notification added") }
        }


        /*Map<String, Object> bpm = new HashMap<>();
        bpm.put("BpM Alert", Objects.requireNonNull(remoteMessage.getNotification()).getBody());
         */
/*
        db.collection("cities").document("LA").set(city).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.e("City", "City added");
            }
        });*/super.onMessageReceived(remoteMessage)
    }
}
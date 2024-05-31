package com.example.kotlinmessenger.icdapi;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;

import org.json.*;

// public class ICDAPIclient extends AsyncTask<Void, Void, Void> {
public class ICDAPIclient {
    private final String TOKEN_ENPOINT = "https://icdaccessmanagement.who.int/connect/token";
    private final String CLIENT_ID = "b51415dc-e89a-42c3-af5f-840d0c4bd957_439fec54-d0e8-4d42-8d5c-dff5a62a52ce";
    private final String CLIENT_SECRET = "fOjTfkXfPdsH2ZUMqHaDzAXA/4erw0F0Zw08cNuvEn4="; // TODO later on - change needed
    private final String SCOPE = "icdapi_access";
    private final String GRANT_TYPE = "client_credentials";


    public final String TAG = "API_Information";

    public ICDAPIclient() {
    }

    public void onTokenAcquired() {

    }


    private void onTokenError() {

    }

    public void testNewApi(Context context) throws Exception {
        AccountManager am = AccountManager.get(context);
        Bundle options = new Bundle();
        Account test = new Account("apiClient", "API");

        if (am.addAccountExplicitly(test, null, null)) {
            am.setUserData(test, "client_id", CLIENT_ID);
            am.setUserData(test, "client_secret", CLIENT_SECRET);

        }

    }



    public void testAPI() throws Exception {

        String uri = "https://id.who.int/icd/entity";

        ICDAPIclient api = new ICDAPIclient();
        // String token = api.getToken();

        // System.out.println("URI Response JSON : \n" + api.getURI(token, uri));
        // Log.d(TAG, "JSON Response: \n"+ api.getURI(token, uri)); //document
    }



    // access ICD API
    private String getURI(String token, String uri) throws Exception {

        System.out.println("Getting URI...");

        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        // HTTP header fields to set
        con.setRequestProperty("Authorization", "Bearer "+token);
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Accept-Language", "en");
        con.setRequestProperty("API-Version", "v2");

        // response
        int responseCode = con.getResponseCode();
        System.out.println("URI Response Code : " + responseCode + "\n");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}

package com.example.kotlinmessenger.icdapi

import android.R.attr.password
import android.util.Log
import com.google.protobuf.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JSON
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext


public class ICDAPI {

    // We are now simply running it locally
    // HEADER INFO
    val ACCEPT_KEY: String = "ACCEPT"
    val ACCEPT_VALUE: String = "application/json"

    val ACCEPT_LANG_KEY: String = "Accept-Language"
    val ACCEPT_LANG_VALUE: String = "en" // todo - localization

    val API_VERSION_KEY: String = "API-Version"
    val API_VERSION_VALUE: String = "v2"

    val GET_REQUEST: String = "GET"

    val BASE_URL: String = "http://10.0.2.2:80" // TODO - Move to server connection once one is setup

    // ICD - Entrypoints
    val FOUNDATION_INFO: String = "/icd/entity"
    val FOUNDATION_ENTITY: String = "/icd/entity/"
    val FOUNDATION_SEARCH: String = "/icd/entity/?q="


    private fun buildConnection(entry: String): URLConnection {
        val url = URL(BASE_URL + entry)
        val con = url.openConnection() as HttpURLConnection

        con.requestMethod = GET_REQUEST

        // Optional: Set request headers
        // Fix for 412 Erorr -> The Order of my Headers was wrong!!
        con.setRequestProperty(ACCEPT_KEY, ACCEPT_VALUE);
        con.setRequestProperty(ACCEPT_LANG_KEY, ACCEPT_LANG_VALUE);
        con.setRequestProperty(API_VERSION_KEY, API_VERSION_VALUE);
        return con
    }

    private fun parseResult(con: HttpURLConnection): JSONObject {
        // Read response from server
        val reader = BufferedReader(InputStreamReader(con.inputStream))
        val response = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()
        return JSONObject(response.toString())
    }

    suspend fun baseInformation() {
        withContext(Dispatchers.IO) {
            try {
                val con = buildConnection(FOUNDATION_INFO) as HttpURLConnection
                con.connect()

                if (con.responseCode == HttpURLConnection.HTTP_OK) {
                    val res = parseResult(con)
                } else {
                    Log.e("FAILED API REQUEST:", "(${con.responseCode})" + " ${con.responseMessage}")
                }
                con.disconnect()
            }catch (e: Exception) {
                Log.e("API_EXCEPTION:", "${e}")
            }
        }

    }

    suspend fun baseInfo() {
        withContext(Dispatchers.IO) {
            try {
                val con = buildConnection(FOUNDATION_INFO) as HttpURLConnection
                con.connect()

                if (con.responseCode == HttpURLConnection.HTTP_OK) {
                    val res = parseResult(con)
                } else {
                    Log.e("FAILED API REQUEST:", "(${con.responseCode})" + " ${con.responseMessage}")
                }
                con.disconnect()
            }catch (e: Exception) {
                Log.e("API_EXCEPTION:", "${e}")
            }
        }
    }
}
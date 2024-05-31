package com.example.kotlinmessenger.icdapi

import android.R.attr.password
import android.util.Log
import com.google.protobuf.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JSON
import org.json.JSONArray
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
    val REPLACE_URL: String = "http://id.who.int"

    // ICD - Entrypoints
    val FOUNDATION_INFO: String = "/icd/entity"
    val FOUNDATION_ENTITY: String = "/icd/entity/"
    val FOUNDATION_SEARCH: String = "/icd/entity/?q="

    val LINEARIZATION_BASE : String = "/icd/release/11"

    val LINERARIZATION_VERSION: String = "/2024-01"
    val LINERARIZATION_CODE: String = "/mms"

    val ALLERGIES: String = "/1991139272"
    val ILLNESS_IMMUNE : String = "/1954798891"
    val DRUGS: String = "/1170065830"
    val DRUGS_IMMUNE: String = "/373236638"

    val DESCENDANT: String = "/?include=descendant"

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
        // Log.i("ICDAPI", response.toString())
        // val temp = JSONObject(response.toString())
        return JSONObject(response.toString())
    }

    private fun makeRequest(url: String): JSONObject {
        val con = buildConnection(url) as HttpURLConnection
        con.connect()

        val response = StringBuilder()
        if (con.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(con.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

        } else {
            Log.e("ICDAPI", "(${con.responseCode})" + " ${con.responseMessage}")
            return JSONObject("FAILED")
        }
        con.disconnect()

        return JSONObject(response.toString())
    }

    private fun recTreeParsing(items: JSONArray, level: Int): Map<String, String> {
        var res = mutableMapOf<String, String>()
        // val items = json.getJSONArray("descendant")
        var recArray =  JSONArray()

        for (i in 0 until items.length()) {
            val json_obj = makeRequest(items.get(i).toString().replace(REPLACE_URL, ""))
            if (json_obj.has("foundationChildElsewhere") ) {
                // Log.e("ICDAPI", "Test")
                val lookup_array = json_obj.getJSONArray("foundationChildElsewhere")

                for (j in 0 until lookup_array.length()) {
                    val code = makeRequest(lookup_array.getJSONObject(j).get("linearizationReference").toString().replace(REPLACE_URL, ""))
                    val title = code.get("title") as JSONObject
                    if (code.get("code").toString().isNotEmpty()) {
                        res.put(code.get("code").toString(), title.get("@value").toString())
                    }else {
                        res.put(level.toString() + j.toString(), title.get("@value").toString())
                        recArray.put(lookup_array.getJSONObject(j).get("linearizationReference").toString())
                        res.putAll(recTreeParsing(recArray, level +1))
                    }
                    // Log.i("ICDAPI", "Child Element done " + title.get("@value").toString())
                }

            }else if (json_obj.get("code").toString().isNotEmpty()) {
                val title = json_obj.get("title") as JSONObject
                res.put(json_obj.get("code").toString(), title.get("@value").toString())
            }else {
                val title = json_obj.get("title") as JSONObject
                res.put(level.toString() + i.toString(), title.get("@value").toString())

                val child_arr = json_obj.get("child") as JSONArray
                res.putAll(recTreeParsing(child_arr, level +1))
            }
        }
            // TODO Error Handling
        return res
    }
    private fun parseTree(json: JSONObject): Map<String, String> {
        val res = mutableMapOf<String, String>()
        val items = json.getJSONArray("descendant")

        for (i in 0 until items.length()) {
            // Replase id.who.int with my localhost information

            // val url = items.get(i).toString().replace(REPLACE_URL, "")
            // val response = makeRequest(url)
            // val json_obj = JSONObject(response.toString())
            val json_obj = makeRequest(items.get(i).toString().replace(REPLACE_URL, ""))

            // TODO Error Handling
            if (json_obj.toString() != "FAILED") {
                // Log.e("ICDAPI", response.toString())
                // We hit the final item in the tree - Save Code + String val
                // TODO IS it still checking for child?
                if (json_obj.isNull("child")) {
                    // Log.e("ICDAPI", "We in")
                    // val title = json_obj.get("title") as JSONObject
                    // res.put(i.toString(), title.get("@value").toString())
                    //val code_array = makeRequest(json_obj.get("@id").toString().replace(REPLACE_URL, "") + DESCENDANT)
                    // val code_array = makeRequest(json_obj.get("@foundationChildElswhere"))
                    val lookup_array = json_obj.getJSONArray("foundationChildElsewhere")

                    for (j in 0 until lookup_array.length()) {
                        // val code = makeRequest(lookup_array.get(j))  // .get("linearizationReference").toString().replace(REPLACE_URL, ""))
                        val code = makeRequest(lookup_array.getJSONObject(j).get("linearizationReference").toString().replace(REPLACE_URL, ""))
                        val title = code.get("title") as JSONObject
                        res.put(code.get("code").toString(), title.get("@value").toString())
                    }

                } else {
                    // Add String that is to be printed in list
                    val title = json_obj.get("title") as JSONObject
                    res.put(i.toString(), title.get("@value").toString())
                }
            }
        }

        return res
    }

    suspend fun baseInformation() {
        withContext(Dispatchers.IO) {
            try {
                val con = buildConnection(FOUNDATION_INFO) as HttpURLConnection
                con.connect()

                if (con.responseCode == HttpURLConnection.HTTP_OK) {
                    val res = parseResult(con)
                } else {
                    Log.e("ICDAPI", "(${con.responseCode})" + " ${con.responseMessage}")
                }
                con.disconnect()
            }catch (e: Exception) {
                Log.e("ICDAPI:", "${e}")
            }
        }

    }

    suspend fun getAllergens(): Map<String, String> {
        var res = mapOf<String, String>()
        withContext(Dispatchers.IO) {
            try {
                val req = makeRequest( LINEARIZATION_BASE + LINERARIZATION_VERSION + LINERARIZATION_CODE + ALLERGIES)
                if (req.toString() != "FAILED" ){
                    res = recTreeParsing(req.getJSONArray("child"), 0)
                    // Load into checkboxes
                }else {
                    // TODO - Error Handling
                }
            }catch (e: Exception) {
                Log.e("ICDAPI:", "${e}")
            }
        }

        return res
    }

    suspend fun getDrugs(): Map<String, String> {
        var res = mapOf<String, String>()
        withContext(Dispatchers.IO) {
            try {
                val req = makeRequest( LINEARIZATION_BASE + LINERARIZATION_VERSION + LINERARIZATION_CODE + DRUGS_IMMUNE)
                if (req.toString() != "FAILED" ){
                    res = recTreeParsing(req.getJSONArray("child"), 0)
                    // Load into checkboxes
                }else {
                    // TODO - Error Handling
                }
            }catch (e: Exception) {
                Log.e("ICDAPI:", "${e}")
            }
        }

        return res
    }

    suspend fun getIllnessess(): Map<String, String> {
        var res = mapOf<String, String>()
        withContext(Dispatchers.IO) {
            try {
                val req = makeRequest( LINEARIZATION_BASE + LINERARIZATION_VERSION + LINERARIZATION_CODE + ILLNESS_IMMUNE)

                if (req.toString() != "FAILED" ){
                    res = recTreeParsing(req.getJSONArray("child"), 0)
                    // Load into checkboxes
                }else {
                    // TODO - Error Handling
                }
            }catch (e: Exception) {
                Log.e("ICDAPI:", "${e}")
            }
        }

        return res
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
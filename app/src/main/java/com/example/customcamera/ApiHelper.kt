/* Class containing two API calling functions:
    1. signInAndSendPhoto() , this function makes the auth call with email and password as params basically gets
                              three keys in response - (access_token, uid, client) which are needed for making
                              the second API call.

    2. sendTestImage() ,      This function uses the three keys received from the above function to upload
                              the file that has been passed from the ThirdActivity.

 */

package com.example.sampleapp

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import id.zelory.compressor.Compressor



class ApiHelper () {

    private var apiAccessToken : String? = null
    private  var apiUid : String? = null
    private  var apiClient : String? = null
    private val email = "amit_4@test.com"
    private val password = "12345678"
    lateinit var client : OkHttpClient


    private val BASE_URL = "http://apistaging.inito.com/api/v1/auth"


    fun signInAndSendPhoto(file : File , callback: ( error: Any?) -> Unit) {
        client = OkHttpClient()


        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()


        val request = Request.Builder()
            .url("$BASE_URL/sign_in")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        println("${request.toString()} ,,,, ${requestBody.toString()}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback( "Failed to connect to server: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("ApiClass", "Connected to server for the first API call.")
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {

                    //Below are three keys needed for the next function.
                    apiAccessToken = response.header("access-token")
                    apiUid = response.header("uid")
                    apiClient = response.header("client")
                    val jsonObject = JSONObject(responseBody)
                    val email = jsonObject.optString("email")
                    val onboarded = jsonObject.optBoolean("onboarded")
                    Log.d("ApiClass"," Token : $apiAccessToken  ,Client :  $apiClient  ,Uid : $apiUid ")
                    sendTestImage(file){ isError, err ->
                        callback(err)
                    }
                } else {
                    val errorMessage = responseBody ?: "Unknown error"
                    callback( errorMessage)
                }
            }
        })

    }

    fun sendTestImage( savedImage: File, callback: (Boolean, Any?) -> Unit) {


        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()).toString()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("test[done_date]", currentDate)
            .addFormDataPart("test[batch_qr_code]", "AAO")
            .addFormDataPart("test[reason]", "NA")
            .addFormDataPart("test[failure]", "false")
            .addFormDataPart("test[images_attributes][][pic]", savedImage.name, savedImage.asRequestBody("image/jpeg".toMediaTypeOrNull()))
            .build()

        val newApiUid : String = apiUid!!
        val newApiAccessToken : String = apiAccessToken!!
        val newApiClient : String = apiClient!!
        val request =
            Request.Builder()
                .url("http://apistaging.inito.com/api/v1/tests")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("access-token", newApiAccessToken)
                .addHeader("uid", newApiUid)
                .addHeader("client", newApiClient)
                .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(true, "Failed to connect to server: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("ApiClass", "Reached onResponse of second API call.")

                if (response.isSuccessful) {
                    callback(false, null)
                } else {
                    val responseBody = response.body?.string() ?: "Unknown error"
                    callback(true, responseBody)
                }
            }
        })
    }



}
package com.example.customcamera

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ThirdActivity : AppCompatActivity() {

    private lateinit var testStatusField: TextView
    private lateinit var timerTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var timerProgressBar: ProgressBar


<<<<<<< HEAD

    private var camera: Camera? = null



=======
    private var camera: Camera? = null


>>>>>>> 6a46fcf (Done with first and second screen)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

//        testStatusField = findViewById(R.id.textViewTestStatus)
//        timerTextView = findViewById(R.id.textViewTimer)
//        timerProgressBar = findViewById(R.id.progressBarTimer)
//
//        testStatusField.setText("Capturing Single Image")
//
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//            != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                arrayOf(Manifest.permission.CAMERA),
//                200);
//            // Permission is not granted
//        }
//        //initializeCameraAndCaptureImage()
//        //timerTextView.setText("301")
//        // Start the countdown timer with an interval of 5 seconds
//        countDownTimer = object : CountDownTimer(301000, 5000) {
//            override fun onTick(millisUntilFinished: Long) {
//                // Update the circular timer and progress bar
//                val secondsRemaining = millisUntilFinished / 1000
//                timerProgressBar.progress = secondsRemaining.toInt()
//                println("Callling the 2 funcs")
//                updateCircularTimer(secondsRemaining)
//                updateProgressBar(secondsRemaining)
//            }
//
//            override fun onFinish() {
//                // Move to Screen 3
//                startActivity(Intent(this@ThirdActivity, SecondActivity::class.java))
//                finish()
//            }
//        }.start()
//
//    }
//
//
//    private val pictureCallback = Camera.PictureCallback { data, camera ->
//        try {
//            println("Entered callback")
//            val pictureFile = getOutputMediaFile()
//            println("outputmedia reached")
//            val fos = FileOutputStream(pictureFile)
//            fos.write(data)
//            fos.close()
//            Log.d(TAG, "Image saved to gallery: ${pictureFile.absolutePath}")
//        } catch (e: IOException) {
//            Log.e(TAG, "Error saving image: ${e.message}")
//        } finally {
//            camera.release()
//            //camera = null
//        }
//    }
//
//    fun initializeCameraAndCaptureImage() {
//        try {
//            println("about to camera oopen**********************")
//            camera = Camera.open(0)
//            val params = camera!!.parameters
//           //params.iso = 100
//            params.focusMode = Camera.Parameters.FOCUS_MODE_FIXED
//            camera!!.parameters = params
//            camera!!.startPreview()
//            println("${camera!!.parameters}")
//            println("about to call callback**********************")
//            camera!!.takePicture(null, null, pictureCallback)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error initializing camera: ${e.message}")
//        }
//    }
//
//    fun getOutputMediaFile(): File {
//        val mediaStorageDir = File(
//            Environment.getExternalStoragePublicDirectory(
//            Environment.DIRECTORY_PICTURES), "MyCameraApp")
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                Log.e(TAG, "Failed to create directory")
//                throw IOException("Failed to create directory")
//            }
//        }
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        return File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
//    }
//
//    private fun updateCircularTimer(secondsRemaining: Long) {
//        // Calculate the percentage of the timer that has elapsed
//        val percentageElapsed = ((300 - secondsRemaining) / 300f) * 100
//
//        // Update the circular timer
//        timerTextView.text = "${secondsRemaining}s"
//        timerTextView.setTextColor(ContextCompat.getColor(this, R.color.purple_500))
//        timerProgressBar.progress = percentageElapsed.toInt()
//    }
//
//    private fun updateProgressBar(secondsRemaining: Long) {
//        // Update the progress bar
//        timerProgressBar.progress = secondsRemaining.toInt()
//    }

<<<<<<< HEAD
=======
    }
>>>>>>> 6a46fcf (Done with first and second screen)
}
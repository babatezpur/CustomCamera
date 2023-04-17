package com.example.customcamera

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.sampleapp.ApiHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory

class ThirdActivity : AppCompatActivity(), SurfaceHolder.Callback, Camera.PictureCallback {
    private val TAG = "ThirdActivity"
    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var testStatusText: TextView
    private var camera: Camera? = null
    private lateinit var pictureDirectory: File
    private var currentExposureValue = -12
    private lateinit var parameters : Camera.Parameters


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        testStatusText = findViewById(R.id.testStatusText)
        surfaceView = findViewById(R.id.surface_view)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        supportActionBar?.title = "Taking multiple photos"



        // Create picture directory in gallery
        val galleryDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        println(galleryDirectory.absolutePath.toString())
        pictureDirectory = File(galleryDirectory, "MyCameraApp")
        println(pictureDirectory.absolutePath.toString())
        if (!pictureDirectory.exists()) {
            pictureDirectory.mkdir()
        }


    }


    // Function to capture Image. Gets called everytime with the ISO value is incremented.
    private fun captureNextImage() {
        camera = Camera.open()
        parameters = camera?.parameters!!

        parameters?.set("iso", "100")
        parameters?.focusMode = Camera.Parameters.FOCUS_MODE_FIXED
        parameters?.focusAreas = null
        //parameters.focusMode = Camera.Parameters.FOCUS_MODE_INFINITY
        camera?.parameters = parameters
        camera?.setPreviewDisplay(surfaceHolder)
        camera?.startPreview()
        if (currentExposureValue > 12) {
            // All images captured, send closest to mean exposure value to backend
            Log.d(TAG,"All pictures captures, calling sendClosestImage()")
            sendClosestImage()
            return
        }
        testStatusText.text = "Capturing Image for Exposure: $currentExposureValue"
        val parameters = camera?.parameters
        parameters?.exposureCompensation = currentExposureValue
        parameters?.setRotation(90)
        camera?.parameters = parameters
        try{
            camera?.takePicture(null, null, this)
        }catch(e:java.lang.NullPointerException){
            println(e)
        }
    }


    //Function which finds the image file (whose ev value is closest to mean value) and then...
    //...sends that image for uploading.
    private fun sendClosestImage() {
        stopCamera()
        testStatusText.text = "Sending Image"
        val filesAll = pictureDirectory.listFiles()?.toList()
        val files = filesAll?.takeLast((2*currentExposureValue)-1)
        if (files.isNullOrEmpty()) {
            Log.e(TAG, "No image files found")
            testStatusText.text = "Test Failed"
            return
        }
        Log.d(TAG,"Total files present in list : ${files.size}")
        val exposureValues = mutableListOf<Int>()
        var exposureValueSum = 0

        for (file in files) {
            val fileNameWithoutExtension = file.nameWithoutExtension
            val list = fileNameWithoutExtension.split("EV")
            val ev = list[1].toInt()
            exposureValues.add(ev)
            exposureValueSum += ev
        }
        Log.d(TAG , "Mean exposure value : $exposureValueSum")
        val exposureValueMean = exposureValueSum / exposureValues.size
        Log.d(TAG , "Mean exposure value : $exposureValueSum")
        Log.d(TAG,"Calling getClosestFile")
        val closestFile = getClosestFile(files, exposureValueMean)
        if (closestFile == null) {
            Log.e(TAG, "Failed to find file with closest exposure value")
            testStatusText.text = "Test Failed"
            return
        }
        println("Closest file : ${closestFile.absolutePath}")
            uploadImage(closestFile)
        //testStatusText.text = "Test Done"
    }

    //Function that simply traverses through the list of files and finds the file with ev value...
    //... closes to the mean exposure value.
    private fun getClosestFile(files: List<File>, exposureValue: Int): File? {
        var closestFile: File? = null
        var closestDiff = Integer.MAX_VALUE
        for (file in files) {
            val fileNameWithoutExtension = file.nameWithoutExtension
            val list = fileNameWithoutExtension.split("EV")
            val ev = list[1].toInt()
            val diff = kotlin.math.abs(ev - exposureValue)
            if (diff < closestDiff) {
                closestDiff = diff
                closestFile = file
            }
        }
        return closestFile
    }

    //An alternate definition of uploadImage() which involves compressing photos
    /*
    private fun uploadImage(file : File){

        val api = ApiHelper()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val compressedFile = Compressor.compress(this@ThirdActivity,   file)
            println("${compressedFile.length()} , and space: ${compressedFile.totalSpace}")

            api.signInAndSendPhoto(compressedFile) { error ->
                if (error != null) {
                    println("Photo Sending Failed: $error")
                    testStatusText.text = "Test Failed"
                } else {
                    testStatusText.text = "Test Done"
                }

            }
        }

    }
   */


    //Function that makes the two api calls. Both the calls have been merged into a single...
    //... call and a single callback function is called from the API class.
    private fun uploadImage(file : File){
        val api = ApiHelper()
        api.signInAndSendPhoto(file) { error ->
            if (error != null) {
                println("Photo Sending Failed: $error")
                testStatusText.text = "Test Failed"
            } else {
                testStatusText.text = "Test Done"
            }

        }

    }


    private fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyCameraApp"
        )

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory.")
                return null
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + "EV" + currentExposureValue +".jpg")
    }


    //Callback function which is called right after the takePicture function. It calls...
    //...the getOutputMediaFile and once it recieves the File objects, stores the image in it.
    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        println("Reached callback")
        val pictureFile = getOutputMediaFile()
        if (pictureFile == null) {
            Log.e(TAG, "Failed to create image file")
            return
        }

        try {
            val outputStream = FileOutputStream(pictureFile)
            outputStream.write(data)
            outputStream.close()
            Log.d(TAG, "Image saved to: ${pictureFile.absolutePath}")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save image", e)
        }

        val uri = Uri.fromFile(pictureFile)
        MediaScannerConnection.scanFile(
            this,
            arrayOf(pictureFile.toString()),
            arrayOf("image/jpeg"),
            null
        )

        // Update test status text
        val exposure = currentExposureValue.toString()
        val testStatus = "Capturing Image for Exposure: $exposure"
        runOnUiThread {
            testStatusText.text = testStatus
        }

        currentExposureValue += 1
        captureNextImage()
    }


    //Callback functions for SurfaceHolder
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            println("surface of 3 created")
            captureNextImage()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        //Does nothing
        println("SURFACE CHNGED 3rd ACTIVITY")
        //captureNextImage()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        println("SURFACE DESTROYED 3rd ACTIVITY")
        stopCamera()
    }

    private fun stopCamera() {
        camera?.stopPreview()
        camera
        camera?.release()
        camera = null
    }

}
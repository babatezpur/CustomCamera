package com.example.customcamera

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SecondActivity : AppCompatActivity(), SurfaceHolder.Callback, Camera.PictureCallback {

    private val TAG : String = "SecondActivity"
    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var countDownTimer : CountDownTimer
    private lateinit var timerProgressBar: ProgressBar

    private var camera: Camera? = null
    private var timerHandler: Handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var timerSeconds = 300
    private lateinit var percentTextview : TextView
    private val currentExposureValue = 0
    private val REQUEST_CAMERA_PERMISSION_CODE = 1


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        supportActionBar?.title = "Taking single photo"

        surfaceView = findViewById(R.id.surface_view)
        progressBar = findViewById(R.id.progress_bar)
        statusTextView = findViewById(R.id.status_text_view)
        timerTextView = findViewById(R.id.timerTextView)
        timerProgressBar = findViewById(R.id.progress_bar)
        percentTextview = findViewById(R.id.percentTextView)

        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        statusTextView.text = "Capturing Single Image"

    }


    //Callback functions for SurfaceHolder
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG,"Surface created")
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION_CODE
            )
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG,"Surface changed")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG,"Surface destroyed")
        stopCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission required for this app to work",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }


    //Function to set the camera configs and capture the image.
    private fun startCamera() {
        try {
            Log.d(TAG,"Starting Camera (open)")
            camera = Camera.open()
            val parameters = camera?.parameters
            parameters?.set("iso", "100")
            parameters?.focusMode = Camera.Parameters.FOCUS_MODE_FIXED
            camera?.parameters = parameters
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
            camera?.autoFocus { success, camera ->
                if (success) {
                    camera.takePicture(null, null, this)
                } else {
                    Toast.makeText(
                        this,
                        "Unable to focus. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open camera: ${e.message}")
            Toast.makeText(this, "Failed to open camera", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun stopCamera() {
        camera?.stopPreview()
        camera
        camera?.release()
        camera = null
    }


    //Callback function which is called right after the takePicture function. It calls...
    //...the getOutputMediaFile and once it recieves the File objects, stores the image in it.
    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        if (data == null) {
            Toast.makeText(this, "Unable to capture image", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        stopCamera()
        val pictureFile = getOutputMediaFile()
        if (pictureFile == null) {
            Toast.makeText(
                this,
                "Failed to create directory to save image",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()
            Log.d(TAG, "Image saved to: ${pictureFile.absolutePath}")

            //val uri = Uri.fromFile(pictureFile)
            MediaScannerConnection.scanFile(
                this,
                arrayOf(pictureFile.toString()),
                arrayOf("image/jpeg"),
                null
            )
            statusTextView.text = "Image Captured"
            Log.d(TAG,"Starting Timer")
            startTimer()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image: ${e.message}")
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    //Function to start timer. Has two callback methods:
    //onTick: gets called after every interval
    //onFinish: gets called after the timer has completed the countdown.
    private fun startTimer() {
        val totalTime = 11000L // 60 seconds
        val interval = 5000L // 5 seconds
        countDownTimer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the circular timer and progress bar
                val progress = ((totalTime - millisUntilFinished) / interval).toInt()
                timerProgressBar.progress = progress

                val secondsRemaining = millisUntilFinished / 1000
                updateCircularTimer(secondsRemaining)

            }

            override fun onFinish() {
                // Move to Screen 3
                startActivity(Intent(this@SecondActivity,ThirdActivity::class.java))
                stopCamera()
                finish()
            }
        }.start()



    }

    private fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable!!)
        timerRunnable = null
        timerSeconds = 300
        timerProgressBar.progress = 100
        timerProgressBar.visibility = View.GONE
    }

    private fun updateCircularTimer(secondsRemaining: Long) {
        // Calculate the percentage of the timer that has elapsed
        val percentageElapsed = ((300 - secondsRemaining) / 300f) * 100
        val roundedPercentage = "%.2f".format(percentageElapsed)

        // Update the timer
        timerTextView.text = "${secondsRemaining}s left"
        timerTextView.setTextColor(ContextCompat.getColor(this, R.color.purple_500))
        timerProgressBar.progress = percentageElapsed.toInt()
        percentTextview.text = "${roundedPercentage} %"
    }

    //Function to get a file where image data is ultimately stored.
    private fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyCameraApp"
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + "EV" + currentExposureValue +".jpg")
    }
}



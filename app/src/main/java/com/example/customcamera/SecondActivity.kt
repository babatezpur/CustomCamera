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

    private val REQUEST_CAMERA_PERMISSION_CODE = 1


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)


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

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            println("surface created")
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
        //Does nothing...
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
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

    private fun startCamera() {
        println("start camera entered")
        try {
            println("start camera entered2")
            camera = Camera.open()
            val parameters = camera?.parameters
            parameters?.set("iso", "100")
            parameters?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            camera?.parameters = parameters
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
            camera?.autoFocus { success, camera ->
                if (success) {
                    println("take pixture called")
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
            Log.e("MainActivity", "Failed to open camera: ${e.message}")
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



    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        if (data == null) {
            Toast.makeText(this, "Unable to capture image", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        println("Inside onpictaken")
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
            println("pic saved")
            Log.d("MainActivity", "Image saved to: ${pictureFile.absolutePath}")

            val uri = Uri.fromFile(pictureFile)
            MediaScannerConnection.scanFile(
                this,
                arrayOf(pictureFile.toString()),
                arrayOf("image/jpeg"),
                null
            )
            statusTextView.text = "Image Captured"
            println("About to start timer")
            startTimer()

        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to save image: ${e.message}")
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startTimer() {
        val totalTime = 301000L // 60 seconds
        val interval = 5000L // 1 second
        countDownTimer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the circular timer and progress bar
                val progress = ((totalTime - millisUntilFinished) / interval).toInt()
                timerProgressBar.progress = progress



                val secondsRemaining = millisUntilFinished / 1000
                updateCircularTimer(secondsRemaining)

                //timerProgressBar.progress = secondsRemaining.toInt()
                //println("Callling the 2 funcs")
//                updateCircularTimer(secondsRemaining)
//                updateProgressBar(secondsRemaining)
            }

            override fun onFinish() {
                // Move to Screen 3
                startActivity(Intent(this@SecondActivity,ThirdActivity::class.java))
                finish()
            }
        }.start()



        /*
        progressBar.visibility = View.VISIBLE
        timerRunnable = object : Runnable {
            override fun run() {
                if (timerSeconds > 0) {
                    timerSeconds -= 5
                    progressBar.progress = (timerSeconds * 100) / 300
                    val remainingSeconds = timerSeconds
                    timerTextView.text = "$remainingSeconds seconds left"


                    timerHandler.postDelayed(this, 5000)
                } else {
                    progressBar.visibility = View.GONE
                    statusTextView.text = "Timer Finished"
                    goToScreen3()
                }
            }
        }
        timerHandler.postDelayed(timerRunnable!!, 5000)
        */
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

        // Update the circular timer
        timerTextView.text = "${secondsRemaining}s"
        timerTextView.setTextColor(ContextCompat.getColor(this, R.color.purple_500))
        timerProgressBar.progress = percentageElapsed.toInt()
        percentTextview.text = "${roundedPercentage} %"
    }

    private fun updateProgressBar(secondsRemaining: Long) {
        // Update the progress bar
        timerProgressBar.progress = secondsRemaining.toInt()
    }

    private fun goToScreen3() {
        val intent = Intent(this, ThirdActivity::class.java)
        startActivity(intent)
        finish()
    }

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
        return File("${mediaStorageDir.path}${File.separator}IMG_${timeStamp}.jpg")
    }
}



package com.example.myapplication


import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.widget.LinearLayout
import kotlin.math.sqrt
import android.hardware.camera2.CameraAccessException
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Vibrator
import android.util.Log
import com.example.myapplication.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var relativeLayout: LinearLayout

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        relativeLayout = findViewById(R.id.relative_layout)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        Objects.requireNonNull(sensorManager)!!.registerListener(sensorListener, sensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        @SuppressLint("ResourceAsColor")
        override fun onSensorChanged(event: SensorEvent) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 5) {
                flashLightOn()
                vibrateOnce()
                relativeLayout.background.setTint(R.color.white)
            } else
                flashLightOff()
                relativeLayout.background.setTint(R.color.black)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun vibrateOnce() {
        val vibrator = application.getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(100)
    }

    private fun flashLightOn() {
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {

            val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            try {
                val cameraId = cameraManager.cameraIdList[0]
                cameraManager.setTorchMode(cameraId, true)
            } catch (e: CameraAccessException) {
                Log.e("Camera Problem", "Cannot turn on camera flashlight")
            }
        }
    }

    private fun flashLightOff() {
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            try {
                val cameraId = cameraManager.cameraIdList[0]
                cameraManager.setTorchMode(cameraId, false)
            } catch (e: CameraAccessException) {
                Log.e("Camera Problem", "Cannot turn off camera flashlight")
            }
        }
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }
}

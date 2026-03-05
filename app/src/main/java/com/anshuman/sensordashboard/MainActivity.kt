package com.anshuman.sensordashboard

import android.content.Intent
import android.content.IntentFilter
import android.hardware.*
import android.location.Location
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var accelerometerText: TextView
    lateinit var gyroscopeText: TextView
    lateinit var magnetometerText: TextView
    lateinit var gpsText: TextView
    lateinit var eventText: TextView
    lateinit var batteryText: TextView

    lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var magnetometer: Sensor? = null

    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        accelerometerText = findViewById(R.id.accelerometerText)
        gyroscopeText = findViewById(R.id.gyroscopeText)
        magnetometerText = findViewById(R.id.magnetometerText)
        gpsText = findViewById(R.id.gpsText)
        eventText = findViewById(R.id.eventText)
        batteryText = findViewById(R.id.batteryText)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000,
                1f
            ) { location: Location ->

                val lat = location.latitude
                val lon = location.longitude

                gpsText.text = "GPS\nLat: $lat\nLon: $lon"
            }

        } catch (e: SecurityException) {
            gpsText.text = "GPS Permission Required"
        }

        val batteryIntent = registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        val batteryPct = level!! * 100 / scale!!

        batteryText.text = "Battery Level: $batteryPct%"
    }

    override fun onResume() {
        super.onResume()

        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        magnetometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            accelerometerText.text = "Accelerometer\nX: $x\nY: $y\nZ: $z"

            val acceleration = sqrt((x*x + y*y + z*z).toDouble())

            when {
                acceleration > 25 -> eventText.text = "Crash Detected!"
                acceleration > 18 -> eventText.text = "Harsh Acceleration"
                acceleration < 3 -> eventText.text = "Hard Brake"
                else -> eventText.text = "Normal Driving"
            }
        }

        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            gyroscopeText.text = "Gyroscope\nX: $x\nY: $y\nZ: $z"
        }

        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            magnetometerText.text = "Magnetometer\nX: $x\nY: $y\nZ: $z"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}
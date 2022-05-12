package com.example.sensorapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.Sensor.*
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(),LocationListener ,SensorEventListener  {

    private lateinit var LocationText : TextView
    private lateinit var RotateSqu : TextView
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private var progressBar: ProgressBar? = null
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var mSensor: Sensor? = null
    private lateinit var LightText: TextView
    private var PresureButton: Button? =null
    private var HumilityButton: Button? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById<ProgressBar>(R.id.pBar) as ProgressBar
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        PresureButton = findViewById(R.id.PresureButton) as Button
        HumilityButton = findViewById(R.id.HumilityButton) as Button
        LightText = findViewById(R.id.LightNumber) as TextView
        RotateSqu = findViewById(R.id.RotateSqu)

        //Reagować na zmianę jasności w pomieszczeniu - 0,33pkt
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        LightText.setText(TYPE_LIGHT.toString())
        progressBar!!.progress = TYPE_LIGHT

        //Reagować na zmianę położenia urządzenia - 0,33pkt
        mSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also{
            sensorManager!!.registerListener(this, it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST)
        }

        //Dowolne dwa inne sensory niż powyższe, lub ciekawe funkcjonalności - każdy po 0,33pkt
        PresureButton!!.setOnClickListener{
            Toast.makeText(this, "Ciśnienie wynosi: $TYPE_PRESSURE", Toast.LENGTH_SHORT).show()
        }
        HumilityButton!!.setOnClickListener{
            Toast.makeText(this, "Wilgotność wynosi: $TYPE_RELATIVE_HUMIDITY", Toast.LENGTH_SHORT).show()
        }

        //Działać w tle i co minutę podawać komunikat z aktualną temperaturą telefonu - 0,33pkt
        GlobalScope.launch{
            temperature()
        }
        //Pokazać współrzędne użytkownika - 0,33pkt
        getLocation()
    }


    suspend fun temperature(){
        while (true){
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this , "Temperature wynosi: " +
                        TYPE_AMBIENT_TEMPERATURE.toString(),
                        Toast.LENGTH_SHORT).show()
            }
            Thread.sleep(1_0000)  // wait for 1 second
        }
    }


    override fun onSensorChanged(event: SensorEvent) {

        if (event.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            RotateSqu.apply{
                rotationX = event.values[1] * 3f
                rotationY = event.values[0] * 3f
                rotation = -event.values[0]
                translationX = event.values[0] * -10
                translationY = event.values[1] * 10
            }
        }

        if (event.sensor?.type == Sensor.TYPE_LIGHT) {
            progressBar!!.progress =event.values[0].toInt()
            LightText.setText(event.values[0].toString())
        }

    }


    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT),
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }


    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }


    @SuppressLint("SetTextI18n")
    override fun onLocationChanged(location: Location) {
        LocationText = findViewById(R.id.LocationNumber) as TextView
        LocationText.setText("x = " + location.latitude.roundToInt().toString()+
                " y = " + location.longitude.roundToInt().toString())
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
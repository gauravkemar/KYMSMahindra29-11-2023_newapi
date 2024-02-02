package com.kemarport.kymsmahindra.activity.newactivity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.util.MapUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.databinding.ActivityNavigateBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Gps
import com.kemarport.kymsmahindra.helper.toLatLong
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.util.*


class NavigateVehicleActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {


    lateinit var binding: ActivityNavigateBinding
    var TamWorth = LatLng(-31.083332, 150.916672)
    var NewCastle = LatLng(-32.916668, 151.750000)
    var Brisbane = LatLng(-27.470125, 153.021072)


    var modelCode: String = ""
    var colorCode: String = ""
    var vinNo: String = ""


    var t = Timer()
    var tt: TimerTask? = null
    private var vehicleLocation: Location? = null
    private var currentLocation: Location? = null

    var currentMarker: Marker? = null
    var vehicleMarker: Marker? = null
    private var flagCurrentLoc = true


    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private var accelerometer: Sensor? = null
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private val alpha = 0.97f // Adjust this value based on your requirements


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_navigate)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding.searchVehicleToolbar.title = "Navigate"
        binding.searchVehicleToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.searchVehicleToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*        val log = intent.getDoubleExtra(Constants.LONGITUDE, 0.0)
                val lat = intent.getDoubleExtra(Constants.LATITUDE, 0.0)  */

        val log = 72.987958
        val lat = 19.236250

        /*  modelCode = intent.getStringExtra(Constants.ModelCode).toString()
          colorCode = intent.getStringExtra(Constants.ColorCode).toString()
          vinNo = intent.getStringExtra(Constants.VinNo).toString()  */

        modelCode = "abc"
        colorCode = "abc"
        vinNo = "abc"

        vehicleLocation = Location("Vehicle location")
        vehicleLocation?.latitude = lat
        vehicleLocation?.longitude = log


        requestLocationPermission()
        binding.btnRecenter.setOnClickListener {
            currentLocation?.let {
                it.toLatLong().let { latLng ->
                    CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        22f
                    )
                }.let { cameraUpdate ->
                    googleMap.moveCamera(
                        cameraUpdate
                    )
                }
            }

        }

        binding.apply {
            if (!vinNo.isEmpty() || vinNo != "null") {
                tvVinValue.setText(vinNo)
            }
            if (!modelCode.isEmpty() || vinNo != "null") {
                tvModelValue.setText(modelCode)
            }
            if (!colorCode.isEmpty() || vinNo != "null") {
                tvColorValue.setText(colorCode)
            }
        }

        /*  if (currentMarker != null) {
              currentMarker!!.remove()
              val currentPos = LatLng(newLat, newLng)
              googleMap?.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
              googleMap?.animateCamera(CameraUpdateFactory.zoomTo(16f))
          }*/
        tt = object : TimerTask() {
            override fun run() {
                getLocationNew()
            }
        }

        t.scheduleAtFixedRate(tt, 3000, 3000)


        /////direction
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        try {

            if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null || sensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER
                ) != null
            ) {
                magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
                accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
            } else {
                magnetometer = null
                accelerometer = null
            }


        } catch (e: Exception) {

        }


        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlertMessage()
        }
    }

    private fun showAlertMessage() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("The location permission is disabled. Do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    10
                )
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun addCursorMarker(position: LatLng) {
        val markerBitmap = generateLocationIcon()
        currentMarker?.remove()
        currentMarker = googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title("Current Position")
                .icon(markerBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
                .flat(true) // Make the marker flat to rotate it
                .rotation(getAzimuth())
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                //startActivity(Intent(this@UnloadingConfirmationActivity,SearchVehicleActivity::class.java))
                //finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun getLocationNew() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        runOnUiThread(Runnable {
            val gps = Gps(this)
            if (gps.canGetLocation()) {
                newLat = gps.getLatitude()
                newLng = gps.getLongitude()
                val bearing = calculateBearing(
                    LatLng(newLat, newLng),
                    LatLng(vehicleLocation!!.latitude, vehicleLocation!!.longitude)
                )

                // Rotate the camera based on the calculated bearing
                googleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(newLat, newLng))
                            .bearing(bearing)
                            .tilt(15f) // Optional: Adjust the tilt as needed
                            .zoom(googleMap.cameraPosition.zoom) // Maintain the current zoom level
                            .build()
                    )
                )
                if (flagCurrentLoc) {
                    recentr()
                    flagCurrentLoc = false
                }
                if (gps != null) {
                    currentLocation = gps.location
                    Log.e("currentLoc", currentLocation.toString())
                    if (::googleMap.isInitialized) {
                        drawLines()
                    }
                } else {
                    requestLocation()
                }
            }
        })
    }

    fun recentr() {
        if (currentMarker != null) currentMarker!!.remove()
        val currentPos = LatLng(newLat, newLng)
        if (currentPos != null && googleMap != null) {
            googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
            googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(45f))
            println("la-$newLat, $newLng")
        }
    }

    fun generateLocationIcon(): Bitmap? {
        val height = 40
        val width = 40
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.navigation_arrow)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    /*    fun generateLocationIcon(rotation: Float): Bitmap? {
            val height = 40
            val width = 40
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.navigation_arrow)

            val matrix = Matrix()
            matrix.postRotate(rotation)

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }*/

    override fun onResume() {
        super.onResume()
        if (t == null) {
            t = Timer()
            tt = object : TimerTask() {
                override fun run() {
                    getLocationNew()
                }
            }
            t.scheduleAtFixedRate(tt, 3000, 3000)
        }
        flagCurrentLoc = true

        //direction
        if (magnetometer != null || accelerometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

    }

    fun generateCarLocationIcon(carImage: Int): Bitmap? {
        val height = 40
        val width = 40
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_car)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private lateinit var googleMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Inside your activity or fragment
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, proceed with getting location
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getLocation()
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private fun requestLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000 // 10 seconds

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    // Handle the location update here
                    if (location != null) {
                        updateLocation(location)
                    }
                }
            },
            null
        )
    }

    private fun updateLocation(location: Location) {
        // Handle the location update here
        /*val latitude = location.latitude

        val longitude = location.longitude
        currentLocation?.latitude = latitude
        currentLocation?.longitude = longitude*/

        currentLocation = location
        if (::googleMap.isInitialized) {
            drawLines()
        }
    }


    private fun getLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Use LocationManager to get location
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    currentLocation = location
                    if (::googleMap.isInitialized) {
                        drawLines()
                    }
                } else {
                    requestLocation()
                }
            }
        } else {
            // GPS is not enabled, prompt the user to enable it
            /*val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)*/
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        //googleMap.clear()
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        //drawLines()
        runOnUiThread {
            recentr()
        }


    }

    override fun onPause() {
        super.onPause()
        if (t != null) {
            t.cancel()
            tt!!.cancel()

        }
        //direction
        sensorManager.unregisterListener(this)
    }

    private fun drawLines() {
        googleMap.clear()
        currentLocation?.let {

            val startPoint = LatLng(
                currentLocation?.latitude!!,
                currentLocation?.longitude!!
            ) // Example start point
            val endPoint = LatLng(
                vehicleLocation?.latitude!!,
                vehicleLocation?.longitude!!
            )// Example end point
            googleMap.addPolyline(
                PolylineOptions().add(startPoint, endPoint)
                    .width
                        (5f)
                    .color(Color.RED)
                    .geodesic(true)
            )
            //setMarkerIcon(currentMarkerRotation)
            addCursorMarker(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))

            /*  val markerOptions =
                  MarkerOptions().position(
                      LatLng(
                          currentLocation?.latitude!!,
                          currentLocation?.longitude!!
                      )
                  ).title("You are here!")
                      .icon(BitmapDescriptorFactory.fromBitmap(generateLocationIcon()!!))
                      .rotation(currentMarkerRotation)

              currentMarker = googleMap?.addMarker(markerOptions)*/
            //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 16f))

            /*           val vehicleOption =
                           MarkerOptions().position(
                               LatLng(
                                   vehicleLocation?.latitude!!,
                                   vehicleLocation?.longitude!!
                               )
                           ).title("You are here!")
                               .icon(BitmapDescriptorFactory.fromBitmap(generateCarLocationIcon(R.drawable.ic_car)!!))*/

            if (modelCode != "null" || modelCode.isNotEmpty() && colorCode != "null" || colorCode.isNotEmpty()) {
                val vehicleOption = MarkerOptions().position(
                    LatLng(
                        vehicleLocation?.latitude!!,
                        vehicleLocation?.longitude!!
                    )
                ).title("You are here!")
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            generateCarLocationIcon(
                                modelCode,
                                colorCode
                            )!!
                        )
                    )
                vehicleMarker = googleMap?.addMarker(vehicleOption)
            } else {

                val vehicleOption = MarkerOptions().position(
                    LatLng(
                        vehicleLocation?.latitude!!,
                        vehicleLocation?.longitude!!
                    )
                ).title("You are here!")
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            generateCarLocationIcon(
                                "XUV 400",
                                "Red"
                            )!!
                        )
                    )
                vehicleMarker = googleMap?.addMarker(vehicleOption)

            }


            //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 16f))

            val distance: Float = roundOff(
                currentLocation?.latitude!!,
                currentLocation?.longitude!!,
                vehicleLocation?.latitude!!,
                vehicleLocation?.longitude!!
            )?.toFloat()!!

            binding.tvTotalDistance.setText("$distance Meters Approx.")
            if (distance > 10f && distance < 15f) {
                audio(1000, true)
            } else if (distance > 5f && distance < 10f) {
                audio(500, true)
            } else if (distance > 2f && distance < 5f) {
                audio(333, true)
            } else if (distance < 0.5f) {
                audio(100, true)
                if (showAlert) showAlertOnce()
            } else {
                audio(0, false)
            }
        }
    }

    /* private fun setMarkerIcon(){
         val markerOptions =
             MarkerOptions().position(
                 LatLng(
                     currentLocation?.latitude!!,
                     currentLocation?.longitude!!
                 )
             ).title("You are here!")
                 .icon(BitmapDescriptorFactory.fromBitmap(generateLocationIcon()!!))
                 .rotation(currentMarkerRotation)

         currentMarker = googleMap?.addMarker(markerOptions)
     }*/

    override fun onDestroy() {
        super.onDestroy()
        toneGen1?.let {
            it.stopTone()
            handler.removeCallbacksAndMessages(null)
            it.release()
        }
        toneGen1 = null
        if (t != null) {
            t.cancel()
            tt!!.cancel()
        }
    }

    /*private fun isWithinRange(userLocation: Location, targetLocation: Location): Boolean {
        // Customize this function to define your "arrival" criteria
        val distance = userLocation.distanceTo(targetLocation)
        return distance <= TARGET_RADIUS_METERS
    }

    private fun vibrateDevice() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // For devices running older Android versions
            vibrator.vibrate(1000)
        }
    }*/


    fun roundOff(lastLat: Double, lastLng: Double, lat: Double, lng: Double): String? {
        val results = FloatArray(1)
        Location.distanceBetween(
            lastLat, lastLng,
            lat, lng,
            results
        )
        //Toast.makeText(getActivity(), "Distance between Point A to B is : " + results[0], Toast.LENGTH_SHORT).show();
        return if (results[0] == 0f) "0.0" else String.format("%.2f", results[0])
    }

    fun audio(delay: Long, audio: Boolean) {
        handler.removeMessages(0)
        if (audio) {
            handler.postDelayed(object : Runnable {
                override fun run() {
                    toneGen1?.let {
                        toneGen1?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                        handler.postDelayed(this, delay)
                    }
                }
            }, 0)
        } else {
            handler.removeMessages(0)
        }
    }

    var newLat = 0.0
    var newLng = 0.0
    var toneGen1: ToneGenerator? = null

    val handler = Handler(Looper.getMainLooper())
    var showAlert = true
    fun showAlertOnce() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert!")
        alertDialog.setMessage("You have reached the location!")
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(
            "Okay"
        ) { dialog, which -> //tt.cancel();
            if (handler != null) handler.removeMessages(0)
        }
        alertDialog.show()
        showAlert = false
    }


    companion object {
        // Define the target location and arrival radius here
        private val targetLocation = Location("").apply {
            latitude = 12.34
            longitude = 56.78
        }
        private const val TARGET_RADIUS_METERS = 100.0
    }

    fun calculateBearing(start: LatLng, end: LatLng): Float {
        val startLat = Math.toRadians(start.latitude)
        val startLng = Math.toRadians(start.longitude)
        val endLat = Math.toRadians(end.latitude)
        val endLng = Math.toRadians(end.longitude)

        val deltaLng = endLng - startLng

        val y = sin(deltaLng) * cos(endLat)
        val x = cos(startLat) * sin(endLat) - sin(startLat) * cos(endLat) * cos(deltaLng)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        bearing = (bearing + 360) % 360 // Normalize to [0, 360)

        return bearing
    }


    fun generateCarLocationIcon(modelCode: String, colorDescription: String): Bitmap? {
        val height = 50
        val width = 50
        val resourceId = getModelColorResourceId(modelCode, colorDescription)
        val bitmap = BitmapFactory.decodeResource(resources, resourceId)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun getModelColorResourceId(modelCode: String, colorDescription: String): Int {
        val modelColorMap = mapOf(
            "XUV300" to mapOf(
                "Red" to R.drawable.xuv300red,
                "Black" to R.drawable.xuv300black,
                "White" to R.drawable.xuv300white,
                "Blue" to R.drawable.xuv300blue
            ),
            "XUV500" to mapOf(
                "Red" to R.drawable.xuv500red,
                "Black" to R.drawable.xuv500black,
                "White" to R.drawable.xuv500white,
                "Blue" to R.drawable.xuv500blue
            ),
            "XUV700" to mapOf(
                "Red" to R.drawable.xuv700red,
                "Black" to R.drawable.xuv700black,
                "White" to R.drawable.xuv700white,
                "Blue" to R.drawable.xuv700blue
            )
        )
        return modelColorMap[modelCode]?.get(colorDescription) ?: R.drawable.ic_car
    }

    // direction

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this example
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor == accelerometer) {
                applyLowPassFilter(event.values, lastAccelerometer)
                lastAccelerometerSet = true
            } else if (event.sensor == magnetometer) {
                applyLowPassFilter(event.values, lastMagnetometer)
                lastMagnetometerSet = true
            }

            if (lastAccelerometerSet && lastMagnetometerSet) {
                SensorManager.getRotationMatrix(
                    rotationMatrix,
                    null,
                    lastAccelerometer,
                    lastMagnetometer
                )
                SensorManager.getOrientation(rotationMatrix, orientation)

                // The values in the 'orientation' array now contain the azimuth, pitch, and roll.
                // Use these values as needed for your compass implementation.

                // Example: Update your cursor marker based on the azimuth
                val azimuthInRadians = orientation[0]
                val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                currentMarker?.rotation = azimuthInDegrees
                //currentLocationMarker?.rotation = getAzimuth()
            }
        }
    }

    private fun applyLowPassFilter(input: FloatArray, output: FloatArray) {
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    private fun getAzimuth(): Float {
        return orientation[0] // Return the azimuth value
    }


}





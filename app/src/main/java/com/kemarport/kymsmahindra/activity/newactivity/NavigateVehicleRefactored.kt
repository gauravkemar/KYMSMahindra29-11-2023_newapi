package com.kemarport.kymsmahindra.activity.newactivity

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.databinding.ActivityNavigateTestingDemoBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Gps
import java.util.Timer
import java.util.TimerTask


class NavigateVehicleRefactored : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    lateinit var binding: ActivityNavigateTestingDemoBinding

    var modelCode: String = ""
    var colorCode: String = ""
    var vinNo: String = ""

    /* var t = Timer()
     var tt: TimerTask? = null*/


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var vehicleLocation: Location? = null
    private var currentLocation: Location? = null

    var currentMarker: Marker? = null
    var vehicleMarker: Marker? = null
    var vehicleOption: MarkerOptions? = null

    private var flagCurrentLoc = true
    private lateinit var googleMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001


    ///direction
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


    var toneGen1: ToneGenerator? = null

    val handler = Handler(Looper.getMainLooper())
    var showAlert = true

    private lateinit var locationProg: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_navigate_testing_demo)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_navigate_testing_demo)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding.searchVehicleToolbar.title = "Navigate"
        binding.searchVehicleToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.searchVehicleToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        locationProg=ProgressDialog(this)
        locationProg.setMessage("Please wait,\nGoogle Map Getting Ready...")
        locationProg.setCancelable(false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


//Intent data
    val lat = intent.getDoubleExtra(Constants.LATITUDE, 0.0)
       val log = intent.getDoubleExtra(Constants.LONGITUDE, 0.0)

/*


        val lat = 19.236293
        val log =72.987646
*/



        modelCode = intent.getStringExtra(Constants.ModelCode).toString()
        colorCode = intent.getStringExtra(Constants.ColorCode).toString()
        vinNo = intent.getStringExtra(Constants.VinNo).toString()


        /*       val log = 72.987958
               val lat = 19.236250



               modelCode = "abc"
               colorCode = "abc"
               vinNo = "abc"*/


        vehicleLocation = Location("Vehicle location")
        vehicleLocation?.latitude = lat
        vehicleLocation?.longitude = log

        //requestLocationPermission()
        requestLocationEnable()
        showProgressBarLocation()
        binding.btnRecenter.setOnClickListener {
            /* currentLocation?.let {
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
             }*/
            recentr()
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
        /*      tt = object : TimerTask() {
                  override fun run() {
                      getLocationNew()
                  }
              }

              t.scheduleAtFixedRate(tt, 1000, 4000)*/
        requestLocation()

    }
    fun showProgressBarLocation() {
        locationProg.show()
    }

    fun hideProgressBarLocation() {
        locationProg.cancel()
    }
    //lifecycle methods
    override fun onPause() {
        super.onPause()
        /*   if (t != null) {
               t.cancel()
               tt!!.cancel()
           }*/
        //direction
        sensorManager.unregisterListener(this)
        stopLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }
    override fun onResume() {
        super.onResume()
        requestLocationEnable()
        showProgressBarLocation()
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(1000)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        /*    if (t == null) {
                t = Timer()
                tt = object : TimerTask() {
                    override fun run() {
                        getLocationNew()
                    }
                }
                t.scheduleAtFixedRate(tt, 1000, 2000)
            }*/
        flagCurrentLoc = true

        //direction
        if (magnetometer != null || accelerometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        toneGen1?.let {
            it.stopTone()
            handler.removeCallbacksAndMessages(null)
            it.release()
        }
        toneGen1 = null
        stopLocationUpdates()
        /*  if (t != null) {
              t.cancel()
              tt!!.cancel()
          }*/
    }


    ////map
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        //googleMap.clear()

        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        //drawLines()
        runOnUiThread {
            recentr()
        }
    }

    /*    fun getLocationNew() {
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
                    *//*             newLat = gps.getLatitude()
                             newLng = gps.getLongitude()*//*
                currentLocation?.latitude = gps.getLatitude()
                currentLocation?.longitude = gps.getLongitude()
                currentLocation?.let {
                    Log.e("currentgetLocationNew","${gps.getLatitude()},${gps.getLongitude()}")
                    val bearing = calculateBearing(
                        LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                        LatLng(vehicleLocation!!.latitude, vehicleLocation!!.longitude)
                    )
                    // Rotate the camera based on the calculated bearing
                    googleMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(
                                    LatLng(
                                        currentLocation!!.latitude,
                                        currentLocation!!.longitude
                                    )
                                )
                                .bearing(bearing)
                                .tilt(15f) // Optional: Adjust the tilt as needed
                                .zoom(googleMap.cameraPosition.zoom) // Maintain the current zoom level
                                .build()
                        )
                    )

                    if (::googleMap.isInitialized) {
                        if(currentLocation!=null)
                        {
                            drawLines()
                        }
                    }
                    if (flagCurrentLoc) {
                        recentr()
                        flagCurrentLoc = false
                    }
                }


                if (gps != null) {
                  *//*  currentLocation = gps.location
                    Log.e("currentLoc", currentLocation.toString())
                    if (::googleMap.isInitialized) {
                        drawLines()
                    }*//*
                } else {
                    requestLocation()
                }
            }
        })
    }*/

    fun calculateBearing(start: LatLng, end: LatLng): Float {
        val startLat = Math.toRadians(start.latitude)
        val startLng = Math.toRadians(start.longitude)
        val endLat = Math.toRadians(end.latitude)
        val endLng = Math.toRadians(end.longitude)

        val deltaLng = endLng - startLng

        val y = Math.sin(deltaLng) * Math.cos(endLat)
        val x =
            Math.cos(startLat) * Math.sin(endLat) - Math.sin(startLat) * Math.cos(endLat) * Math.cos(
                deltaLng
            )

        var bearing = Math.toDegrees(Math.atan2(y, x)).toFloat()
        bearing = (bearing + 360) % 360 // Normalize to [0, 360)

        return bearing
    }


    /* private fun requestLocationPermission() {
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
             //getLocation()
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
                // getLocation()
             } else {
                 // Permission denied, handle accordingly
             }
         }
     }*/

    //location code (fused/gps/network)
    private fun requestLocationEnable() {
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

    /*   private fun getLocation() {
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
                       Log.e("currentLocationNewGPS",location.toString())
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
               *//*val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)*//*
        }
    }
*/
    private fun requestLocation() {
        /*     val locationRequest = LocationRequest.create()
             locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
             locationRequest.interval = 1000
          locationRequest.fastestInterval=1000
          locationRequest.maxWaitTime=2000*/
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(1000)
            .build()

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


        /*fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback
        )*/
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation

            if (location != null) {
                hideProgressBarLocation()
                Log.e("currentLocNewFusedGPS", location.toString())
                Toast.makeText(this@NavigateVehicleRefactored, "lat-${location.latitude} , Long-${location.longitude}", Toast.LENGTH_SHORT).show()
                updateLocation(location)
            }
        }
    }
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    private fun updateLocation(location: Location) {
        currentLocation = location

        val bearing = calculateBearing(
            LatLng(location.latitude, location.longitude),
            LatLng(vehicleLocation!!.latitude, vehicleLocation!!.longitude)
        )
        // Rotate the camera based on the calculated bearing
      /*  googleMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(
                        LatLng(
                            location.latitude,
                            location.longitude
                        )
                    )
                    .bearing(bearing)
                    .tilt(15f) // Optional: Adjust the tilt as needed
                    .zoom(googleMap.cameraPosition.zoom) // Maintain the current zoom level
                    .build()
            )
        )*/
        if (flagCurrentLoc) {
            recentr()
            flagCurrentLoc = false
        }
        if (::googleMap.isInitialized) {
            drawLines()
        }
    }

    fun recentr() {
        //if (currentMarker != null) currentMarker!!.remove()
        currentLocation?.let {
            val currentPos = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
            if (currentPos != null && googleMap != null) {
                googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
                googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(45f))
                println("(${currentLocation!!.latitude}, ${currentLocation!!.longitude}")
            }
        }

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
                    .width(15f)
                    .color(Color.RED)
                    .geodesic(true)
            )
            addCursorMarker(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
            if (modelCode != "null" || modelCode.isNotEmpty() && colorCode != "null" || colorCode.isNotEmpty()) {

            /*    val gifDrawable = generateCarLocationIcon(modelCode, colorCode)
                val bitmap = gifDrawable?.bitmap ?: return*/

                vehicleOption = MarkerOptions().position(
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

                vehicleMarker = googleMap?.addMarker(vehicleOption!!)
            } else {
 /*               val gifDrawable = generateCarLocationIcon(modelCode, colorCode)
                val bitmap = gifDrawable?.bitmap ?: return*/

                vehicleOption = MarkerOptions().position(
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

                 /*.icon(
                        BitmapDescriptorFactory.fromBitmap(
                            bitmap
                        )
                    )*/
                vehicleMarker = googleMap?.addMarker(vehicleOption!!)

            }

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
            } else if (distance < 2f) {
                audio(100, true)
                if (showAlert) showAlertOnce()
            } else {
                audio(0, false)
            }
        }
    }

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

    //adding cursor to location
    private fun addCursorMarker(position: LatLng) {
        val markerBitmap = generateLocationIcon()
        //currentMarker?.remove()
        Log.e(
            "getAzimuth", getAzimuth().toString()
        )
        if (getAzimuth() != 0.0f) {
            currentMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("Current Position")
                    .icon(markerBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
                    .flat(true) // Make the marker flat to rotate it
                    .rotation(getAzimuth())
            )
        } else {
            currentMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("Current Position")
                    .icon(markerBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
                //.flat(true) // Make the marker flat to rotate it
            )
        }
    }

    fun generateLocationIcon(): Bitmap? {
        val height = 50
        val width = 50
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.nav_icon)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
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
/*    fun generateCarLocationIcon(modelCode: String, colorDescription: String): BitmapDrawable? {
        val height = 50
        val width = 50
        val resourceId = getModelColorResourceId(modelCode, colorDescription)
        val inputStream = resources.openRawResource(resourceId)
        val movie = Movie.decodeStream(inputStream)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        movie.draw(canvas, 0f, 0f)
        return BitmapDrawable(resources, bitmap)
    }*/

/*    private fun getModelColorResourceId(modelCode: String, colorDescription: String): Int {
        val modelColorMap = mapOf(
            "XUV300" to mapOf(
                "Red" to R.raw.car_location,
                "Black" to R.raw.car_location,
                "White" to R.raw.car_location,
                "Blue" to R.raw.car_location
            ),
            "XUV500" to mapOf(
                "Red" to R.raw.car_location,
                "Black" to R.raw.car_location,
                "White" to R.raw.car_location,
                "Blue" to R.raw.car_location
            ),
            "XUV700" to mapOf(
                "Red" to R.raw.car_location,
                "Black" to R.raw.car_location,
                "White" to R.raw.car_location,
                "Blue" to R.raw.car_location
            )
        )
        return modelColorMap[modelCode]?.get(colorDescription) ?: R.raw.car_location
    }*/




    ///sensors
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}


/*    private fun drawLines() {
        googleMap.clear()
        currentLocation?.let {
            //direction
            val azimuthInRadians = orientation[0]
            val azimuthInDegrees = (Math.toDegrees(azimuthInRadians.toDouble()) + 360) % 360
            // Calculate rotation angle for the current marker
            currentMarkerRotation = -azimuthInDegrees.toFloat()
            if (currentMarker != null) currentMarker!!.remove()
            if (vehicleMarker != null) vehicleMarker!!.remove()

            *//*  if (currentMarker != null) {
                  currentMarker!!.rotation = currentMarkerRotation
              }*//*

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
            val bearing = calculateBearing(
                vehicleLocation!!.latitude,
                vehicleLocation!!.longitude, newLat,newLng )

            var normalizeBearing=normalizeBearing(bearing)

            val markerOptions =
                MarkerOptions().position(
                    LatLng(
                        currentLocation?.latitude!!,
                        currentLocation?.longitude!!
                    )
                ).title("You are here!")

                    .icon(BitmapDescriptorFactory.fromBitmap(generateLocationIcon()!!))
                    .flat(true)

            currentMarker = googleMap?.addMarker(markerOptions)
            currentMarker!!.setPosition(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
            currentMarker!!.setRotation(currentLocation!!.getBearing());

            Log.d("roate",bearing.toString())

            //currentMarker?.rotation(bearing)

            //currentMarker!!.rotation=bearing
           // currentMarker?.let { it1 -> rotateMarker(it1, bearing) }
            //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 16f))
            val vehicleOption =
                           MarkerOptions().position(
                               LatLng(
                                   vehicleLocation?.latitude!!,
                                   vehicleLocation?.longitude!!
                               )
                           ).title("You are here!")
                               .icon(BitmapDescriptorFactory.fromBitmap(generateCarLocationIcon()!!))



            vehicleMarker = googleMap?.addMarker(vehicleOption)



            //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 16f))

            val distance: Float = roundOff(
                currentLocation?.latitude!!,
                currentLocation?.longitude!!,
                vehicleLocation?.latitude!!,
                vehicleLocation?.longitude!!
            )?.toFloat()!!

            binding.tvTotalDistance.setText("$distance Meters Approx.")

        }
    }*/
/*  fun normalizeBearing(bearing: Float): Float {
      var normalizedBearing = bearing
      while (normalizedBearing < 0) {
          normalizedBearing += 360f
      }
      while (normalizedBearing >= 360) {
          normalizedBearing -= 360f
      }
      return normalizedBearing
  }

  fun generateCarLocationIcon(): Bitmap? {
      val height = 40
      val width = 40
      val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_car)
      return Bitmap.createScaledBitmap(bitmap, width, height, false)
  }
  fun generateLocationIcon(): Bitmap? {
      val height = 40
      val width = 40
      val bitmap = BitmapFactory.decodeResource(resources, R.drawable.navigation_arrow)
      return Bitmap.createScaledBitmap(bitmap, width, height, false)
  }
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

  fun recentr() {
      if (currentMarker != null) currentMarker!!.remove()
      val currentPos = LatLng(newLat, newLng)
      if (currentPos != null && googleMap != null) {
          googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
          googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(45f))
          println("la-$newLat, $newLng")
      }
  }

  private fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng): Double {
      val PI = 3.14159
      val lat1 = latLng1.latitude * PI / 180
      val long1 = latLng1.longitude * PI / 180
      val lat2 = latLng2.latitude * PI / 180
      val long2 = latLng2.longitude * PI / 180
      val dLon = long2 - long1
      val y = Math.sin(dLon) * Math.cos(lat2)
      val x = Math.cos(lat1) * Math.sin(lat2) - (Math.sin(lat1)
              * Math.cos(lat2) * Math.cos(dLon))
      var brng = Math.atan2(y, x)
      brng = Math.toDegrees(brng)
      brng = (brng + 360) % 360
      return brng
  }

  fun calculateBearing(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
      val sourceLatLng = LatLng(lat1, lng1)
      val destinationLatLng = LatLng(lat2, lng2)
      return SphericalUtil.computeHeading(sourceLatLng, destinationLatLng).toFloat()
  }
  private fun rotateMarker(marker: Marker, toRotation: Float) {
      if (!isMarkerRotating) {
          val handler = Handler()
          val start = SystemClock.uptimeMillis()
          val startRotation = marker.rotation
          val duration: Long = 2000

          val interpolator: Interpolator = LinearInterpolator()

          handler.post(object : Runnable {
              override fun run() {
                  isMarkerRotating = true

                  val elapsed = SystemClock.uptimeMillis() - start
                  val t = interpolator.getInterpolation(elapsed.toFloat() / duration)

                  val rot = t * toRotation + (1 - t) * startRotation

                  val bearing = if (-rot > 180) rot / 2 else rot

                  marker.rotation = bearing

                  if (t < 1.0) {
                      // Post again 16ms later.
                      handler.postDelayed(this, 16)
                  } else {
                      isMarkerRotating = false
                  }
              }
          })
      }
  }

  override fun onResume() {
      super.onResume()
      sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
      sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
  }

  override fun onPause() {
      super.onPause()
      sensorManager.unregisterListener(this, magnetometer)
      sensorManager.unregisterListener(this, accelerometer)
  }

  override fun onSensorChanged(event: SensorEvent) {
      when (event.sensor) {
          magnetometer -> {
              System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
              lastMagnetometerSet = true
          }
          accelerometer -> {
              System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
              lastAccelerometerSet = true
          }
      }

      if (lastAccelerometerSet && lastMagnetometerSet) {
          SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
          SensorManager.getOrientation(rotationMatrix, orientation)

          val azimuthInRadians = orientation[0]
          val azimuthInDegrees = (Math.toDegrees(azimuthInRadians.toDouble()) + 360) % 360

          val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.compass)
          val matrix = Matrix()
          matrix.postRotate((-azimuthInDegrees).toFloat())
          val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
          compassImage=BitmapDescriptorFactory.fromBitmap(rotatedBitmap)
          currentMarker!!.setIcon(compassImage)
      }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
      // Handle accuracy changes if needed
  }
}*/
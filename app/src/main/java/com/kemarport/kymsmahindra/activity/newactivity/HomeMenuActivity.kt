package com.kemarport.kymsmahindra.activity.newactivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.databinding.ActivityHomeMenuBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.SessionManager
import com.kemarport.kymsmahindra.helper.Utils
import java.util.ArrayList

class HomeMenuActivity : AppCompatActivity() {
    //////location
    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var isLocationAvailable = false
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 1000 // Update interval in milliseconds
        fastestInterval = 1000 // Fastest update interval in milliseconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private val LOCATION_PROVIDER_CHECK_INTERVAL = 1000
    private val locationProviderCheckHandler = Handler()
    private val locationProviderCheckRunnable = object : Runnable {
        override fun run() {
            checkLocationProviderStatus()
            locationProviderCheckHandler.postDelayed(
                this,
                LOCATION_PROVIDER_CHECK_INTERVAL.toLong()
            )
        }
    }
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private lateinit var locationManager: LocationManager
    var isGPSEnabled = false
    var isNetworkEnabled = false
    var coordinatePref = ""
    lateinit var coordinates: ArrayList<LatLng>
    private lateinit var session: SessionManager
    lateinit var binding:ActivityHomeMenuBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_home_menu)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        session = SessionManager(this)
        binding.homeToolbar.setTitle("KYMS")
        binding.homeToolbar.setTitleTextColor(resources.getColor(android.R.color.white))

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        coordinatePref = Utils.getSharedPrefs(this@HomeMenuActivity, Constants.USER_COORDINATES).toString()
        coordinates = parseStringToList(coordinatePref)
        checkLocationPermission()
        startLocationProviderCheck()

        binding.logout.setOnClickListener {
            showLogoutDialog()
        }

        binding.mcvParkVehicle.setOnClickListener {
            startActivity(Intent(this@HomeMenuActivity,ParkReparkActivity::class.java))
        }

        binding.mcvSearchVehicle.setOnClickListener {
            startActivity(Intent(this@HomeMenuActivity,SearchVehicleActivity::class.java))
        }

        binding.mcvPrdOutList.setOnClickListener {
            startActivity(Intent(this@HomeMenuActivity,ProductionOutListActivity::class.java))
        }

        binding.mcvMyTransaction.setOnClickListener {
            startActivity(Intent(this@HomeMenuActivity,MyTransactionActivity::class.java))
        }

        binding.mcvDispatchVehicle .setOnClickListener {
            startActivity(Intent(this@HomeMenuActivity,DispatchVehicleActivity::class.java))
        }

        binding.mcvAdminSettings .setOnClickListener {
            startActivity(Intent(this@HomeMenuActivity,AdminActivity::class.java))
        }
    }

    fun parseStringToList(inputString: String): ArrayList<LatLng> {
        val regex = Regex("\\((-?\\d+\\.\\d+),(-?\\d+\\.\\d+)\\)")
        val matches = regex.findAll(inputString)
        val latLngList = ArrayList<LatLng>()
        for (match in matches) {
            val (latitudeStr, longitudeStr) = match.destructured
            val latitude = latitudeStr.toDouble()
            val longitude = longitudeStr.toDouble()
            val latLng = LatLng(latitude, longitude)
            latLngList.add(latLng)
        }
        return latLngList
    }
    //////location
    private fun startLocationProviderCheck() {
        locationProviderCheckHandler.postDelayed(
            locationProviderCheckRunnable,
            LOCATION_PROVIDER_CHECK_INTERVAL.toLong()
        )
    }
    private fun checkLocationProviderStatus() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location provider is turned off, update UI accordingly
            /* currentLatitude = 0.0
             currentLongitude = 0.0*/
            /* runOnUiThread(Runnable {
                 binding.indicator.setImageResource(R.drawable.ic_circl_red)
             })*/
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is granted, start location updates
            startLocationUpdates()
            checkLocationProviderStatus()
        }
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            // Handle location updates here
            for (location in locationResult.locations) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                println("$currentLatitude-laffffff,   $currentLongitude - lofffffff")
                isLocationAvailable = true
                setMenuAndDashboard()
                updateUIWithLocation(currentLatitude, currentLongitude, isLocationAvailable)
            }
        }
    }
    private fun updateUIWithLocation(
        latitude: Double,
        longitude: Double,
        isLocationAvailable: Boolean
    ) {
        // Update your UI elements with the new latitude and longitude values
        // For example, display them on TextViews or use them in calculations.
        if (isLocationAvailable) {

            if (PolyUtil.containsLocation(LatLng(latitude, longitude), coordinates, false)) {
                runOnUiThread {
                    binding.indicator.setImageResource(R.drawable.ic_circl_green)
                }
            } else {
                runOnUiThread {
                    binding.indicator.setImageResource(R.drawable.ic_circl_red)
                }
            }
        } else {
            runOnUiThread {
                binding.indicator.setImageResource(R.drawable.ic_circl_red)
            }
        }
    }
    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates()
            } else {
                // Permission denied, handle this case
            }
        }
    }
    fun containsLocation(point: LatLng?, polygon: List<LatLng?>?, geodesic: Boolean): Boolean {
        return PolyUtil.containsLocation(point, polygon, geodesic)
    }
    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()

        stopLocationProviderCheck()
    }

    private fun stopLocationProviderCheck() {
        locationProviderCheckHandler.removeCallbacks(locationProviderCheckRunnable)
    }

    private fun setMenuAndDashboard() {
        if (isLocationEnabled()) {
            checkLocationPermission()
        } else {
            openLocationSettings()
        }

        if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
            if (session.getRole().equals("supervisor"))
            {

            } else if (session.getRole().equals("Super Admin"))
            {
                binding.mcvPrdOutList.visibility=View.VISIBLE
                binding.mcvParkVehicle.visibility=View.VISIBLE
                binding.mcvSearchVehicle.visibility=View.VISIBLE
                binding.mcvMyTransaction.visibility=View.VISIBLE
                binding.mcvDispatchVehicle.visibility=View.VISIBLE
                binding.mcvAdminSettings.visibility=View.VISIBLE

            } else if (session.getRole().equals("Driver"))
            {
                binding.mcvPrdOutList.visibility=View.VISIBLE
                binding.mcvParkVehicle.visibility=View.VISIBLE
                binding.mcvSearchVehicle.visibility=View.VISIBLE
                binding.mcvMyTransaction.visibility=View.VISIBLE
                binding.mcvDispatchVehicle.visibility=View.GONE
                binding.mcvAdminSettings.visibility=View.GONE

            }
        }
        else{
            if (session.getRole().equals("supervisor"))
            {

            } else if (session.getRole().equals("Super Admin"))
            {
                binding.mcvPrdOutList.visibility=View.GONE
                binding.mcvParkVehicle.visibility=View.GONE
                binding.mcvSearchVehicle.visibility=View.GONE
                binding.mcvMyTransaction.visibility=View.GONE
                binding.mcvDispatchVehicle.visibility=View.GONE
                binding.mcvAdminSettings.visibility=View.VISIBLE
                binding.tvNoInsideServiceArea.visibility=View.GONE

            } else if (session.getRole().equals("Driver"))
            {
                binding.mcvPrdOutList.visibility=View.GONE
                binding.mcvParkVehicle.visibility=View.GONE
                binding.mcvSearchVehicle.visibility=View.GONE
                binding.mcvMyTransaction.visibility=View.GONE
                binding.mcvDispatchVehicle.visibility=View.GONE
                binding.mcvAdminSettings.visibility=View.GONE
                binding.tvNoInsideServiceArea.visibility=View.VISIBLE
            }
        }

    }

    private fun logout(){
        session.logoutUser()
        startActivity(Intent(this@HomeMenuActivity, LoginActivity::class.java))
        finish()
    }
    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, which ->
                logout()
            }
            .setNegativeButton("Cancel") { dialog, which ->

                dialog.dismiss()
            }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }
}
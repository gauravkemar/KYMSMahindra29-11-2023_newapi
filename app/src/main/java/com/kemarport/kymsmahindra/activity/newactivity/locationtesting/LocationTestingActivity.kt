package com.kemarport.kymsmahindra.activity.newactivity.locationtesting

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest

import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.databinding.ActivityLocationTestingBinding
import java.util.Timer
import java.util.TimerTask


class LocationTestingActivity : AppCompatActivity() {
    lateinit var binding:ActivityLocationTestingBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
 /*   private lateinit var locationManager: LocationManager
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Handle location updates here
            showToast("Latitude: ${location.latitude}, Longitude: ${location.longitude}")
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Handle provider status changes
        }

        override fun onProviderEnabled(provider: String) {
            // Handle provider enabled
            showToast("$provider enabled")
        }

        override fun onProviderDisabled(provider: String) {
            // Handle provider disabled
            showToast("$provider disabled")
        }
    }*/

    var t = Timer()
    var tt: TimerTask? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_location_testing)
     fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
          tt = object : TimerTask() {
                   override fun run() {
                       getLocationNew()
                   }
               }
               t.scheduleAtFixedRate(tt, 1000, 1000)

 }
    private fun getLocationNew(){
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
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {
                    val lat = location.latitude
                    val lon = location.longitude
                    binding.tvLocation.setText("location  ($lat  ,  $lon).")
                    Toast.makeText(this, "  location($lat, $lon).", Toast.LENGTH_SHORT).show()
                    Log.d("currentLoc","location  ($lat  ,  $lon).")


                }

            }

    }
    override fun onPause() {
        super.onPause()
         if (t != null) {
             t.cancel()
             tt!!.cancel()
         }

    }

    override fun onResume() {
        super.onResume()
        if (t == null) {
            t = Timer()
            tt = object : TimerTask() {
                override fun run() {
                    getLocationNew()
                }
            }
            t.scheduleAtFixedRate(tt, 1000, 1000)
        }
    }

      /*  locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        // Request location updates
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            MIN_TIME_BETWEEN_UPDATES,
            MIN_DISTANCE_CHANGE_FOR_UPDATES,
            locationListener
        )*/
    }

   /* override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, request location updates
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
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListener
                )
            } else {
                showToast("Location permission denied")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val MIN_TIME_BETWEEN_UPDATES = 1000L // Minimum time interval between location updates (milliseconds)
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 0F // Minimum distance between location updates (meters)
    }*/

/*
class LocationTestingActivity : AppCompatActivity() {
    var t = Timer()
    var tt: TimerTask? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLocation: Location? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_testing)

       fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestLocation()

    }
 */
/*   override fun onPause() {
        super.onPause()
        binding.mapview.onPause()
        if (isRFIDInit) {
            rfidHandler!!.onPause()
        }
        if (isBarcodeInit) {
            deInitScanner()
        }
        *//*
*/
/* if (t != null) {
             t.cancel()
             tt!!.cancel()
         }*//*
*/
/*
        resumeFlag = true
    }*//*


       private fun requestLocation() {
           val locationRequest = LocationRequest.create()
           locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
           locationRequest.interval = 0
           locationRequest.fastestInterval=0

           val locationRequest=LocationRequest
 */
/*          mLocationRequest = LocationRequest()
           mLocationRequest.setInterval(0)*//*


          // mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

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
               object : LocationCallback() {
                   override fun onLocationResult(locationResult: LocationResult) {
                       val location = locationResult.lastLocation
                       // Handle the location update here
                       if (location != null) {
                           //Log.e("fromfused",location.toString())
                           Log.d("currentLocNewFusedGPS",location.toString())

                       }
                   }
               },
               null
           )
       }
}*/

package com.kemarport.kymsmahindra.activity.newactivity

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.activity.ScanBarcodeActivity
import com.kemarport.kymsmahindra.databinding.ActivityDispatchVehicleBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Gps
import com.kemarport.kymsmahindra.helper.RFIDHandlerForDispatch
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.SessionManager
import com.kemarport.kymsmahindra.helper.Utils
import com.kemarport.kymsmahindra.model.newapi.parkrepark.GetVehicleStatusRequest
import com.kemarport.kymsmahindra.model.newapi.parkrepark.PostVehicleMovementRequest
import com.kemarport.kymsmahindra.repository.KYMSRepository
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.parkrepark.ParkReparkViewModel
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.parkrepark.ParkReparkViewModelFactory
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.BarcodeManager
import com.symbol.emdk.barcode.ScanDataCollection
import com.symbol.emdk.barcode.Scanner
import com.symbol.emdk.barcode.ScannerException
import com.symbol.emdk.barcode.ScannerResults
import com.symbol.emdk.barcode.StatusData
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import java.util.ArrayList
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask

class DispatchVehicleActivity : AppCompatActivity(), View.OnClickListener,
    RFIDHandlerForDispatch.ResponseHandlerInterface,
    EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener, OnMapReadyCallback {
    lateinit var binding: ActivityDispatchVehicleBinding
    var emdkManager: EMDKManager? = null
    var barcodeManager: BarcodeManager? = null
    var scanner: Scanner? = null

    var resumeFlag = false
    var TAG = "ParkInActivity"

    var rfidHandler: RFIDHandlerForDispatch? = null

    var newLat = 0.0
    var newLng = 0.0

/*    var t = Timer()
    var tt: TimerTask? = null*/
    private lateinit var viewModel: ParkReparkViewModel

    private lateinit var session: SessionManager
    private lateinit var userDetails: HashMap<String, String?>
    private var token: String? = ""
    private var userName: String? = ""
    private var locationId: String? = ""
    private lateinit var progress: ProgressDialog

    val coordinatesMap = HashMap<String, ArrayList<LatLng>>()
    val locationMap = HashMap<String, ArrayList<LatLng>>()
    val idMap = HashMap<String, String>()

    var isRFIDInit = false
    var isBarcodeInit = false

    var map: GoogleMap? = null
    var currentMarker: Marker? = null

    private fun initReader() {
        rfidHandler = RFIDHandlerForDispatch()
        rfidHandler!!.init(this)
    }

    override fun onPause() {
        super.onPause()
        binding.mapview.onPause()
        if (isRFIDInit) {
            rfidHandler!!.onPause()
        }
        if (isBarcodeInit) {
            deInitScanner()
        }
      /*  if (t != null) {
            t.cancel()
            tt!!.cancel()
        }*/
        resumeFlag = true
        stopLocationUpdates()
    }

    override fun onPostResume() {
        super.onPostResume()
        binding.mapview.onResume()
        if (isRFIDInit) {
            val status = rfidHandler!!.onResume()
            Toast.makeText(this@DispatchVehicleActivity, status, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapview.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapview.onDestroy()
        binding.unbind()
        if (isRFIDInit) {
            rfidHandler!!.onDestroy()
        }
        if (isBarcodeInit) {
            deInitScanner()
        }
        stopLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }
    fun performInventory() {
        rfidHandler!!.performInventory()
    }

    fun stopInventory() {
        rfidHandler!!.stopInventory()
    }
    private fun defaultVinApiCall(){
        val intent = intent
        val vin = intent.getStringExtra("vin")
        binding.tvBarcode.setText(vin)
        vin?.let { getPrdOutStatus(it) }
    }
    fun getPrdOutStatus(scanned: String) {

        viewModel.getVehicleStatus(
            token!!,
            Constants.BASE_URL,
            GetVehicleStatusRequest( scanned,"")
        )

    }
    var coordinatePref = ""

    lateinit var coordinates: ArrayList<LatLng>
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private var flagCurrentLoc=true
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_dispatch_vehicle)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding.listener = this
        binding.parkInVehicleToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        binding.parkInVehicleToolbar.setTitle("Dispatch Vehicle")
        setSupportActionBar(binding.parkInVehicleToolbar)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val color = Color.parseColor("#004f8c") // Replace with your desired color
        val grey = Color.parseColor("#80D9D9D9") // Replace with your desired color
        binding.mapview.onCreate(savedInstanceState)
        binding.mapview.getMapAsync(this)
        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory =
            ParkReparkViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[ParkReparkViewModel::class.java]

        progress = ProgressDialog(this)
        progress.setMessage("Loading...")

        session = SessionManager(this)
        coordinatePref =
            Utils.getSharedPrefs(this@DispatchVehicleActivity, Constants.USER_COORDINATES).toString()
        coordinates = parseStringToList(coordinatePref)
        if (session.getRole().equals("Driver")) {
            binding.parkInVehicleToolbar.title = "Park/Repark"
        } else if (session.getRole().equals("supervisor") || session.getRole()
                .equals("superadmin")
        ) {
            binding.parkInVehicleToolbar.title = "Dispatch"
        }

        requestLocationEnable()
        requestLocation()
        userDetails = session.getUserDetails()
        token = userDetails["jwtToken"]
        userName = userDetails["userName"]
        locationId = userDetails["locationId"]

        defaultVinApiCall()
        // binding.radioGroup.check(binding.radioBtn2.getId())

        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            binding.scanBarcode.visibility = View.GONE
            //binding.radioGroup.visibility = View.VISIBLE
            defaultRFID()
            //initBarcode()
        } else {
            binding.scanBarcode.visibility = View.VISIBLE
            // binding.radioGroup.visibility = View.GONE
        }

        /*binding.radioGroup.setOnCheckedChangeListener { buttonView, selected ->
            if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
                if (selected == binding.radioBtn1.getId()) {
                    isRFIDInit = true
                    isBarcodeInit = false
                    deInitScanner()
                    Thread.sleep(1000)
                    initReader()
                } else if (selected == binding.radioBtn2.getId()) {
                    isRFIDInit = false
                    isBarcodeInit = true
                    rfidHandler!!.onPause()
                    rfidHandler!!.onDestroy()
                    Thread.sleep(1000)
                    val results = EMDKManager.getEMDKManager(this@ParkInActivity, this)
                    if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                        Log.e(TAG, "EMDKManager object request failed!")
                    } else {
                        Log.e(
                            TAG,
                            "EMDKManager object initialization is   in   progress......."
                        )
                    }
                }
            }
        }*/
        viewModel.getAllInternalYardLocations(
            session.getToken(),
            Constants.BASE_URL
        )

        viewModel.getAllInternalYardMutable.observe(this)
        { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        //if (resultResponse.size > 0) {
                        //for (e in resultResponse) {
                        if (resultResponse != null) {

                            for (e in resultResponse)
                            {
                                Log.d("thiscoordinates",e.coordinates.toString())
                                if(e.locationType.equals("Internal"))
                                {

                                    if(e.coordinates!=null)
                                    {

                                        var coordinates: ArrayList<LatLng> = parseStringToList(e.coordinates!!)
                                        if (!coordinatesMap.containsKey(e.locationId.toString())) {
                                            coordinatesMap[e.locationId.toString()] = coordinates
                                            //  idMap[e.locationCode] = "${resultResponse.locationId}"
                                        }
                                        if(!locationMap.containsKey(e.locationName))
                                        {
                                            locationMap[e.locationName] = coordinates
                                        }

                                        val polygonOptions =
                                            PolygonOptions().addAll(coordinates).clickable(false)
                                                .strokeColor(
                                                    ContextCompat.getColor(
                                                        this,
                                                        R.color.colorPrimaryLight
                                                    )
                                                )
                                                .strokeWidth(3f)
                                        map!!.addPolygon(polygonOptions)
                                    }

                                }
                            }
                        }
                        // }
                    }
                    // }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        session.showToastAndHandleErrors(resultResponse,this@DispatchVehicleActivity)
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }

                else -> {}
            }
        }

        viewModel.parkReparkVehicleMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        if(resultResponse.message!=null)
                        {
                            Toasty.success(
                                this@DispatchVehicleActivity,
                                resultResponse.message.toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }

                        clear()
                        if (resultResponse.responseMessage != null) {
                            Toasty.success(
                                this@DispatchVehicleActivity,
                                resultResponse.responseMessage.toString(),
                                Toasty.LENGTH_SHORT
                            ).show()

                        } else if (resultResponse.errorMessage != null) {
                            Toasty.warning(
                                this@DispatchVehicleActivity,
                                resultResponse.errorMessage.toString(),
                                Toasty.LENGTH_SHORT
                            ).show()

                        }

                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    clear()
                    ViewCompat.setBackgroundTintList(
                        binding.parkReparkBtn,
                        ColorStateList.valueOf(grey)
                    )
                    binding.parkReparkBtn.isEnabled = false
                    binding.parkReparkBtn.setText("Park Vehicle")

                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        session.showToastAndHandleErrors(resultResponse,this@DispatchVehicleActivity)
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }

                else -> {}
            }
        }

        viewModel.getVehicleStatusMutableLiveData.observe(this) { response ->
            when (response) {
                /*is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        binding.clVehicleImgs.visibility = View.VISIBLE

                        if(resultResponse.vin!=null)
                        {
                            binding.tvVinValue.setText(resultResponse.vin)
                        }
                        else
                        {
                            binding.tvVinValue.setText("")
                        }
                        if(resultResponse.modelCode!=null)
                        {
                            binding.tvModelCodeValue.setText(resultResponse.modelCode.toString())
                        }
                        else
                        {
                            binding.tvModelCodeValue.setText("resultResponse.modelCode.toString()")
                        }


                        if(resultResponse.status!=null)
                        {
                            binding.tvStatusValue.setText(resultResponse.status)
                        }
                        else
                        {
                            binding.tvStatusValue.setText("")
                        }


                        if (resultResponse.status.equals("Production Out")) {
                            ViewCompat.setBackgroundTintList(
                                binding.parkReparkBtn,
                                ColorStateList.valueOf(grey)
                            )
                            binding.parkReparkBtn.setText("Dispatch Vehicle")
                            binding.parkReparkBtn.isEnabled = false
                        } else if (resultResponse.status.equals("Parking")) {
                            binding.parkReparkBtn.setText("Dispatch Vehicle")
                            ViewCompat.setBackgroundTintList(
                                binding.parkReparkBtn,
                                ColorStateList.valueOf(color)
                            )
                            binding.parkReparkBtn.isEnabled = true
                            binding.parkReparkBtn.setOnClickListener {
                                checkVehicleInsideGeofenceRFIDNew(resultResponse.vin, "Dispatched")
                            }
                        } else if (resultResponse.status.equals("Re Park")) {
                            binding.parkReparkBtn.setText("Dispatch Vehicle")

                            ViewCompat.setBackgroundTintList(
                                binding.parkReparkBtn,
                                ColorStateList.valueOf(color)
                            )
                            binding.parkReparkBtn.isEnabled = true
                            binding.parkReparkBtn.setOnClickListener {
                                checkVehicleInsideGeofenceRFIDNew(resultResponse.vin, "Dispatched")
                            }
                        } else if (resultResponse.status.equals("Delivered")) {
                            ViewCompat.setBackgroundTintList(
                                binding.parkReparkBtn,
                                ColorStateList.valueOf(grey)
                            )
                            binding.parkReparkBtn.setText("Dispatch Vehicle")
                            binding.parkReparkBtn.isEnabled = false
                        }

                    }
                }*/
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        binding.clVehicleImgs.visibility = View.VISIBLE

                        binding.tvVinValue.text = resultResponse.vin.orEmpty()
                        binding.tvModelCodeValue.text = resultResponse.modelCode?.toString().orEmpty()
                        binding.tvStatusValue.text = resultResponse.status.orEmpty()

                        val buttonColor: Int
                        val buttonText: String
                        val isButtonEnabled: Boolean

                        when (resultResponse.status) {
                            "Production Out" -> {
                                buttonColor = color
                                buttonText = "Dispatch Vehicle"
                                isButtonEnabled = true
                            }
                            "Parking", "Re Park" -> {
                                buttonColor = color
                                buttonText = "Dispatch Vehicle"
                                isButtonEnabled = true
                            }
                            "Delivered" -> {
                                buttonColor = grey
                                buttonText = "Dispatch Vehicle"
                                isButtonEnabled = false
                            }
                            else -> {
                                buttonColor = grey
                                buttonText = "Dispatch Vehicle"
                                isButtonEnabled = false
                            }
                        }

                        with(binding.parkReparkBtn) {
                            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(buttonColor))
                            setText(buttonText)
                            isEnabled = isButtonEnabled

                            if (isButtonEnabled) {
                                setOnClickListener {
                                    resultResponse.vin?.let { it1 ->
                                        checkVehicleInsideGeofenceRFIDNew(
                                            it1, "Dispatched")
                                    }
                                }
                            }
                        }
                        var vehicleInfoMessage=resultResponse.message

                        if(!vehicleInfoMessage.isNullOrEmpty())
                        {
                            Toasty.warning(
                                this@DispatchVehicleActivity,
                                vehicleInfoMessage,
                                Toasty.LENGTH_SHORT
                            ).show()

                        }


                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    clear()
                    ViewCompat.setBackgroundTintList(
                        binding.parkReparkBtn,
                        ColorStateList.valueOf(color)
                    )
                    binding.parkReparkBtn.isEnabled = true
                    binding.parkReparkBtn.setText("Park/Repark Vehicle")

                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        session.showToastAndHandleErrors(resultResponse,this@DispatchVehicleActivity)

                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }

                else -> {}
            }
        }
 /*       tt = object : TimerTask() {
            override fun run() {
                getLocationNew()
            }
        }

        t.scheduleAtFixedRate(tt, 1000, 1000)*/
    }
    private fun requestLocationEnable() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            session.showAlertMessage(this@DispatchVehicleActivity)
        }
    }
    private fun requestLocation() {
    /*    val locationRequest = LocationRequest.create()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000 //4 seconds*/
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
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
   /*     fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    // Handle the location update here
                    if (location != null) {
                        //Log.e("fromfused",location.toString())
                        Log.e("currentLocNewFusedGPS",location.toString())
                        updateLocation(location)
                    }
                }
            },
            null
        )*/
    }



private val locationCallback = object : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
        val location = locationResult.lastLocation
        if (location != null) {
            Log.e("currentLocNewFusedGPS", location.toString())
            //Toast.makeText(this@DispatchVehicleActivity, "lat-${location.latitude} , Long-${location.longitude}", Toast.LENGTH_SHORT).show()
            updateLocation(location)
        }
    }
}

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    private fun updateLocation(location: Location) {

        runOnUiThread(Runnable {

            newLat = location.latitude
            newLng = location.longitude
            currentLatitude = location.latitude
            currentLongitude = location.longitude
            if (currentMarker != null) currentMarker!!.remove()
            updateTvCurrentLoc()
            val markerOptions =
                MarkerOptions().position(LatLng(newLat, newLng)).title("You are here!")
                    .icon(BitmapDescriptorFactory.fromBitmap(generateLocationIcon()!!))
            currentMarker = map!!.addMarker(markerOptions)

            Log.e(TAG, "Latitude/Longitude - $newLat,$newLng")
            if(flagCurrentLoc)
            {
                recentr()
                flagCurrentLoc=false
            }



        })

    }
    fun getVehicleStatus(scanned: String) {
        if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
            viewModel.getVehicleStatus(
                token!!,
                Constants.BASE_URL,
                GetVehicleStatusRequest("",scanned,)
            )
        } else {
            Toasty.warning(
                this@DispatchVehicleActivity,
                "You are not inside Service Area!!",
                Toasty.LENGTH_SHORT
            ).show()
        }

    }
    fun getVehicleStatusBarcode(scanned: String) {
        if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
            viewModel.getVehicleStatus(
                token!!,
                Constants.BASE_URL,
                GetVehicleStatusRequest(scanned,"")
            )
        } else {
            Toasty.warning(
                this@DispatchVehicleActivity,
                "You are not inside Service Area!!",
                Toasty.LENGTH_SHORT
            ).show()
        }

    }

    private fun initBarcode(){
        isRFIDInit = false
        isBarcodeInit = true
        //rfidHandler!!.onPause()
        //rfidHandler!!.onDestroy()
        Thread.sleep(1000)
        val results = EMDKManager.getEMDKManager(this@DispatchVehicleActivity, this)
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Log.e(TAG, "EMDKManager object request failed!")
        } else {
            Log.e(
                TAG,
                "EMDKManager object initialization is   in   progress......."
            )
        }
    }
    fun checkVehicleInsideGeofenceRFIDNew(scanned: String, s: String) {
        try {
            var insideGeofence = false
            var geofenceId = -1

            // Check if the user is inside any static geofence
            if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
                // Check each dynamic geofence
                for ((key, value) in coordinatesMap.entries) {
                    if (containsLocation(LatLng(newLat, newLng), value, false)) {
                        insideGeofence = true
                        geofenceId = key.toInt()
                        break
                    }
                }
            }
            else
            {
                Toasty.warning(
                    this@DispatchVehicleActivity,
                    "You are not inside Service Area!!",
                    Toasty.LENGTH_SHORT
                ).show()
            }

            // Process based on whether the user is inside any geofence or not
            if (insideGeofence) {
                // User is inside a geofence
                viewModel.parkReparkVehicle(
                    token!!,
                    Constants.BASE_URL,
                    PostVehicleMovementRequest(userName!!, "$newLat,$newLng", s, scanned, geofenceId)
                )
            } else {
                // User is not inside any geofence
                viewModel.parkReparkVehicle(
                    token!!,
                    Constants.BASE_URL,
                    PostVehicleMovementRequest(userName!!, "$newLat,$newLng", s, scanned, locationId!!.toInt())
                )
        /*        Toasty.warning(
                    this@DispatchVehicleActivity,
                    "You are not inside any parking!!",
                    Toasty.LENGTH_SHORT
                ).show()*/
            }
        }
        catch (e:Exception)
        {
            Toasty.warning(
                this@DispatchVehicleActivity,
                e.message.toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }

    }

    fun checkVehicleInsideGeofenceRFID(scanned: String, s: String) {
        if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
            for ((key, value) in coordinatesMap.entries) {
                if (containsLocation(LatLng(newLat, newLng), value, false)) {
                    viewModel.parkReparkVehicle(
                        token!!,
                        Constants.BASE_URL,
                        PostVehicleMovementRequest(userName!!, "$newLat,$newLng", s, scanned,0)
                    )
                } else {
                    runOnUiThread(Runnable {
                        Toasty.warning(
                            this@DispatchVehicleActivity,
                            "You are not inside parking!!",
                            Toasty.LENGTH_SHORT
                        ).show()
                    })
                }
            }
        } else {
            Toasty.warning(
                this@DispatchVehicleActivity,
                "You are not inside parking!!",
                Toasty.LENGTH_SHORT
            ).show()
        }

    }
    fun containsLocation(point: LatLng, polygon: List<LatLng>, geodesic: Boolean): Boolean {
        return PolyUtil.containsLocation(point, polygon, geodesic)
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
                currentLatitude = gps.getLatitude()
                currentLongitude = gps.getLongitude()
                if (currentMarker != null) currentMarker!!.remove()
                updateTvCurrentLoc( )
                val markerOptions =
                    MarkerOptions().position(LatLng(newLat, newLng)).title("You are here!")
                        .icon(BitmapDescriptorFactory.fromBitmap(generateLocationIcon()!!))
                currentMarker = map!!.addMarker(markerOptions)
                Log.e(TAG, "Latitude/Longitude - $newLat,$newLng")
                if(flagCurrentLoc)
                {
                    recentr()
                    flagCurrentLoc=false
                }
                val currentPos = LatLng(newLat, newLng)
                if(currentPos!=null && map!=null)
                {

                    map!!.setOnCameraMoveListener {
                        val cameraPosition = map!!.cameraPosition
                        rotateCompassImageView(cameraPosition.bearing)
                    }
                }
            }
        })
    }
 /*   fun updateTvCurrentLoc( ) {
        if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
            for ((key, value) in locationMap.entries) {
                if (containsLocation(LatLng(newLat, newLng), value, false)) {
                    binding.tvCurrentAt.setText("You are at $key !!")
                } else {
                    binding.tvCurrentAt.setText("You are at Unkown Location !!")
                }
            }
        }
        *//*else {
        }*//*
    }*/
    fun updateTvCurrentLoc() {
        if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
            for ((key, value) in locationMap.entries) {
                if (containsLocation(LatLng(newLat, newLng), value, false)) {
                    binding.tvCurrentAt.text = "You are at $key !!"
                    return  // Exit the loop once a match is found
                }
            }
            // If no match is found in the loop
            binding.tvCurrentAt.text = "You are at Unknown Location !!"
        }
        // Handle the case when current location is not in the specified coordinates
        // Uncomment and add code as needed
        /*
        else {
            // Handle the case when current location is not in the specified coordinates
        }
        */
    }

    private fun defaultRFID() {
        isRFIDInit = true
        isBarcodeInit = false
        deInitScanner()
        Thread.sleep(1000)
        initReader()
    }

    fun generateLocationIcon(): Bitmap? {
        val height = 35
        val width = 35
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_pin)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    override fun handleTagdata(tagData: Array<TagData>) {
        val sb = StringBuilder()
        sb.append(tagData[0].tagID)
        runOnUiThread {
            var tagDataFromScan = tagData[0].tagID
            binding.tvBarcode.setText(tagDataFromScan)
            Log.e(TAG, "RFID Data : $tagDataFromScan")

            stopInventory()
        }
        //checkVehicleInsideGeofenceRFID(tagData[0].tagID.toString())
        getVehicleStatus(tagData[0].tagID.toString())
    }

    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            performInventory()
        } else stopInventory()

    }

    override fun onOpened(emdkManager: EMDKManager?) {
        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            this.emdkManager = emdkManager
            initBarcodeManager()
            initScanner()
        }
    }

    override fun onClosed() {
        if (emdkManager != null) {
            emdkManager!!.release()
            emdkManager = null
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapview.onResume()
        if (resumeFlag) {
            resumeFlag = false
            if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
                initBarcodeManager()
                initScanner()
            }
        }
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
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
   /*     if (t == null) {
            t = Timer()
            tt = object : TimerTask() {
                override fun run() {
                    getLocationNew()
                }
            }
            t.scheduleAtFixedRate(tt, 1000, 1000)
        }*/
        flagCurrentLoc=true
    }

    fun initBarcodeManager() {
        barcodeManager =
            emdkManager!!.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
        if (barcodeManager == null) {
            Toast.makeText(
                this@DispatchVehicleActivity,
                "Barcode scanning is not supported.",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    fun initScanner() {
        if (scanner == null) {
            barcodeManager =
                emdkManager?.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
            scanner = barcodeManager!!.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
            scanner?.addDataListener(this)
            scanner?.addStatusListener(this)
            scanner?.triggerType = Scanner.TriggerType.HARD
            try {
                scanner?.enable()
            } catch (e: ScannerException) {
                e.printStackTrace()
            }
        }
    }

    fun deInitScanner() {
        if (scanner != null) {
            try {
                scanner!!.release()
            } catch (e: Exception) {
            }
            scanner = null
        }
    }


    override fun onData(scanDataCollection: ScanDataCollection?) {
        var dataStr: String? = ""
        if (scanDataCollection != null && scanDataCollection.result == ScannerResults.SUCCESS) {
            val scanData = scanDataCollection.scanData
            for (data in scanData) {
                val barcodeData = data.data
                val labelType = data.labelType
                dataStr = barcodeData
            }
            runOnUiThread { binding.tvBarcode.setText(dataStr) }
            // checkVehicleInsideGeofenceBarcode(dataStr.toString())

            dataStr?.let { getVehicleStatusBarcode(it) }

            Log.e(TAG, "Barcode Data : $dataStr")
        }
    }

    override fun onStatus(statusData: StatusData) {
        val state = statusData.state
        var statusStr = ""
        when (state) {
            StatusData.ScannerStates.IDLE -> {
                statusStr = statusData.friendlyName + " is   enabled and idle..."
                setConfig()
                try {
                    scanner!!.read()
                } catch (e: ScannerException) {
                }
            }

            StatusData.ScannerStates.WAITING -> statusStr =
                "Scanner is waiting for trigger press..."

            StatusData.ScannerStates.SCANNING -> statusStr = "Scanning..."
            StatusData.ScannerStates.DISABLED -> {}
            StatusData.ScannerStates.ERROR -> statusStr = "An error has occurred."
            else -> {}
        }
        setStatusText(statusStr)
    }

    private fun setConfig() {
        if (scanner != null) {
            try {
                val config = scanner!!.config
                if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                    config.scanParams.decodeHapticFeedback = true
                }
                scanner!!.config = config
            } catch (e: ScannerException) {
                Log.e(TAG, e.message!!)
            }
        }
    }

    fun setStatusText(msg: String) {
        Log.e(TAG, "StatusText: $msg")
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.scanBarcode -> {
                val intent = Intent(this, ScanBarcodeActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE)
            }

            R.id.btnClear -> {
                clear()
            }

            R.id.btnRecenter -> {
                recentr()

            }
        }
    }

    fun recentr(){
        if (currentMarker != null) currentMarker!!.remove()
        val currentPos = LatLng(newLat, newLng)
        if(currentPos!=null && map!=null)
        {
            map!!.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
            map!!.animateCamera(CameraUpdateFactory.zoomTo(25f))
            println("la-$newLat, $newLng")


        }
    }

    private fun rotateCompassImageView(bearing: Float) {
        if (::binding.isInitialized) {
            val currentRotation = binding.imCompassDirection.rotation
            val toRotation = -bearing

            val rotateAnimation = RotateAnimation(
                currentRotation, toRotation,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )

            rotateAnimation.duration = 100 // Adjust the duration as needed
            rotateAnimation.fillAfter = true

            binding.imCompassDirection.startAnimation(rotateAnimation)
        } else {
            Log.e("MainActivity", "Binding is not initialized")
        }
    }

    private fun clear(){
        val grey = Color.parseColor("#80D9D9D9") // Replace with your desired color
        binding.tvBarcode.setText("")
        binding.tvVinValue.setText("")
        binding.tvModelCodeValue.setText("")
        binding.tvStatusValue.setText("")
        ViewCompat.setBackgroundTintList(
            binding.parkReparkBtn,
            ColorStateList.valueOf(grey)
        )
        binding.parkReparkBtn.isEnabled = false
        binding.parkReparkBtn.setText("Dispatch Vehicle")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val resultValue = data?.getStringExtra(ScanBarcodeActivity.EXTRA_RESULT)
                Log.e(TAG, resultValue ?: "Empty")
            } else {
                Log.e(TAG, "Result cancelled")
            }
        }
    }

    fun showProgressBar() {
        progress.show()
    }

    fun hideProgressBar() {
        progress.cancel()
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

    companion object {
        const val REQUEST_CODE = 7777
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.uiSettings.isMyLocationButtonEnabled = false
        map!!.mapType = GoogleMap.MAP_TYPE_NORMAL


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}
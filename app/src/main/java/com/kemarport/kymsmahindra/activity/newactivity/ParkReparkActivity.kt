package com.kemarport.kymsmahindra.activity.newactivity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
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
import com.kemarport.kymsmahindra.databinding.ActivityParkReparkBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Gps
import com.kemarport.kymsmahindra.helper.RFIDHandlerForParkRepark
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.SessionManager
import com.kemarport.kymsmahindra.helper.Utils
import com.kemarport.kymsmahindra.model.newapi.parkrepark.GetVehicleStatusRequest
import com.kemarport.kymsmahindra.model.newapi.parkrepark.PostVehicleMovementRequest
import com.kemarport.kymsmahindra.repository.KYMSRepository
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.parkrepark.ParkReparkViewModel
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.parkrepark.ParkReparkViewModelFactory
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.barcode.BarcodeManager
import com.symbol.emdk.barcode.ScanDataCollection
import com.symbol.emdk.barcode.Scanner
import com.symbol.emdk.barcode.ScannerException
import com.symbol.emdk.barcode.ScannerResults
import com.symbol.emdk.barcode.StatusData
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import java.util.Timer
import java.util.TimerTask

class ParkReparkActivity : AppCompatActivity(), View.OnClickListener,
    RFIDHandlerForParkRepark.ResponseHandlerInterface,
    EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener, OnMapReadyCallback {
    lateinit var binding: ActivityParkReparkBinding
    var emdkManager: EMDKManager? = null
    var barcodeManager: BarcodeManager? = null
    var scanner: Scanner? = null

    var resumeFlag = false
    var TAG = "ParkInActivity"

    var rfidHandler: RFIDHandlerForParkRepark? = null

    var newLat = 0.0
    var newLng = 0.0

    var t = Timer()
    var tt: TimerTask? = null
    private lateinit var viewModel: ParkReparkViewModel

    private lateinit var session: SessionManager
    private lateinit var userDetails: HashMap<String, String?>
    private var token: String? = ""
    private var userName: String? = ""
    private var locationId: String? = ""
    private lateinit var progress: ProgressDialog

    val coordinatesMap = HashMap<String, ArrayList<LatLng>>()
    val idMap = HashMap<String, String>()

    var isRFIDInit = false
    var isBarcodeInit = false

    var map: GoogleMap? = null
    var currentMarker: Marker? = null

    private fun initReader() {
        rfidHandler = RFIDHandlerForParkRepark()
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
        if (t != null) {
            t.cancel()
            tt!!.cancel()
        }
        resumeFlag = true
    }

    override fun onPostResume() {
        super.onPostResume()
        binding.mapview.onResume()
        if (isRFIDInit) {
            val status = rfidHandler!!.onResume()
            Toast.makeText(this@ParkReparkActivity, status, Toast.LENGTH_SHORT).show()
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
    }

    fun performInventory() {
        rfidHandler!!.performInventory()
    }

    fun stopInventory() {
        rfidHandler!!.stopInventory()
    }

    var coordinatePref = ""

    lateinit var coordinates: ArrayList<LatLng>
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    val locationMap = HashMap<String, ArrayList<LatLng>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_park_repark)
        binding.listener = this
        binding.parkInVehicleToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
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

        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory =
            ParkReparkViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[ParkReparkViewModel::class.java]

        progress = ProgressDialog(this)
        progress.setMessage("Loading...")

        session = SessionManager(this)
        coordinatePref =
            Utils.getSharedPrefs(this@ParkReparkActivity, Constants.USER_COORDINATES).toString()
        coordinates = parseStringToList(coordinatePref)
        binding.parkInVehicleToolbar.title = "Park/Repark"

        userDetails = session.getUserDetails()
        token = userDetails["jwtToken"]
        userName = userDetails["userName"]
        locationId = userDetails["locationId"]

        // binding.radioGroup.check(binding.radioBtn2.getId())

        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            binding.scanBarcode.visibility = View.GONE
            //binding.radioGroup.visibility = View.VISIBLE
        } else {
            binding.scanBarcode.visibility = View.VISIBLE
            // binding.radioGroup.visibility = View.GONE
        }
        defaultRFID()
        defaultVinApiCall()
        getInternalYardLoc()
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


        viewModel.getAllInternalYardMutable.observe(this)
        { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        //if (resultResponse.size > 0) {
                        //for (e in resultResponse) {
                        if (resultResponse != null) {

                            for (e in resultResponse) {
                                if (e.locationType.equals("Internal")) {
                                    if (e.coordinates != null) {
                                        var coordinates: ArrayList<LatLng> =
                                            parseStringToList(e.coordinates)
                                        if (!coordinatesMap.containsKey(e.locationId.toString())) {
                                            coordinatesMap[e.locationId.toString()] = coordinates
                                        }
                                        if (!locationMap.containsKey(e.locationName)) {
                                            locationMap[e.locationName] = coordinates
                                        }
                                        Log.d("thiscoordinates", coordinates.toString())
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
                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR
                        ) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@ParkReparkActivity
                            )
                        }
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
                    clear()

                    response.data?.let { resultResponse ->
                        if (resultResponse.responseMessage != null) {
                            Toasty.success(
                                this@ParkReparkActivity,
                                resultResponse.responseMessage.toString(),
                                Toasty.LENGTH_SHORT
                            ).show()

                        } else if (resultResponse.errorMessage != null) {
                            Toasty.warning(
                                this@ParkReparkActivity,
                                resultResponse.errorMessage.toString(),
                                Toasty.LENGTH_SHORT
                            ).show()

                        }

                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    clear()
                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        session.showToastAndHandleErrors(resultResponse, this@ParkReparkActivity)

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
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        resultResponse?.let { vehicleInfo ->
                            binding.clVehicleImgs.visibility = View.VISIBLE
                            with(binding) {
                                tvVinValue.text = vehicleInfo.vin.orEmpty()
                                tvModelCodeValue.text = vehicleInfo.modelCode?.toString().orEmpty()
                                tvStatusValue.text = vehicleInfo.status.orEmpty()
                                ViewCompat.setBackgroundTintList(
                                    parkReparkBtn,
                                    ColorStateList.valueOf(color)
                                )
                                parkReparkBtn.isEnabled = true
                                when (vehicleInfo.status) {
                                    "Production Out" -> {
                                        parkReparkBtn.text = "Park Vehicle"
                                        parkReparkBtn.setOnClickListener {
                                            vehicleInfo.vin?.let { vin ->
                                                checkVehicleInsideGeofenceRFIDNew(vin, "Parking")
                                            }
                                        }
                                    }

                                    "Parking" -> {
                                        parkReparkBtn.text = "Repark Vehicle"
                                        parkReparkBtn.setOnClickListener {
                                            vehicleInfo.vin?.let { vin ->
                                                checkVehicleInsideGeofenceRFIDNew(vin, "Re Park")
                                            }
                                        }
                                    }

                                    "Re Park" -> {
                                        parkReparkBtn.text = "Repark Vehicle"
                                        parkReparkBtn.setOnClickListener {
                                            vehicleInfo.vin?.let { vin ->
                                                checkVehicleInsideGeofenceRFIDNew(vin, "Re Park")
                                            }
                                        }
                                    }

                                    "Delivered" -> {
                                        ViewCompat.setBackgroundTintList(
                                            parkReparkBtn,
                                            ColorStateList.valueOf(grey)
                                        )
                                        parkReparkBtn.text = "Park Vehicle"
                                        parkReparkBtn.isEnabled = false
                                        Toast.makeText(
                                            this@ParkReparkActivity,
                                            "Vehicle already Delivered",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    "Dispatched" -> {
                                     /*   parkReparkBtn.text = "Repark Vehicle"
                                        parkReparkBtn.setOnClickListener {
                                            vehicleInfo.vin?.let { vin ->
                                                checkVehicleInsideGeofenceRFIDNew(vin, "Re Park")
                                            }
                                        }*/
                                        //Toast.makeText(this@ParkReparkActivity, "Vehicle already Dispatched", Toast.LENGTH_SHORT).show()
                                        ViewCompat.setBackgroundTintList(
                                            parkReparkBtn,
                                            ColorStateList.valueOf(grey)
                                        )
                                        parkReparkBtn.text = "Park Vehicle"
                                        parkReparkBtn.isEnabled = false
                                        Toast.makeText(
                                            this@ParkReparkActivity,
                                            "Vehicle already Dispatched",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                }
                            }
                        }


                        /* binding.clVehicleImgs.visibility = View.VISIBLE

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

                         ViewCompat.setBackgroundTintList(
                             binding.parkReparkBtn,
                             ColorStateList.valueOf(color)
                         )
                         binding.parkReparkBtn.isEnabled = true
                         if (resultResponse.status.equals("Production Out")) {
                             binding.parkReparkBtn.setText("Park Vehicle")
                             binding.parkReparkBtn.setOnClickListener {
                                 resultResponse.vin?.let { it1 ->
                                     checkVehicleInsideGeofenceRFIDNew(
                                         it1, "Parking")
                                 }
                             }
                         } else if (resultResponse.status.equals("Parking")) {
                             binding.parkReparkBtn.setText("Repark Vehicle")
                             binding.parkReparkBtn.setOnClickListener {
                                 resultResponse.vin?.let { it1 ->
                                     checkVehicleInsideGeofenceRFIDNew(
                                         it1, "Re Park")
                                 }
                             }
                         } else if (resultResponse.status.equals("Re Park")) {
                             binding.parkReparkBtn.setText("Repark Vehicle")
                             binding.parkReparkBtn.setOnClickListener {
                                 resultResponse.vin?.let { it1 ->
                                     checkVehicleInsideGeofenceRFIDNew(
                                         it1, "Re Park")
                                 }
                             }
                         } else if (resultResponse.status.equals("Delivered")) {
                             ViewCompat.setBackgroundTintList(
                                 binding.parkReparkBtn,
                                 ColorStateList.valueOf(grey)
                             )
                             binding.parkReparkBtn.setText("Park Vehicle")
                             binding.parkReparkBtn.isEnabled = false
                         }*/

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
                        session.showToastAndHandleErrors(resultResponse, this@ParkReparkActivity)
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }

                else -> {}
            }
        }

        tt = object : TimerTask() {
            override fun run() {
                getLocationNew()
            }
        }

        t.scheduleAtFixedRate(tt, 1000, 1000)

    }

    private fun getInternalYardLoc() {
       try {
           viewModel.getAllInternalYardLocations(
               session.getToken(),
               Constants.BASE_URL
           )
       }
       catch (e:Exception)
       {
           Toasty.warning(
               this@ParkReparkActivity,
               e.message.toString(),
               Toasty.LENGTH_SHORT
           ).show()
       }
    }

    private fun defaultVinApiCall(){
        val intent = intent
        val vin = intent.getStringExtra("vin")
        binding.tvBarcode.setText(vin)
        vin?.let { getVehicleStatus(it) }
    }

    fun getVehicleStatus(scanned: String) {
        if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
            viewModel.getVehicleStatus(
                token!!,
                Constants.BASE_URL,
                GetVehicleStatusRequest("", scanned)
            )
        } else {
            runOnUiThread {
                Toasty.warning(
                    this@ParkReparkActivity,
                    "You are not inside Service Area!!",
                    Toasty.LENGTH_SHORT
                ).show()
            }

        }

    }

    private fun clear() {
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
        binding.parkReparkBtn.setText("Park Vehicle")
    }

    fun checkVehicleInsideGeofenceRFID(scanned: String, s: String) {
        if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
            for ((key, value) in coordinatesMap.entries) {
                if (containsLocation(LatLng(newLat, newLng), value, false)) {
                    viewModel.parkReparkVehicle(
                        token!!,
                        Constants.BASE_URL,
                        PostVehicleMovementRequest(
                            userName!!,
                            "$newLat,$newLng",
                            s,
                            scanned,
                            key.toInt()
                        )
                    )
                } else {

                    viewModel.parkReparkVehicle(
                        token!!,
                        Constants.BASE_URL,
                        PostVehicleMovementRequest(userName!!, "$newLat,$newLng", s, scanned, 2)
                    )
                    /*  runOnUiThread(Runnable {
                          Toasty.warning(
                              this@ParkReparkActivity,
                              "You are not inside parking!!",
                              Toasty.LENGTH_SHORT
                          ).show()
                      })*/
                }
            }
        } else {
            Toasty.warning(
                this@ParkReparkActivity,
                "You are not inside parking!!",
                Toasty.LENGTH_SHORT
            ).show()
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

            // Process based on whether the user is inside any geofence or not
            if (insideGeofence) {
                // User is inside a geofence
                viewModel.parkReparkVehicle(
                    token!!,
                    Constants.BASE_URL,
                    PostVehicleMovementRequest(
                        userName!!,
                        "$newLat,$newLng",
                        s,
                        scanned,
                        geofenceId
                    )
                )
            } else {
                // User is not inside any geofence
                viewModel.parkReparkVehicle(
                    token!!,
                    Constants.BASE_URL,
                    PostVehicleMovementRequest(
                        userName!!,
                        "$newLat,$newLng",
                        s,
                        scanned,
                        locationId!!.toInt()
                    )
                )
                Toasty.warning(
                    this@ParkReparkActivity,
                    "You are not inside any parking!!",
                    Toasty.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toasty.warning(
                this@ParkReparkActivity,
                e.message.toString(),
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
                updateTvCurrentLoc()
                val markerOptions =
                    MarkerOptions().position(LatLng(newLat, newLng)).title("You are here!")
                        .icon(BitmapDescriptorFactory.fromBitmap(generateLocationIcon()!!))
                currentMarker = map!!.addMarker(markerOptions)
                Log.e(TAG, "Latitude/Longitude - $newLat,$newLng")
            }
        })
    }

    /*    fun updateTvCurrentLoc( ) {
            if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
                for ((key, value) in locationMap.entries) {
                    if (containsLocation(LatLng(newLat, newLng), value, false)) {
                       binding.tvCurrentAt.setText("You are at $key !!")
                        break
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

    fun initBarcodeManager() {
        barcodeManager =
            emdkManager!!.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
        if (barcodeManager == null) {
            Toast.makeText(
                this@ParkReparkActivity,
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
                if (currentMarker != null) currentMarker!!.remove()
                val currentPos = LatLng(newLat, newLng)
                map!!.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
                map!!.animateCamera(CameraUpdateFactory.zoomTo(16f))
                println("la-$newLat, $newLng")
            }
        }
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
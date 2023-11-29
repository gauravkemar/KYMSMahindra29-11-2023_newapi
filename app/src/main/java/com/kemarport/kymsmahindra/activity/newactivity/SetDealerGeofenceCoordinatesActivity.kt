package com.kemarport.kymsmahindra.activity.newactivity

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.databinding.ActivitySetDealerGeofenceCoordinatesBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Gps
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.SessionManager
import com.kemarport.kymsmahindra.model.newapi.setgeofence.GetChildLocationsResponse
import com.kemarport.kymsmahindra.model.newapi.setgeofence.GetParentLocationsResponse
import com.kemarport.kymsmahindra.model.newapi.setgeofence.PostGeofenceCoordinatesRequest
import com.kemarport.kymsmahindra.repository.KYMSRepository
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.setgeofence.SetGeofenceViewModel
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.setgeofence.SetGeofenceViewModelFactory
import es.dmoral.toasty.Toasty
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask


class SetDealerGeofenceCoordinatesActivity : AppCompatActivity(),
    OnMapReadyCallback {
    lateinit var binding: ActivitySetDealerGeofenceCoordinatesBinding

    private lateinit var viewModel: SetGeofenceViewModel

    private lateinit var progress: ProgressDialog

    var map: GoogleMap? = null

    var currentMarker: Marker? = null

    var newLat = 0.0
    var newLng = 0.0

    var t = Timer()
    var tt: TimerTask? = null

    var latLngList: ArrayList<LatLng> = ArrayList()
    var markerList: ArrayList<Marker> = ArrayList()

    var builder: AlertDialog.Builder? = null
    var dialog: AlertDialog? = null

    lateinit var dealerSpinner: ArrayList<String>
    private var dealerSpinneradapter: ArrayAdapter<String>? = null

    lateinit var parentLocationArr: ArrayList<String>
    private var parentLocatinAdapter: ArrayAdapter<String>? = null

    lateinit var childLocationArr: ArrayList<String>
    private var childLocationadapter: ArrayAdapter<String>? = null


    val parentLocationCoordinatesMap = HashMap<String, ArrayList<LatLng>>()
    val childLocationCoordinateMap = HashMap<String, ArrayList<LatLng>>()

    val dealerMap = HashMap<String, String>()

    val parentLocationMap = HashMap<String, String>()
    val childLocationMap = HashMap<String, String>()
    private lateinit var userDetails: HashMap<String, String?>

    private var token: String? = ""
    private lateinit var session: SessionManager

    private var selectedDealerId: Int = 0
    private var selectedParentLocationId: Int = 0
    private var selectedChildLocationId: Int = 0

    private var getParentLocationArray = ArrayList<GetParentLocationsResponse>()
    private var getChildLocationsArray = ArrayList<GetChildLocationsResponse>()

    private var isIntialSelectDealer = true
    private var isInitialSelectParentLoc = true
    private var isInitialSelectChildLoc = true
    val defaultCoordinates: ArrayList<LatLng> =  ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_set_dealer_geofence_coordinates)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        binding.mapview.onCreate(savedInstanceState)
        binding.mapview.getMapAsync(this)
        binding.setGeofenceToolbar.title = "Set Dealer Geofence"
        binding.setGeofenceToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.setGeofenceToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()

        session = SessionManager(this)
        userDetails = session.getUserDetails()
        token = userDetails["jwtToken"]


        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory =
            SetGeofenceViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[SetGeofenceViewModel::class.java]
        callDealerList()
        dealerSpinner = ArrayList()
        parentLocationArr = ArrayList()
        childLocationArr = ArrayList()

        //if coordinates are null
        defaultCoordinates.add(LatLng(0.0, 0.0))
        defaultCoordinates.add(LatLng(0.0, 0.0))
        defaultCoordinates.add(LatLng(0.0, 0.0))

        viewModel.getAllActiveDealersMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    dealerSpinner.clear()
                    dealerSpinner.add("Select Dealer Code")
                    response.data?.let { resultResponse ->
                        if (resultResponse.size > 0) {
                            map?.clear()
                            for (e in resultResponse) {
                                dealerMap[e.key.toString()] = e.value
                                (dealerSpinner).add(e.value)
                            }
                            dealerSpinner()
                            parentLocatinAdapter?.notifyDataSetChanged()
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        session.showToastAndHandleErrors(
                            resultResponse,
                            this@SetDealerGeofenceCoordinatesActivity
                        )
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    /*    viewModel.getAllParentLocationsMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    parentLocationCoordinatesMap.clear()
                    getParentLocationArray.clear()
                    parentLocationArr.clear()
                    (parentLocationArr).add("Select Parent Location")
                    response.data?.let { resultResponse ->
                        if (resultResponse.size > 0) {
                            map?.clear()
                            binding.clSpinnerDealerParentLoc.visibility = View.VISIBLE
                            for (e in resultResponse) {
                                var coordinates: ArrayList<LatLng> =
                                    parseStringToList(e.coordinates)
                                if (!parentLocationCoordinatesMap.containsKey(e.value)) {
                                    parentLocationCoordinatesMap[e.value] = coordinates
                                    parentLocationMap[e.key.toString()] = e.value
                                    (parentLocationArr).add(e.value)
                                }
                                Log.d("coordinates", coordinates.toString())
                                val polygonOptions =
                                    PolygonOptions().addAll(coordinates).clickable(false)
                                        .strokeColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.colorPrimaryLight
                                            )
                                        )
                                        .strokeWidth(3f)
                                map?.addPolygon(polygonOptions)

                            }
                            dealerParentLocationSpinner()
                            getParentLocationArray.addAll(resultResponse)
                            parentLocatinAdapter?.notifyDataSetChanged()
                        } else {
                            binding.clSpinnerDealerParentLoc.visibility = View.GONE
                            binding.clSpinnerDealerChildLocation.visibility = View.GONE
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    binding.clSpinnerDealerParentLoc.visibility = View.GONE
                    binding.clSpinnerDealerChildLocation.visibility = View.GONE
                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        session.showToastAndHandleErrors(
                            resultResponse,
                            this@SetDealerGeofenceCoordinatesActivity
                        )
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
*/
        viewModel.getAllParentLocationsMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    parentLocationCoordinatesMap.clear()
                    getParentLocationArray.clear()
                    parentLocationArr.clear()
                    (parentLocationArr).add("Select Parent Location")
                    response.data?.let { resultResponse ->
                        if (resultResponse.size > 0) {
                            map?.clear()
                            binding.clSpinnerDealerParentLoc.visibility = View.VISIBLE
                            for (e in resultResponse) {
                                if (e.coordinates != null) {
                                    var coordinates: java.util.ArrayList<LatLng> = parseStringToList(e.coordinates)
                                    if (coordinates.isNotEmpty()) {
                                        Log.d("coordinates", coordinates.toString())
                                        val polygonOptions =
                                            PolygonOptions().addAll(coordinates).clickable(false)
                                                .strokeColor(
                                                    ContextCompat.getColor(
                                                        this,
                                                        R.color.colorPrimaryLight
                                                    )
                                                )
                                                .strokeWidth(3f)

                                        map?.addPolygon(polygonOptions)
                                        if (!parentLocationCoordinatesMap.containsKey(e.value)) {
                                            parentLocationCoordinatesMap[e.value] = coordinates
                                            parentLocationMap[e.code] = e.value
                                            (parentLocationArr).add(e.value)
                                        }
                                    }
                                } else {
                                    if (!parentLocationCoordinatesMap.containsKey(e.value)) {
                                        parentLocationCoordinatesMap[e.value] = defaultCoordinates
                                        parentLocationMap[e.code] = e.value
                                        (parentLocationArr).add(e.value)
                                    }
                                    Log.d("coordinates", "Coordinates are null for entry: $e")
                                    // Add your specific handling if needed
                                }
                            }
                            dealerParentLocationSpinner()
                            getParentLocationArray.addAll(resultResponse)
                            parentLocatinAdapter?.notifyDataSetChanged()
                        } else {
                            binding.clSpinnerDealerParentLoc.visibility = View.GONE
                            binding.clSpinnerDealerChildLocation.visibility = View.GONE
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    binding.clSpinnerDealerParentLoc.visibility = View.GONE
                    binding.clSpinnerDealerChildLocation.visibility = View.GONE
                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        session.showToastAndHandleErrors(
                            resultResponse,
                            this@SetDealerGeofenceCoordinatesActivity
                        )
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        viewModel.getAllChildLocationsMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    childLocationCoordinateMap.clear()
                    getChildLocationsArray.clear()
                    childLocationArr.clear()
                    (childLocationArr).add("Select Child Location")
                    response.data?.let { resultResponse ->
                        if (resultResponse.size > 0) {
                            binding.clSpinnerDealerChildLocation.visibility = View.VISIBLE
                      /*      for (e in resultResponse) {
                                var coordinates: ArrayList<LatLng> =
                                    parseStringToList(e.coordinates)
                                if (!childLocationCoordinateMap.containsKey(e.value)) {
                                    childLocationCoordinateMap[e.value] = coordinates
                                    childLocationMap[e.key.toString()] = "${e.value}"
                                    (childLocationArr).add(e.value)
                                }
                                Log.d("coordinates", coordinates.toString())
                                val polygonOptions =
                                    PolygonOptions().addAll(coordinates).clickable(false)
                                        .strokeColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.colorPrimaryLight
                                            )
                                        )
                                        .strokeWidth(3f)
                                map?.addPolygon(polygonOptions)
                            }*/

                            for (e in resultResponse) {
                                if (e.coordinates != null) {
                                    var coordinates: ArrayList<LatLng> =
                                        parseStringToList(e.coordinates)
                                    if (coordinates.isNotEmpty()) {
                                        Log.d("coordinates", coordinates.toString())
                                        val polygonOptions =
                                            PolygonOptions().addAll(coordinates).clickable(false)
                                                .strokeColor(
                                                    ContextCompat.getColor(
                                                        this,
                                                        R.color.colorPrimaryLight
                                                    )
                                                )
                                                .strokeWidth(3f)

                                        map?.addPolygon(polygonOptions)
                                        if (!childLocationCoordinateMap.containsKey(e.value)) {
                                            childLocationCoordinateMap[e.value] = coordinates
                                            childLocationMap[e.key.toString()] = "${e.value}"
                                            (childLocationArr).add(e.value)
                                        }
                                    }
                                } else {
                                    if (!childLocationCoordinateMap.containsKey(e.value)) {
                                        childLocationCoordinateMap[e.value] = defaultCoordinates
                                        childLocationMap[e.key.toString()] = "${e.value}"
                                        (childLocationArr).add(e.value)
                                    }
                                    Log.d("coordinates", "Coordinates are null for entry: $e")
                                    // Add your specific handling if needed
                                }
                            }
                            childLocationSpinner()
                            getChildLocationsArray.addAll(resultResponse)
                            childLocationadapter?.notifyDataSetChanged()
                        } else {
                            binding.clSpinnerDealerChildLocation.visibility = View.GONE
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        session.showToastAndHandleErrors(
                            resultResponse,
                            this@SetDealerGeofenceCoordinatesActivity
                        )
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        viewModel.postGeofenceCoordinatesMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    setToDefault()
                    response.data?.let { resultResponse ->

                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        session.showToastAndHandleErrors(
                            resultResponse,
                            this@SetDealerGeofenceCoordinatesActivity
                        )
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        tt = object : TimerTask() {
            override fun run() {
                getLocationNew()
            }
        }
        t.scheduleAtFixedRate(tt, 1000, 1000)

        binding.btnRecenter.setOnClickListener {
            if (currentMarker != null) currentMarker!!.remove()
            val currentPos = LatLng(newLat, newLng)
            map!!.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
            map!!.animateCamera(CameraUpdateFactory.zoomTo(16f))
            println("la-$newLat, $newLng")
        }
        binding.btClear.setOnClickListener {
            setToDefault()
        }
        binding.addGeofence.setOnClickListener {
            if (latLngList.size > 3)
                postGeofenceCoordinates()
            else
                session.showToastAndHandleErrors(
                    "Please draw atleast 4 \n points on map to add Geofence!",
                    this@SetDealerGeofenceCoordinatesActivity
                )
        }


    }

    private fun setToDefault() {
        callDealerList()
        isIntialSelectDealer = true
        isInitialSelectParentLoc = true
        isInitialSelectChildLoc = true
        binding.clSpinnerDealerParentLoc.visibility = View.GONE
        binding.clSpinnerDealerChildLocation.visibility = View.GONE
        selectedDealerId = 0
        selectedParentLocationId = 0
        selectedChildLocationId = 0
        clearAllMarker()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapview.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        binding.mapview.onPause()
        if (t != null) {
            t.cancel()
            tt!!.cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapview.onResume()

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

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapview.onLowMemory()
    }

    private fun dealerSpinner() {

        dealerSpinneradapter = ArrayAdapter<String>(this, R.layout.spinner_item, dealerSpinner)
        binding.spinnerDealerList.adapter = dealerSpinneradapter
        binding.spinnerDealerList.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    val selectedItem = adapterView?.selectedItem.toString()

                    val selectedItemPosi = adapterView?.selectedItemPosition

                    if (!isIntialSelectDealer) {
                        if (dealerSpinner[i] != "Select Dealer Code") {
                            val selectedKey: String? =
                                dealerMap.entries.find { it.value == selectedItem }?.key
                            selectedDealerId = selectedKey!!.toInt()
                            // callParentLocationApi(selectedKey)
                            callParentLocationApi(selectedKey)
                        }
                    }
                    isIntialSelectDealer = false
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
    }

    private fun dealerParentLocationSpinner() {

        parentLocatinAdapter = ArrayAdapter<String>(this, R.layout.spinner_item, parentLocationArr)
        binding.spinnerDealerParentLocationList.adapter = parentLocatinAdapter
        binding.spinnerDealerParentLocationList.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    val selectedItem = adapterView?.selectedItem.toString()

                    val selectedItemPosi = adapterView?.selectedItemPosition

                    /*   for(p in getParentLocationArray)
                       {
                           if(p.equals(selectedKey))
                           {
                               selectedParentLocationId=p.key
                           }
                       }*/

                    if (!isInitialSelectParentLoc) {
                        if (parentLocationArr[i] != "Select Parent Location") {
                            val selectedKey: String? =
                                parentLocationMap.entries.find { it.value == selectedItem }?.key
                            callChildLocationApi(selectedKey)
                            val selectedParentLocation =
                                getParentLocationArray.find { it.code == selectedKey }
                            if (selectedParentLocation != null)
                                selectedParentLocationId = selectedParentLocation.key

                        }
                    }
                    isInitialSelectParentLoc = false
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
    }

    private fun childLocationSpinner() {

        childLocationadapter = ArrayAdapter<String>(this, R.layout.spinner_item, childLocationArr)
        binding.spinnerDealerChildLocationList.adapter = childLocationadapter
        binding.spinnerDealerChildLocationList.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    val selectedItem = adapterView?.selectedItem.toString()
                    val selectedItemPosi = adapterView?.selectedItemPosition

                    if (!isInitialSelectChildLoc) {
                        if (childLocationArr[i] != "Select Child Location") {
                            val selectedKey: String? =
                                childLocationMap.entries.find { it.value == selectedItem }?.key
                            //callChildLocationApi(selectedKey)
                            val selectedChildLocation =
                                getChildLocationsArray.find { it.key.toString() == selectedKey }
                            if (selectedChildLocation != null)
                                selectedChildLocationId = selectedChildLocation.key
                        }
                    }
                    isInitialSelectChildLoc = false

                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
    }

    /* override fun onClick(view: View?) {
     when (view!!.id) {
         R.id.bt_clear -> clearAllMarker()
         R.id.btnRecenter -> {
             if (currentMarker != null) currentMarker!!.remove()
             val currentPos = LatLng(newLat, newLng)
             map!!.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
             map!!.animateCamera(CameraUpdateFactory.zoomTo(16f))
             println("la-$newLat, $newLng")
         }
     }
 }*/
    fun clearAllMarker() {
        for (marker in markerList) marker.remove()
        latLngList.clear()
        markerList.clear()
    }

    private fun callDealerList() {
        try {

            viewModel.getAllActiveDealers(token!!, Constants.BASE_URL)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun callParentLocationApi(selectedKey: String?) {
        try {

            viewModel.getAllParentLocations(token!!, Constants.BASE_URL, selectedKey!!.toInt())
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun callChildLocationApi(selectedKey: String?) {
        try {

            viewModel.getAllChildLocations(token!!, Constants.BASE_URL, selectedKey!!.toInt())
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun postGeofenceCoordinates() {
        try {
            var arr = latLngList.toString()
            if (selectedDealerId != 0) {
                viewModel.postGeofenceCoordinates(
                    token!!, Constants.BASE_URL,
                    PostGeofenceCoordinatesRequest(
                        selectedChildLocationId, arr,
                        selectedDealerId, selectedParentLocationId
                    )
                )

                Log.d("parentchildloc","$selectedDealerId:dealer-$selectedParentLocationId:parentloc-$selectedChildLocationId:child")


               /* selectedChildLocationId?.let {
                    selectedDealerId?.let { it1 ->
                        selectedParentLocationId?.let { it2 ->
                            PostGeofenceCoordinatesRequest(
                                it, arr,
                                it1, it2
                            )
                        }
                    }
                }
                    ?.let {
                        viewModel.postGeofenceCoordinates(
                            token!!, Constants.BASE_URL,
                            it
                        )
                    }*/
            } else {
                Toast.makeText(this, "Please select DealerId/Parent Location", Toast.LENGTH_SHORT)
                    .show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
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
                if (currentMarker != null) currentMarker!!.remove()
                val markerOptions =
                    MarkerOptions().position(LatLng(newLat, newLng)).title("You are here!")
                        .icon(BitmapDescriptorFactory.fromBitmap(generateLocationIcon()!!))
                currentMarker = map?.addMarker(markerOptions)
            }
        })
    }

    fun generateLocationIcon(): Bitmap? {
        val height = 40
        val width = 40
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_pin)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    fun generateSmallIcon(): Bitmap? {
        val height = 20
        val width = 20
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.location_marker_green)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.isMyLocationButtonEnabled = false
        //map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        resetMap()

        map?.setOnMapClickListener { latLng: LatLng? -> addMarker(latLng) }
    }

    fun resetMap() {
        map?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(20.5937, 78.9629)))
        map?.animateCamera(CameraUpdateFactory.zoomTo(3f))
    }

    fun addMarker(latLng: LatLng?) {
        val markerOptions = MarkerOptions().position(latLng!!).anchor(0.5f, 0.5f).icon(
            BitmapDescriptorFactory.fromBitmap(
                generateSmallIcon()!!
            )
        )
        val marker = map?.addMarker(markerOptions)
        latLngList.add(latLng)
        markerList.add(marker!!)
    }

    private fun resetPolygon(polygonName: String) {
        map?.clear()
        for ((key, value) in parentLocationCoordinatesMap.entries) {
            if (polygonName.equals(key, true)) {
                val polygonOptions =
                    PolygonOptions().addAll(value).clickable(false)
                        .strokeColor(
                            ContextCompat.getColor(
                                this,
                                R.color.red
                            )
                        )
                        .strokeWidth(3f)
                map?.addPolygon(polygonOptions)
            } else if (polygonName.equals("")) {
                val polygonOptions =
                    PolygonOptions().addAll(value).clickable(false)
                        .strokeColor(
                            ContextCompat.getColor(
                                this,
                                R.color.colorPrimaryLight
                            )
                        )
                        .strokeWidth(3f)
                map?.addPolygon(polygonOptions)
            } else {
                val polygonOptions =
                    PolygonOptions().addAll(value).clickable(false)
                        .strokeColor(
                            ContextCompat.getColor(
                                this,
                                R.color.grey
                            )
                        )
                        .strokeWidth(3f)
                map?.addPolygon(polygonOptions)
            }
        }
    }
}
package com.kemarport.kymsmahindra.activity.newactivity


import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.adapter.SearchVehicleAdapter
import com.kemarport.kymsmahindra.databinding.ActivitySearchVehicleBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Constants.LATITUDE
import com.kemarport.kymsmahindra.helper.Constants.LONGITUDE
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.SessionManager
import com.kemarport.kymsmahindra.model.newapi.searchvehicles.GetSearchVehiclesListResponse
import com.kemarport.kymsmahindra.repository.KYMSRepository
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.searchvehicle.SearchVehicleViewModel
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.searchvehicle.SearchVehicleViewModelFactory
import es.dmoral.toasty.Toasty
import java.util.HashMap
import kotlin.collections.ArrayList

class SearchVehicleActivity : AppCompatActivity() {
    lateinit var listItems: ArrayList<GetSearchVehiclesListResponse>
    lateinit var colorsList: ArrayList<String>
    lateinit var modelCodeList: ArrayList<String>
    lateinit var binding: ActivitySearchVehicleBinding
    private lateinit var viewModel: SearchVehicleViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private var vehicleListRecycler: RecyclerView? = null
    private var vehicleRecyclerAdapter: SearchVehicleAdapter? = null
    private var token: String? = ""


    private var selectedColor:String=""
    private var selectedModelCode:String=""
    private var isIntialSelectionColor = true
    private var isInitialSelectionModel = true
    private lateinit var userDetails: HashMap<String, String?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_vehicle)
        binding.searchVehicleToolbar.title = "Find/Search Vehicle"
        binding.searchVehicleToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.searchVehicleToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        modelCodeList=ArrayList()
        colorsList= ArrayList()
        listItems= ArrayList()


        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory = SearchVehicleViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[SearchVehicleViewModel::class.java]
        session = SessionManager(this)
        userDetails = session.getUserDetails()
        token = userDetails["jwtToken"]
        searchVehicleDetails("","")
        getModelCode( )
        getColorDescription( )
        viewModel.getSearchVehicleListMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    listItems.clear()
                    response.data?.let { resultResponse ->
                        try {
                            listItems = resultResponse
                            setVehicleList(listItems)
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@SearchVehicleActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@SearchVehicleActivity,
                            "Error Message: $errorMessage"
                        ).show()
                        if (errorMessage == "Unauthorized" || errorMessage == "Authentication token expired" ||
                            errorMessage == Constants.CONFIG_ERROR) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@SearchVehicleActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }


        viewModel.getVehicleModelsMutable .observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    modelCodeList.clear()
                    response.data?.let { resultResponse ->
                        try {
                            if(resultResponse.size>0)
                            {
                                modelCodeList.add("Select Code")
                                for(modelCode in resultResponse )
                                modelCodeList.add(modelCode.model)
                            }

                            loadModelCodeSpinner(modelCodeList)
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@SearchVehicleActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@SearchVehicleActivity,
                            "Error Message: $errorMessage"
                        ).show()
                        if (errorMessage == "Unauthorized" || errorMessage == "Authentication token expired" ||
                            errorMessage == Constants.CONFIG_ERROR) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@SearchVehicleActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        viewModel.getVehicleColorsMutable .observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    colorsList.clear()
                    response.data?.let { resultResponse ->
                        try {
                            if(resultResponse.size>0)
                            {
                                colorsList.add("Select Color")
                                for(modelCode in resultResponse )
                                    colorsList.add(modelCode.color)
                            }
                            loadModelColorSpinner(colorsList)
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@SearchVehicleActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@SearchVehicleActivity,
                            "Error Message: $errorMessage"
                        ).show()
                        if (errorMessage == "Unauthorized" || errorMessage == "Authentication token expired" ||
                            errorMessage == Constants.CONFIG_ERROR) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@SearchVehicleActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
       /* viewModel.generateTokenMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        try {
                            viewModel.searchVehicleList(
                                token = "bearer ${resultResponse.jwtToken}",
                                vehicleListRequest = SearchVehiclePostRequest(
                                    UserName = "Bhola",
                                    Password = "Bhola@123"
                                )
                            )
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@SearchVehicleActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@SearchVehicleActivity,
                            "Error Message: $errorMessage"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }*/
        binding.mcvBtnClear.setOnClickListener {
            binding.etSearch.setText("")
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })

        binding.getAllVehicles.setOnClickListener {
            searchVehicleDetails("","")
            getModelCode( )
            getColorDescription( )
            isIntialSelectionColor=true
            isInitialSelectionModel=true
            selectedColor=""
            selectedModelCode=""
        }
    }
    private fun loadModelCodeSpinner(arr:  ArrayList<String>) {

        val spinner = findViewById<Spinner>(R.id.spinnerModel)

        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, arr
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {


                    if (!isInitialSelectionModel){
                        if(arr[position]!="Select Code")
                        {
                            selectedModelCode = arr[position]
                            searchVehicleDetails(selectedModelCode,selectedColor)
                        }
                    }
                    isInitialSelectionModel = false
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedModelCode = arr[0]
                }

            }
        }
    }

    private fun loadModelColorSpinner(arr:  ArrayList<String>) {


        val spinner = findViewById<Spinner>(R.id.spinnerColor)

        if (spinner != null) {

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, arr
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {

                    if (!isIntialSelectionColor) {
                        if(arr[position]!="Select Color")
                        {
                            selectedColor = arr[position]
                            searchVehicleDetails(selectedModelCode,selectedColor)
                        }
                    }

                    isIntialSelectionColor = false
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedColor = arr[0]
                }

            }
        }
    }


    override fun onResume() {
        super.onResume()
        searchVehicleDetails("","")
    }
    private fun searchVehicleDetails(model:String?,color:String){
        try {
            viewModel.getSearchVehicleList (token!!,Constants.BASE_URL,model,color)
        } catch (e: Exception) {
            Toasty.warning(
                this@SearchVehicleActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun getModelCode( ){
        try {
            viewModel.getVehicleModels (token!!,Constants.BASE_URL )
        } catch (e: Exception) {
            Toasty.warning(
                this@SearchVehicleActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }
    private fun getColorDescription( ){
        try {
            viewModel.getVehicleColors (token!!,Constants.BASE_URL )
        } catch (e: Exception) {
            Toasty.warning(
                this@SearchVehicleActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }
    private fun filterItems(search: String) {
        listItems?.let {
            val searchItesm = it.filter { it.vin?.contains(search) == true }
            setVehicleList(searchItesm)
        }
    }

    private fun setVehicleList(vehicleListModel: List<GetSearchVehiclesListResponse>) {
        vehicleListRecycler = findViewById(R.id.vehicle_list_rc)
        vehicleRecyclerAdapter = SearchVehicleAdapter({
            startActivity(
                Intent(
                    this@SearchVehicleActivity,
                    VehicleDetailsScreen::class.java
                ).apply {
                    putExtra("model", it)
                })

        }) {
            startActivity(
                Intent(
                    this@SearchVehicleActivity,
                    NavigateVehicleActivity::class.java
                ).apply {
             /*       it.coordinates?.let { it1 -> Utils.parseString(it1) }?.get(0)
                        ?.let { it1 -> putExtra(LATITUDE, it1.latitude) }
                    it.coordinates?.let { it1 -> Utils.parseString(it1) }?.get(0)
                        ?.let { it1 -> putExtra(LONGITUDE, it1.longitude) }*/
                    val (latitude: Double?, longitude) = it.coordinates?.split(",")!!.map { it.toDoubleOrNull() }
                    if (latitude != null && longitude != null) {
                        val intent = Intent(this@SearchVehicleActivity, NavigateVehicleActivity::class.java)
                        intent.putExtra(LATITUDE, latitude)
                        intent.putExtra(LONGITUDE, longitude)
                        startActivity(intent)
                    } else {
                        // Handle invalid coordinates here if needed.
                    }
                })
        }


        vehicleRecyclerAdapter?.setVehicleList(vehicleListModel, this@SearchVehicleActivity)
        vehicleListRecycler!!.adapter = vehicleRecyclerAdapter
        binding.vehicleListRc.layoutManager = LinearLayoutManager(this)
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

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }
}
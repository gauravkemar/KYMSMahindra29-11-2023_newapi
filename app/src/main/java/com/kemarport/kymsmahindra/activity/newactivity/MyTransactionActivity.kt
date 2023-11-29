package com.kemarport.kymsmahindra.activity.newactivity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.adapter.SearchVehicleAdapter
import com.kemarport.kymsmahindra.databinding.ActivityMyTransactionBinding
import com.kemarport.kymsmahindra.databinding.ActivitySearchVehicleBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.SessionManager
import com.kemarport.kymsmahindra.model.newapi.searchvehicles.GetSearchVehiclesListResponse
import com.kemarport.kymsmahindra.repository.KYMSRepository
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.searchvehicle.SearchVehicleViewModel
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.searchvehicle.SearchVehicleViewModelFactory
import es.dmoral.toasty.Toasty
import java.util.HashMap

class MyTransactionActivity : AppCompatActivity() {

    private var listItems: ArrayList<GetSearchVehiclesListResponse>? = null
    lateinit var binding:ActivityMyTransactionBinding
    private lateinit var viewModel: SearchVehicleViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private var vehicleListRecycler: RecyclerView? = null
    private var vehicleRecyclerAdapter: SearchVehicleAdapter? = null
    private var token: String? = ""
    private var userName: String? = ""
    private lateinit var userDetails: HashMap<String, String?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_my_transaction)
        binding.myTransactionToolbar.title = "MY Transaction"
        binding.myTransactionToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.myTransactionToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

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
        userName = userDetails["userName"]
        searchVehicleDetails()
        viewModel.getMyTransactionsMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            listItems = resultResponse
                            var searchTx = binding.etSearch.text.toString()
                            if (searchTx.isEmpty()) {
                                setVehicleList(listItems!!)
                            } else {
                                binding.etSearch.addTextChangedListener(object : TextWatcher {
                                    override fun onTextChanged(
                                        s: CharSequence?,
                                        start: Int,
                                        before: Int,
                                        count: Int
                                    ) {
                                        val searchText = s.toString().trim().toLowerCase()
                                        if (searchText != null && searchText.isNotEmpty()) {
                                            val filteredListList =
                                                ArrayList(listItems!!.filter {
                                                    it.vin?.toLowerCase()!!.contains(searchText)
                                                })
                                            setVehicleList(filteredListList)
                                        } else {
                                            setVehicleList(listItems!!)
                                        }
                                    }

                                    override fun beforeTextChanged(
                                        s: CharSequence?,
                                        start: Int,
                                        count: Int,
                                        after: Int
                                    )
                                    {

                                    }

                                    override fun afterTextChanged(s: Editable?) {}
                                })
                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@MyTransactionActivity,
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
                            this@MyTransactionActivity,
                            "Error Message: $errorMessage"
                        ).show()
                        if (errorMessage == "Unauthorized" || errorMessage == "Authentication token expired" ||
                            errorMessage == Constants.CONFIG_ERROR) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@MyTransactionActivity
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
    }

    override fun onResume() {
        super.onResume()
        searchVehicleDetails()
    }
    private fun searchVehicleDetails(){
        try {
            viewModel.getMyTransactions (token!!, Constants.BASE_URL,userName!!)
        } catch (e: Exception) {
            Toasty.warning(
                this@MyTransactionActivity,
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
        vehicleListRecycler = findViewById(R.id.my_transaction_rc)
        vehicleRecyclerAdapter = SearchVehicleAdapter({
            startActivity(
                Intent(
                    this@MyTransactionActivity,
                    VehicleDetailsScreen::class.java
                ).apply {
                    putExtra("model", it)
                })
        }) {
            startActivity(
                Intent(
                    this@MyTransactionActivity,
                    NavigateVehicleActivity::class.java
                ).apply {
                    /*       it.coordinates?.let { it1 -> Utils.parseString(it1) }?.get(0)
                               ?.let { it1 -> putExtra(LATITUDE, it1.latitude) }
                           it.coordinates?.let { it1 -> Utils.parseString(it1) }?.get(0)
                               ?.let { it1 -> putExtra(LONGITUDE, it1.longitude) }*/
                    val (latitude: Double?, longitude) = it.coordinates?.split(",")!!.map { it.toDoubleOrNull() }
                    if (latitude != null && longitude != null) {
                        val intent = Intent(this@MyTransactionActivity, NavigateVehicleActivity::class.java)
                        intent.putExtra(Constants.LATITUDE, latitude)
                        intent.putExtra(Constants.LONGITUDE, longitude)
                        startActivity(intent)
                    } else {
                        // Handle invalid coordinates here if needed.
                    }
                })
        }


        vehicleRecyclerAdapter?.setVehicleList(vehicleListModel, this@MyTransactionActivity)
        vehicleListRecycler!!.adapter = vehicleRecyclerAdapter
        binding.myTransactionRc.layoutManager = LinearLayoutManager(this)
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
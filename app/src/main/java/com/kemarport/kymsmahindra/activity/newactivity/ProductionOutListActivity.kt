package com.kemarport.kymsmahindra.activity.newactivity

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.adapter.PrdOutAdapter
import com.kemarport.kymsmahindra.databinding.ActivityProductionOutListBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.SessionManager
import com.kemarport.kymsmahindra.model.newapi.prdout.PrdOutListResponse
import com.kemarport.kymsmahindra.repository.KYMSRepository
import com.kemarport.kymsmahindra.viewmodel.login.LoginViewModel
import com.kemarport.kymsmahindra.viewmodel.login.LoginViewModelFactory
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.productionout.ProductionOutListViewModel
import com.kemarport.kymsmahindra.viewmodel.newviewmodel.productionout.ProductionOutListViewModelFactory
import es.dmoral.toasty.Toasty
import java.util.HashMap

class ProductionOutListActivity : AppCompatActivity() {
    lateinit var binding:ActivityProductionOutListBinding
    lateinit var viewModel:ProductionOutListViewModel
    private lateinit var session: SessionManager
    private lateinit var progress: ProgressDialog
    private var token: String? = ""
    private lateinit var userDetails: HashMap<String, String?>
    private var prdAdapter: PrdOutAdapter? = null
    lateinit var prdOutListResponse: ArrayList<PrdOutListResponse>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_production_out_list)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()
        binding.productionOutToolbar.title = "PRD OUT"
        binding.productionOutToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.productionOutToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory = ProductionOutListViewModelFactory(application, kymsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[ProductionOutListViewModel::class.java]
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        session = SessionManager(this)
        userDetails = session.getUserDetails()
        token = userDetails["jwtToken"]


        prdOutListResponse = ArrayList()
        callApi()
        viewModel.getVehicleStatusMutable .observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    //hideProgressBar()
                    prdOutListResponse.clear()
                    response.data?.let { resultResponse ->
                        if(resultResponse.size>0)
                        {
                            prdOutListResponse.addAll(resultResponse)
                            setPrdOutListRc(prdOutListResponse)
                        }
                    }
                }
                is Resource.Error -> {
                    // hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        session.showToastAndHandleErrors(resultResponse,this@ProductionOutListActivity)
                    }
                }
                is Resource.Loading -> {
                    // showProgressBar()
                }
                else -> {
                    //
                }
            }
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
       binding.mcvBtnClear.setOnClickListener {
           binding.etSearch.setText("")
       }
    }

    override fun onResume() {
        super.onResume()
        callApi()
    }
    private fun filterItems(search: String)
    {
        prdOutListResponse?.let{
            val searchItesm = it.filter { it.vin?.contains(search) == true } as ArrayList<PrdOutListResponse>
            setPrdOutListRc(searchItesm)
        }
    }
    private fun callApi()
    {
        try
        {
            viewModel.getVehicleStatus(token!!,Constants.BASE_URL)
        }
        catch (e:Exception)
        {
            session.showToastAndHandleErrors(e.message.toString(),this@ProductionOutListActivity)
        }
    }
    private fun setPrdOutListRc(prdOutListResponse: ArrayList<PrdOutListResponse>)
    {
        prdAdapter = PrdOutAdapter()
        prdAdapter?.setDockLevelerList(prdOutListResponse, this@ProductionOutListActivity)
        binding.vehicleListRc!!.adapter = prdAdapter
        binding.vehicleListRc.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            android.R.id.home ->
            {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}
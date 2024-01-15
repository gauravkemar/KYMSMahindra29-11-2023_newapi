package com.kemarport.kymsmahindra.activity.newactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.databinding.ActivityVehicleDetailsScreenBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.model.newapi.searchvehicles.GetSearchVehiclesListResponse
class VehicleDetailsScreen : AppCompatActivity() {
    lateinit var binding: ActivityVehicleDetailsScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehicle_details_screen)
        binding.searchVehicleToolbar.title = "Find/Search Vehicle"
        binding.searchVehicleToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.searchVehicleToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val previousScreenDataModel = intent.getParcelableExtra<GetSearchVehiclesListResponse>("model")
        binding.tvColorValue.text = previousScreenDataModel?.colorDescription
        //binding.tvEngineValue.text = previousScreenDataModel?.country
        binding.tvModelValue.text = previousScreenDataModel?.modelCode
        binding.tvVinValue.text = previousScreenDataModel?.vin
        binding.tvCurrentLocationValue.text = previousScreenDataModel?.currentLocation

        binding.ivNavigate.setOnClickListener {
            startActivity(
                Intent(
                    this@VehicleDetailsScreen,
                    NavigateVehicleActivity::class.java
                ).apply {
                    previousScreenDataModel.run {
                     /*   this?.coordinates?.let { it1 -> Utils.parseString(it1) }?.get(0)
                            ?.let { it1 -> putExtra(Constants.LATITUDE, it1.latitude) }
                        this?.coordinates?.let { it1 -> Utils.parseString(it1) }?.get(0)
                            ?.let { it1 -> putExtra(Constants.LONGITUDE, it1.longitude) }*/

                        val (latitude: Double?, longitude) = this?.coordinates?.split(",")!!.map { it.toDoubleOrNull() }
                        if (latitude != null && longitude != null) {
                            val intent = Intent(this@VehicleDetailsScreen, NavigateVehicleActivity::class.java)
                            intent.putExtra(Constants.LATITUDE, latitude)
                            intent.putExtra(Constants.LONGITUDE, longitude)
                            startActivity(intent)
                        } else {
                            // Handle invalid coordinates here if needed.
                        }
                    }
                }
            )
        }
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
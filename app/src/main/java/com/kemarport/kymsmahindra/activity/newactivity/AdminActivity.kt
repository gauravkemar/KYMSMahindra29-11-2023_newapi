package com.kemarport.kymsmahindra.activity.newactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    lateinit var binding:ActivityAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding=DataBindingUtil.setContentView(this,R.layout.activity_admin)
        binding.adminSettingToolbar .title = "Admin Settings"
        binding.adminSettingToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.adminSettingToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.mcvYardGeofence.setOnClickListener {
            startActivity(Intent(this@AdminActivity,SetYardGeofenceActivity::class.java))
        }

        binding.mcvDealerGeofence.setOnClickListener {
            startActivity(Intent(this@AdminActivity,SetDealerGeofenceCoordinatesActivity::class.java))
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
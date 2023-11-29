package com.kemarport.kymsmahindra.activity.newactivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Utils

class SplashActivity : AppCompatActivity() {

    private val SPLASH_SCREEN_TIME_OUT = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Handler().postDelayed({
            if (Utils.getSharedPrefsBoolean(this@SplashActivity, Constants.LOGGEDIN, false)) {
                Utils.setSharedPrefsBoolean(this@SplashActivity, Constants.LOGGEDIN, true)
                startActivity(Intent(this@SplashActivity, HomeMenuActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }
        }, SPLASH_SCREEN_TIME_OUT)
    }
}
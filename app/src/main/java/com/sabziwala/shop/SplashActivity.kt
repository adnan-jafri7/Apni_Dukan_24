package com.sabziwala.shop

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        var appVersion=findViewById<TextView>(R.id.txtApp_Version)
        var versionCode=BuildConfig.VERSION_NAME
        appVersion.text=versionCode.toString()
        Handler().postDelayed({
            val homeIntent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(homeIntent)
            finish()
        }, SPLASH_TIME_OUT.toLong())
    }

    companion object {
        private const val SPLASH_TIME_OUT = 1000
    }
}
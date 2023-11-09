package com.kittyandpuppy.withallmyanimal

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import com.kittyandpuppy.withallmyanimal.LoginPage.LoginActivity
import com.kittyandpuppy.withallmyanimal.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent= Intent( this,LoginActivity::class.java)
            startActivity(intent)

            finish()

        }, 3000)
    }
}
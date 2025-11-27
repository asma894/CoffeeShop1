package com.example.coffeeshop.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Check authentication after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            if (auth.currentUser != null) {
                // User is logged in, go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // User not logged in, go to IntroActivity
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 1000) // 1 second splash
    }
}
package com.example.onlineexamapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.onlineexamapp.activities.AuthActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {

    private val splashDelay: Long = 1000 // 1 second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)

        // Enable edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)



        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT



        val logo = findViewById<ImageView>(R.id.logoImageView)
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_in)
        logo.startAnimation(scaleAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isUserLoggedIn()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
            }
            finish()
        }, splashDelay)
    }

    private fun isUserLoggedIn(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        return user != null
    }
}

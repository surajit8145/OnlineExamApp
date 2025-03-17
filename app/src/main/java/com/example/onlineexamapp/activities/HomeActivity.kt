package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
        val userRole = sharedPref.getString("userRole", null)

        if (auth.currentUser != null) {
            // User is logged in, show Dashboard, Profile, and Logout
            binding.btnDashboard.visibility = View.VISIBLE
            binding.btnProfile.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.GONE
            binding.btnRegister.visibility = View.GONE

            // Change dashboard text based on role
            binding.btnDashboard.text = if (userRole == "admin") "Admin Dashboard" else "Student Dashboard"

            binding.btnDashboard.setOnClickListener {
                val intent = if (userRole == "admin") {
                    Intent(this, AdminDashboardActivity::class.java)
                } else {
                    Intent(this, DashboardActivity::class.java)
                }
                startActivity(intent)
            }
            binding.btnProfile.setOnClickListener {
                startActivity(Intent(this, ViewProfileActivity::class.java))
            }

            binding.btnLogout.setOnClickListener {
                auth.signOut()
                sharedPref.edit().clear().apply()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }

        } else {
            // User is not logged in, show Login and Register buttons
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.VISIBLE
            binding.btnDashboard.visibility = View.GONE
            binding.btnProfile.visibility = View.GONE
            binding.btnLogout.visibility = View.GONE
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))


        }
    }
}

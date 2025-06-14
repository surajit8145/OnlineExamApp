package com.example.onlineexamapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.onlineexamapp.databinding.ActivityMainBinding
import com.example.onlineexamapp.fragments.*
import com.example.onlineexamapp.utils.AddWebDesigningQuestions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var backPressedOnce = false
    private val backPressHandler = Handler(Looper.getMainLooper())
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Upload questions only once (comment this out in production)
        AddWebDesigningQuestions.uploadQuestionsIfNeeded(this)

        // Initialize NavController from NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configure BottomNavigationView to work with NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // Hide admin menu item by default
        binding.bottomNavigation.menu.findItem(R.id.nav_admin).isVisible = false

        binding.bottomNavigation.menu.findItem(R.id.nav_results).isVisible = true

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role == "admin") {
                        binding.bottomNavigation.menu.findItem(R.id.nav_admin).isVisible = true
                        binding.bottomNavigation.menu.findItem(R.id.nav_results).isVisible = false
                    }
                }
        }

        // Handle bottom navigation selections (NavController will handle fragment navigation)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.nav_exams -> {
                    navController.navigate(R.id.examsFragment)
                    true
                }
                R.id.nav_results -> {
                    navController.navigate(R.id.resultsFragment)
                    true
                }
                R.id.nav_admin -> {
                    navController.navigate(R.id.AdminPanelFragment)
                    true
                }
                R.id.nav_profile -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && !user.isEmailVerified) {
            FirebaseAuth.getInstance().signOut()
        }
    }

    override fun onBackPressed() {
        if (navController.previousBackStackEntry != null) {
            // If there is a previous fragment, navigate back
            navController.navigateUp()
        } else {
            if (backPressedOnce) {
                super.onBackPressed()
                return
            }

            backPressedOnce = true
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

            backPressHandler.postDelayed({ backPressedOnce = false }, 2000)
        }
    }


}

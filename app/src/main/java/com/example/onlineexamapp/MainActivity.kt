package com.example.onlineexamapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.onlineexamapp.databinding.ActivityMainBinding  // Import for View Binding
import com.example.onlineexamapp.fragments.DashboardFragment
import com.example.onlineexamapp.fragments.HomeFragment
import com.example.onlineexamapp.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding  // Use View Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding:
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)  // Set the content view using the binding

        // Load the HomeFragment when the activity is created:
        loadFragment(HomeFragment())

        // Set up a listener for the bottom navigation view:
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment()) // Load HomeFragment
                    true // Indicate that the item click is handled
                }
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment()) // Load DashboardFragment
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment()) // Load ProfileFragment
                    true
                }
                else -> false //when (item.itemId)
            }
        }
    }

    // Function to load a fragment into the fragment container:
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Replace the fragment
            .commit() // Commit the transaction
    }
}

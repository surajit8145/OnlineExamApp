package com.example.onlineexamapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.onlineexamapp.databinding.ActivityMainBinding
import com.example.onlineexamapp.fragments.*
import com.example.onlineexamapp.utils.AddWebDesigningQuestions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Upload questions only once (you can comment this out in production)
        AddWebDesigningQuestions.uploadQuestionsIfNeeded(this)

        // Load HomeFragment by default
        loadFragment(HomeFragment())

        // Show Admin panel only if user is 'admin'
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role == "admin") {
                        binding.bottomNavigation.menu.findItem(R.id.nav_admin).isVisible = true
                    } else {
                        binding.bottomNavigation.menu.findItem(R.id.nav_admin).isVisible = false
                    }
                }
        }


        // Handle Bottom Navigation selections
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.nav_exams -> {
                    loadFragment(ExamsFragment())
                    true
                }
                R.id.nav_results -> {
                    loadFragment(ResultsFragment())
                    true
                }
                R.id.nav_admin -> {
                    loadFragment(AdminPanelFragment())
                    true
                }

                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
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

    private fun loadFragment(fragment: Fragment) {
        if (!isFinishing && !supportFragmentManager.isStateSaved) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss()
        }
    }
}

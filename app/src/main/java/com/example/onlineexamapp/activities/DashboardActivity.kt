package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userRole: String? = null  // Store user role globally

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser

        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Fetch user role from Firestore
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userRole = document.getString("role") ?: "student"  // Default to student

                    if (userRole == "admin") {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                        finish()
                    } else {
                        setupStudentDashboard(user.displayName ?: user.email ?: "Student")
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching role", Toast.LENGTH_SHORT).show()
                setupStudentDashboard(user.displayName ?: user.email ?: "Student")
            }
    }

    private fun setupStudentDashboard(username: String) {
        setContentView(R.layout.activity_dashboard)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val btnExamList = findViewById<Button>(R.id.btnExamList)
        val btnViewResults = findViewById<Button>(R.id.btnViewResults)
        val btnProfile = findViewById<Button>(R.id.btnProfile)

        tvWelcome.text = "Welcome, $username"



        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnExamList.setOnClickListener {
            startActivity(Intent(this, ExamListActivity::class.java))
        }

        btnViewResults.setOnClickListener {
            val intent = if (userRole == "admin") {
                Intent(this, AdminResultsActivity::class.java)
            } else {
                Intent(this, ViewResultsActivity::class.java)
            }
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ViewProfileActivity::class.java))
        }
    }
}

package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.MainActivity
import com.example.onlineexamapp.R
import com.example.onlineexamapp.fragments.ProfileFragment
import com.example.onlineexamapp.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etName: EditText
    private lateinit var tvEmail: TextView
    private lateinit var btnUpdate: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etName = findViewById(R.id.etName)
        tvEmail = findViewById(R.id.tvEmail)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnBack = findViewById(R.id.btnBack)

        loadUserProfile()

        btnUpdate.setOnClickListener {
            updateUserProfile()
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUserProfile() {
        val userId = FirebaseHelper.getUserId()
        if (userId == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etName.setText(document.getString("name") ?: "No Name")
                    tvEmail.text = document.getString("email") ?: "No Email"
                } else {
                    Toast.makeText(this, "User profile not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserProfile() {
        val userId = FirebaseHelper.getUserId()
        if (userId == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val newName = etName.text.toString().trim()
        if (newName.isEmpty()) {
            etName.error = "Name cannot be empty"
            return
        }

        db.collection("users").document(userId)
            .update("name", newName)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java)) // Redirect to View Profile
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
            }
    }

}

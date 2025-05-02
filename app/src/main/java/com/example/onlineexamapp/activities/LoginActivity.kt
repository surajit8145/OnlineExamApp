package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.MainActivity
import com.example.onlineexamapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Disable button and show progress
                binding.btnLogin.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val userId = authResult.user?.uid
                        if (userId != null) {
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    binding.progressBar.visibility = View.GONE
                                    binding.btnLogin.isEnabled = true

                                    if (document.exists()) {
                                        val role = document.getString("role")
                                        val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
                                        with(sharedPref.edit()) {
                                            putString("userRole", role)
                                            apply()
                                        }
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    binding.progressBar.visibility = View.GONE
                                    binding.btnLogin.isEnabled = true
                                    Toast.makeText(this, "Failed to fetch user role!", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(this, "Login Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }
    }
}

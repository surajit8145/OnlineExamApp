package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val role = if (binding.radioAdmin.isChecked) "admin" else "student"

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    saveUserToFirestore(user, name, phone, email, role)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Registration Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser, name: String, phone: String, email: String, role: String) {
        val userId = "user-" + email.replace(".", "_") // Convert email to a safe Firestore document ID
        val userData = hashMapOf(
            "id" to userId,
            "name" to name,
            "phone" to phone,
            "email" to email,
            "role" to role,
            "created_at" to System.currentTimeMillis()
        )

        db.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                saveUserRoleInPreferences(role)
                redirectToHomePage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save user data!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserRoleInPreferences(role: String) {
        val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("userRole", role)
            apply()
        }
    }

    private fun redirectToHomePage() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }


}

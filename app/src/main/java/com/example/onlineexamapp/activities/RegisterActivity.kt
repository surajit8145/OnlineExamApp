package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.MainActivity
import com.example.onlineexamapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val ADMIN_CODE = "02122003"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 👇 Add this to toggle the Admin Code field based on selection
        binding.radioAdmin.setOnCheckedChangeListener { _, isChecked ->
            binding.adminCodeLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

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

        if (role == "admin") {
            val adminCode = binding.etAdminCode.text.toString().trim()
            if (adminCode != ADMIN_CODE) {
                Toast.makeText(this, "Invalid admin registration code!", Toast.LENGTH_SHORT).show()
                return
            }
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
        val userId = "user-" + email.replace(".", "_") // safe Firestore ID
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
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

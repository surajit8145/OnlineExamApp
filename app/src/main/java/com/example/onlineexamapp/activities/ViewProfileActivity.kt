package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.databinding.ActivityViewProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserProfile()

        binding.btnUpdateProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.tvName.text = document.getString("name")
                        binding.tvEmail.text = document.getString("email")
                        binding.tvPhone.text = document.getString("phone")
                        binding.tvRole.text = document.getString("role")?.capitalize()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load profile!", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

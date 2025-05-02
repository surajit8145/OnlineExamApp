package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnUpdatePassword: Button
    private lateinit var progressBar: ProgressBar


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)
        progressBar = findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()

        btnUpdatePassword.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            val email = user?.email

            if (user != null && email != null) {
                progressBar.visibility = View.VISIBLE
                btnUpdatePassword.isEnabled = false

                val credential = EmailAuthProvider.getCredential(email, currentPassword)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updatePassword(newPassword)
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, ProfileActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                btnUpdatePassword.isEnabled = true
                                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        btnUpdatePassword.isEnabled = true
                        Toast.makeText(this, "Re-authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}

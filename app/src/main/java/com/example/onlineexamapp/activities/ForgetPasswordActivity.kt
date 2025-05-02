package com.example.onlineexamapp.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        etEmail = findViewById(R.id.etEmail)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        auth = FirebaseAuth.getInstance()

        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ForgetPasswordActivity,
                            "Email sent successfully to reset your password!",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@ForgetPasswordActivity,
                            task.exception?.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}

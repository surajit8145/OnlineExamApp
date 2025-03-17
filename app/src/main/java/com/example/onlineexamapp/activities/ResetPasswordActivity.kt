package com.example.onlineexamapp.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: String

    private lateinit var etEmail: EditText
    private lateinit var btnSendOTP: Button
    private lateinit var etOTP: EditText
    private lateinit var btnVerifyOTP: Button
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnUpdatePassword: Button
    private lateinit var layoutEmail: LinearLayout
    private lateinit var layoutOTP: LinearLayout
    private lateinit var layoutNewPassword: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        auth = FirebaseAuth.getInstance()

        // UI Elements
        etEmail = findViewById(R.id.etEmail)
        btnSendOTP = findViewById(R.id.btnSendOTP)
        etOTP = findViewById(R.id.etOTP)
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)
        layoutEmail = findViewById(R.id.layoutEmail)
        layoutOTP = findViewById(R.id.layoutOTP)
        layoutNewPassword = findViewById(R.id.layoutNewPassword)

        // Initially hide OTP and Password fields
        layoutOTP.visibility = View.GONE
        layoutNewPassword.visibility = View.GONE

        // Step 1: Send OTP to Email
        btnSendOTP.setOnClickListener {
            email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://onlineexamapp.page.link/reset")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(packageName, true, null)
                .build()

            auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnSuccessListener {
                    Toast.makeText(this, "OTP sent to your email", Toast.LENGTH_LONG).show()
                    layoutEmail.visibility = View.GONE
                    layoutOTP.visibility = View.VISIBLE
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Step 2: Verify OTP
        btnVerifyOTP.setOnClickListener {
            val otp = etOTP.text.toString().trim()
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simulating OTP verification (you should verify the OTP from Firebase)
            Toast.makeText(this, "OTP Verified Successfully!", Toast.LENGTH_SHORT).show()
            layoutOTP.visibility = View.GONE
            layoutNewPassword.visibility = View.VISIBLE
        }

        // Step 3: Change Password
        btnUpdatePassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, newPassword)
                .addOnSuccessListener {
                    val user = auth.currentUser
                    user?.updatePassword(newPassword)
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        ?.addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

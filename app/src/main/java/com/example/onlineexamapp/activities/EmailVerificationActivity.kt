package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.MainActivity
import com.example.onlineexamapp.databinding.ActivityEmailVerificationBinding
import com.google.firebase.auth.FirebaseAuth

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailVerificationBinding
    private val auth = FirebaseAuth.getInstance()
    private lateinit var autoCheckHandler: Handler
    private lateinit var autoCheckRunnable: Runnable
    private var countDownTimer: CountDownTimer? = null
    private val autoCheckInterval: Long = 5000 // 5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("email")
        if (email.isNullOrBlank()) {
            binding.tvEmailInfo.text = "Email not provided. Please try again."
        } else {
            binding.tvEmailInfo.text = "We've sent a verification link to $email.\nPlease verify it before continuing."
        }



        binding.btnCheckVerified.setOnClickListener {
            checkIfVerified()
        }

        binding.btnResend.setOnClickListener {
            resendVerificationEmail()
        }

        startAutoCheckVerification()
    }

    private fun checkIfVerified() {
        val user = auth.currentUser ?: return
        binding.progressBar.visibility = View.VISIBLE

        user.reload().addOnSuccessListener {
            binding.progressBar.visibility = View.GONE

            if (user.isEmailVerified) {
                proceedAfterVerification()
            } else {
                Toast.makeText(this, "Email not verified yet.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resendVerificationEmail() {
        val user = auth.currentUser ?: return
        binding.progressBar.visibility = View.VISIBLE

        // Disable button & start timer
        binding.btnResend.isEnabled = false
        startCountdownTimer()

        user.sendEmailVerification().addOnSuccessListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Verification email resent.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Failed to resend: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCountdownTimer() {
        binding.tvResendMessage.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvResendMessage.text = "Please wait $seconds seconds before requesting again."
            }

            override fun onFinish() {
                binding.btnResend.isEnabled = true
                binding.tvResendMessage.text = ""
            }
        }.start()
    }

    private fun startAutoCheckVerification() {
        autoCheckHandler = Handler(Looper.getMainLooper())
        autoCheckRunnable = object : Runnable {
            override fun run() {
                auth.currentUser?.reload()?.addOnSuccessListener {
                    if (auth.currentUser?.isEmailVerified == true) {
                        proceedAfterVerification()
                    } else {
                        autoCheckHandler.postDelayed(this, autoCheckInterval)
                    }
                }
            }
        }
        autoCheckHandler.postDelayed(autoCheckRunnable, autoCheckInterval)
    }

    private fun proceedAfterVerification() {
        Toast.makeText(this, "Email verified!", Toast.LENGTH_SHORT).show()
        stopAutoCheck()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun stopAutoCheck() {
        autoCheckHandler.removeCallbacks(autoCheckRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        stopAutoCheck()
    }

    override fun onBackPressed() {
        if (!auth.currentUser?.isEmailVerified!!) {
            showExitDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun showExitDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Are you sure?")
            .setMessage("Your email is not verified yet. If you leave now, you may need to verify your email again when you log in.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                auth.signOut()
                val intent = Intent(this, AuthActivity::class.java)
                intent.putExtra("startFragment", "SignInFragment")
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
}

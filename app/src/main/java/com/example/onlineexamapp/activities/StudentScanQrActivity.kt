package com.example.onlineexamapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.databinding.ActivityScanQrBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import java.util.*

class

StudentScanQrActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanQrBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScan.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setPrompt("Scan QR Code")
            integrator.setBeepEnabled(true)
            integrator.setOrientationLocked(false)
            integrator.initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            handleQrData(result.contents)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleQrData(qrData: String) {
        val parts = qrData.split("|")
        if (parts.size < 5) {
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show()
            return
        }

        val semester = parts[0]
        val department = parts[1]
        val subject = parts[2]
        val code = parts[3]

        val sessionDocId = "$semester-$department-$subject"
        val user = auth.currentUser ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val studentRef = db.collection("attendance_sessions")
            .document(sessionDocId)
            .collection("students")
            .document(user.uid + "_" + today) // unique per day

        studentRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                Toast.makeText(this, "Already marked for today!", Toast.LENGTH_LONG).show()
            } else {
                val data = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "date" to today,
                    "marked_at" to System.currentTimeMillis()
                )
                studentRef.set(data).addOnSuccessListener {
                    Toast.makeText(this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to mark attendance!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

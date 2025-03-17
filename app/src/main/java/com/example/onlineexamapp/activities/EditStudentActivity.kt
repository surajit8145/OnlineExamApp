package com.example.onlineexamapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.databinding.ActivityEditStudentBinding
import com.google.firebase.firestore.FirebaseFirestore

class EditStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditStudentBinding
    private val db = FirebaseFirestore.getInstance()
    private var studentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get student details from intent
        studentId = intent.getStringExtra("studentId")
        binding.etName.setText(intent.getStringExtra("studentName"))
        binding.etEmail.setText(intent.getStringExtra("studentEmail"))
        binding.etPhone.setText(intent.getStringExtra("studentPhone"))

        // Save Changes
        binding.btnSave.setOnClickListener {
            updateStudentDetails()
        }
    }

    private fun updateStudentDetails() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        val studentData = mapOf(
            "name" to name,
            "phone" to phone
        )

        studentId?.let {
            db.collection("users").document(it)
                .update(studentData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update student", Toast.LENGTH_SHORT).show()
                }
        }
    }

}

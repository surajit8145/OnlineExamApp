package com.example.onlineexamapp.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.example.onlineexamapp.databinding.ActivityCreateSubjectBinding
import com.google.firebase.firestore.FirebaseFirestore

class CreateSubjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateSubjectBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Semester Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.semesters,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSemester.adapter = adapter
        }

        // Setup Department Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.departments,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDepartment.adapter = adapter
        }

        // Save button
        binding.btnSaveSubject.setOnClickListener {
            saveSubject()
        }
    }

    private fun saveSubject() {
        val semester = binding.spinnerSemester.selectedItem.toString()
        val department = binding.spinnerDepartment.selectedItem.toString()
        val subjectName = binding.etSubjectName.text.toString().trim()

        if (subjectName.isEmpty()) {
            binding.etSubjectName.error = "Enter subject name"
            return
        }

        val subjectData = hashMapOf(
            "semester" to semester,
            "department" to department,
            "subject_name" to subjectName,
            "created_at" to System.currentTimeMillis()
        )

        db.collection("subjects")
            .add(subjectData)
            .addOnSuccessListener {
                Toast.makeText(this, "Subject added successfully", Toast.LENGTH_SHORT).show()
                binding.etSubjectName.text?.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add subject", Toast.LENGTH_SHORT).show()
            }
    }
}

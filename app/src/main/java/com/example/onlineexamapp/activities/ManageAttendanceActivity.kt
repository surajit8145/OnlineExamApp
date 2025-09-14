package com.example.onlineexamapp.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineexamapp.R
import com.example.onlineexamapp.databinding.ActivityManageAttendanceBinding
import com.example.onlineexamapp.models.StudentAttendance
import com.example.onlineexamapp.adapters.AttendanceAdapter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ManageAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageAttendanceBinding
    private val db = FirebaseFirestore.getInstance()
    private val studentList = mutableListOf<StudentAttendance>()
    private lateinit var adapter: AttendanceAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var selectedSemester = ""
    private var selectedDepartment = ""
    private var selectedSubject = ""
    private var selectedDate = dateFormat.format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupRecyclerView()

        // Date Picker
        binding.tvDate.text = selectedDate
        binding.tvDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.tvDate.text = selectedDate
                loadAttendance()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // FAB → add student
        binding.fabAddStudent.setOnClickListener {
            showAddStudentDialog()
        }
    }

    private fun setupSpinners() {
        ArrayAdapter.createFromResource(
            this, R.array.semesters, android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSemester.adapter = it
        }

        ArrayAdapter.createFromResource(
            this, R.array.departments, android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDepartment.adapter = it
        }

        binding.spinnerSemester.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: android.view.View?, pos: Int, id: Long) {
                selectedSemester = binding.spinnerSemester.selectedItem.toString()
                loadSubjects()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.spinnerDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: android.view.View?, pos: Int, id: Long) {
                selectedDepartment = binding.spinnerDepartment.selectedItem.toString()
                loadSubjects()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun loadSubjects() {
        if (selectedSemester.isEmpty() || selectedDepartment.isEmpty()) return

        db.collection("subjects")
            .whereEqualTo("semester", selectedSemester)
            .whereEqualTo("department", selectedDepartment)
            .get()
            .addOnSuccessListener { result ->
                val subjects = result.mapNotNull { it.getString("subject_name") }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerSubject.adapter = adapter

                binding.spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: android.view.View?, pos: Int, id: Long) {
                        selectedSubject = binding.spinnerSubject.selectedItem.toString()
                        loadAttendance()
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }
            }
    }

    private fun setupRecyclerView() {
        adapter = AttendanceAdapter(studentList) { student ->
            deleteStudent(student)
        }
        binding.recyclerViewAttendance.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAttendance.adapter = adapter
    }

    private fun loadAttendance() {
        if (selectedSemester.isEmpty() || selectedDepartment.isEmpty() || selectedSubject.isEmpty()) return

        val sessionId = "$selectedSemester-$selectedDepartment-$selectedSubject"
        db.collection("attendance_sessions")
            .document(sessionId)
            .collection("students")
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { result ->
                studentList.clear()
                for (doc in result) {
                    val student = StudentAttendance(
                        uid = doc.getString("uid") ?: "",
                        email = doc.getString("email") ?: "",
                        date = doc.getString("date") ?: "",
                        docId = doc.id
                    )
                    studentList.add(student)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun deleteStudent(student: StudentAttendance) {
        val sessionId = "$selectedSemester-$selectedDepartment-$selectedSubject"
        db.collection("attendance_sessions")
            .document(sessionId)
            .collection("students")
            .document(student.docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Student removed", Toast.LENGTH_SHORT).show()
                loadAttendance()
            }
    }

    private fun showAddStudentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_student, null)
        val etEmail = dialogView.findViewById<EditText>(R.id.etStudentEmail)

        AlertDialog.Builder(this)
            .setTitle("Add Student")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val email = etEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    addStudent(email)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addStudent(email: String) {
        val sessionId = "$selectedSemester-$selectedDepartment-$selectedSubject"
        val dateKey = selectedDate
        val docId = "${email}_${dateKey}"

        val data = hashMapOf(
            "email" to email,
            "date" to selectedDate,
            "uid" to email, // or map actual uid if available
            "marked_at" to System.currentTimeMillis()
        )

        db.collection("attendance_sessions")
            .document(sessionId)
            .collection("students")
            .document(docId)
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show()
                loadAttendance()
            }
    }
}

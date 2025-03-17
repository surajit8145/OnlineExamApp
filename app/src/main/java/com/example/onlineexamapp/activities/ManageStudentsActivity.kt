package com.example.onlineexamapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineexamapp.adapters.StudentAdapter
import com.example.onlineexamapp.databinding.ActivityManageStudentsBinding
import com.example.onlineexamapp.models.Student
import com.google.firebase.firestore.FirebaseFirestore

class ManageStudentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageStudentsBinding
    private lateinit var studentAdapter: StudentAdapter
    private val studentList = mutableListOf<Student>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentAdapter(studentList) { student ->
            deleteStudent(student)
        }
        binding.rvStudents.adapter = studentAdapter

        // Load students
        loadStudents()
    }

    private fun loadStudents() {
        db.collection("users").whereEqualTo("role", "student").get()
            .addOnSuccessListener { documents ->
                studentList.clear()
                for (document in documents) {
                    val id = document.id // Use Firestore document ID
                    val name = document.getString("name") ?: ""
                    val email = document.getString("email") ?: ""
                    val phone = document.getString("phone") ?: ""
                    studentList.add(Student(id, name, email, phone))
                }
                studentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ManageStudentsActivity", "Error loading students", e)
                Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteStudent(student: Student) {
        db.collection("users").document(student.id).delete()
            .addOnSuccessListener {
                studentList.remove(student)
                studentAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Student removed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ManageStudentsActivity", "Error deleting student", e)
                Toast.makeText(this, "Error deleting student", Toast.LENGTH_SHORT).show()
            }
    }
}

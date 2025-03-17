package com.example.onlineexamapp.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.onlineexamapp.R
import com.google.firebase.firestore.FirebaseFirestore

class CreateExamActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etSubject: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText   // ✅ Added Exam Time
    private lateinit var etDuration: EditText
    private lateinit var btnCreateExam: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_exam)

        etTitle = findViewById(R.id.etTitle)
        etSubject = findViewById(R.id.etSubject)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)   // ✅ Initialize Exam Time
        etDuration = findViewById(R.id.etDuration)
        btnCreateExam = findViewById(R.id.btnCreateExam)

        auth = FirebaseAuth.getInstance()

        etDate.setOnClickListener { showDatePicker() }
        etTime.setOnClickListener { showTimePicker() }  // ✅ Add Time Picker

        btnCreateExam.setOnClickListener { createExam() }
    }

    // ✅ Show Date Picker Dialog
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            etDate.setText(dateFormat.format(selectedDate.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.show()
    }

    // ✅ Show Time Picker Dialog
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            val selectedTime = Calendar.getInstance()
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTime.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format with AM/PM
            etTime.setText(timeFormat.format(selectedTime.time))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false) // `false` for 12-hour format

        timePickerDialog.show()
    }

    private fun createExam() {
        val title = etTitle.text.toString().trim()
        val subject = etSubject.text.toString().trim()
        val date = etDate.text.toString().trim()
        val time = etTime.text.toString().trim()  // ✅ Get selected time
        val duration = etDuration.text.toString().trim()

        if (title.isEmpty() || subject.isEmpty() || date.isEmpty() || time.isEmpty() || duration.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val examId = "exam" + System.currentTimeMillis()
        val createdBy = auth.currentUser?.email ?: "unknown@example.com"

        val exam = hashMapOf(
            "id" to examId,
            "title" to title,
            "subject" to subject,
            "date" to date,
            "time" to time,   // ✅ Store Time in Firestore
            "duration" to duration.toInt(),
            "createdBy" to createdBy
        )

        val db = FirebaseFirestore.getInstance()

        db.collection("exams").document(examId)
            .set(exam)
            .addOnSuccessListener {
                Toast.makeText(this, "Exam Created Successfully", Toast.LENGTH_SHORT).show()

                // ✅ Navigate to Add Questions Page
                val intent = Intent(this, AddQuestionActivity::class.java)
                intent.putExtra("EXAM_ID", examId)  // Pass the exam ID
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

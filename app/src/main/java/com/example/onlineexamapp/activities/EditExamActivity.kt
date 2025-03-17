package com.example.onlineexamapp.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditExamActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var etExamTitle: EditText
    private lateinit var etExamSubject: EditText
    private lateinit var etExamDate: EditText
    private lateinit var etExamTime: EditText  // New Field
    private lateinit var etExamDuration: EditText
    private lateinit var btnUpdateExam: Button
    private lateinit var btnDeleteExam: Button

    private var examId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_exam)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        etExamTitle = findViewById(R.id.etExamTitle)
        etExamSubject = findViewById(R.id.etExamSubject)
        etExamDate = findViewById(R.id.etExamDate)
        etExamTime = findViewById(R.id.etExamTime)  // Initialize Time Field
        etExamDuration = findViewById(R.id.etExamDuration)
        btnUpdateExam = findViewById(R.id.btnUpdateExam)
        btnDeleteExam = findViewById(R.id.btnDeleteExam)

        examId = intent.getStringExtra("examId")

        if (!examId.isNullOrEmpty()) {
            loadExamData(examId!!)
        } else {
            Toast.makeText(this, "Invalid Exam ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        etExamDate.setOnClickListener { showDatePicker() }
        etExamTime.setOnClickListener { showTimePicker() }  // Set Click Listener

        btnUpdateExam.setOnClickListener { updateExam() }
        btnDeleteExam.setOnClickListener { deleteExam() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this, { _, year, month, dayOfMonth ->
                etExamDate.setText("$year-${month + 1}-$dayOfMonth")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(this, { _, hour, minute ->
            val isAM = hour < 12
            val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, if (isAM) "AM" else "PM")
            etExamTime.setText(formattedTime)
        }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), false) // false for 12-hour format

        timePickerDialog.show()
    }

    private fun loadExamData(id: String) {
        db.collection("exams").document(id).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etExamTitle.setText(document.getString("title") ?: "")
                    etExamSubject.setText(document.getString("subject") ?: "")
                    etExamDate.setText(document.getString("date") ?: "")
                    etExamTime.setText(document.getString("time") ?: "")  // Load Time
                    etExamDuration.setText(document.getLong("duration")?.toString() ?: "")
                } else {
                    Toast.makeText(this, "Exam not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    // Function to update the exam details
    private fun updateExam() {
        val title = etExamTitle.text.toString().trim()
        val subject = etExamSubject.text.toString().trim()
        val date = etExamDate.text.toString().trim()
        val time = etExamTime.text.toString().trim()
        val duration = etExamDuration.text.toString().trim()

        if (title.isEmpty() || subject.isEmpty() || date.isEmpty() || time.isEmpty() || duration.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the existing exam document
        db.collection("exams").document(examId!!).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Preserve createdBy and id while updating other fields
                    val createdBy = document.getString("createdBy") ?: ""  // Get the createdBy from the existing document
                    val updatedExam: Map<String, Any> = hashMapOf(
                        "title" to title,
                        "subject" to subject,
                        "date" to date,
                        "time" to time,
                        "duration" to duration.toLong(),
                        "createdBy" to createdBy   // Preserve createdBy field
                    )

                    // Use update() to ensure no fields are removed
                    db.collection("exams").document(examId!!).update(updatedExam)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Exam updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error updating exam: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Exam not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching exam: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to delete the exam
    private fun deleteExam() {
        db.collection("exams").document(examId!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Exam deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting exam: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

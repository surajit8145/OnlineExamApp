package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val btnUpdateProfile = findViewById<Button>(R.id.btnUpdateProfile)
        val btnManageStudents = findViewById<Button>(R.id.btnManageStudents)
        val btnCreateExam = findViewById<Button>(R.id.btnCreateExam)
        val btnManageExams = findViewById<Button>(R.id.btnManageExams)
        val btnAddQuestions = findViewById<Button>(R.id.btnAddQuestions)
        val btnEditQuestions = findViewById<Button>(R.id.btnEditQuestions) // Added Edit Question button
        val btnViewResults = findViewById<Button>(R.id.btnViewResults)
        val btnHome = findViewById<Button>(R.id.btnHome)

        btnUpdateProfile.setOnClickListener {
            startActivity(Intent(this, ViewProfileActivity::class.java))
        }

        btnManageStudents.setOnClickListener {
            startActivity(Intent(this, ManageStudentsActivity::class.java))
        }

        btnCreateExam.setOnClickListener {
            startActivity(Intent(this, CreateExamActivity::class.java))
        }

        btnManageExams.setOnClickListener {
            startActivity(Intent(this, ManageExamsActivity::class.java))
        }

        btnAddQuestions.setOnClickListener {
            startActivity(Intent(this, AddQuestionActivity::class.java))
        }

        btnEditQuestions.setOnClickListener {
            startActivity(Intent(this, EditQuestionActivity::class.java)) // Added Intent for Edit Questions
        }

        btnViewResults.setOnClickListener {
            startActivity(Intent(this, AdminViewResultsActivity::class.java))
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}

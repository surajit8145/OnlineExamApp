package com.example.onlineexamapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.R
import com.example.onlineexamapp.adapters.ExamAdapter
import com.example.onlineexamapp.models.Exam
import com.example.onlineexamapp.activities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rootView: View
    private var userRole: String? = null

    private lateinit var rvUpcomingExams: RecyclerView
    private lateinit var rvPastExams: RecyclerView
    private lateinit var tvTotalExams: TextView
    private lateinit var tvUpcomingExams: TextView
    private lateinit var tvPastExams: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupUI()
        fetchExams()
        return rootView
    }

    private fun setupUI() {
        val user = auth.currentUser
        rootView.findViewById<TextView>(R.id.tvWelcome).text =
            "Welcome  ${user?.displayName ?: user?.email ?: " Guest"}"

        rvUpcomingExams = rootView.findViewById(R.id.rvUpcomingExams)
        rvPastExams = rootView.findViewById(R.id.rvPastExams)
        tvTotalExams = rootView.findViewById(R.id.tvTotalExams)
        tvUpcomingExams = rootView.findViewById(R.id.tvPendingExams)
        tvPastExams = rootView.findViewById(R.id.tvCompletedExams)

        rvUpcomingExams.layoutManager = LinearLayoutManager(requireContext())
        rvPastExams.layoutManager = LinearLayoutManager(requireContext())

        toggleButtons(false, R.id.btnLogin, R.id.btnRegister, R.id.btnExamList, R.id.btnViewResults,
            R.id.btnManageStudents, R.id.btnCreateExam, R.id.btnManageExams, R.id.btnAdminViewResults,
            R.id.btnAddQuestion, R.id.btnEditQuestion) // Initially hide all buttons

        if (user == null) {
            toggleButtons(true, R.id.btnLogin, R.id.btnRegister) // Guest mode
        } else {
            userRole = requireContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE)
                .getString("userRole", null)

            userRole?.let { updateUI() } ?: fetchUserRole(user.uid)
        }

        setButtonClickListeners()
    }

    private fun fetchUserRole(userId: String) {
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            userRole = document.getString("role") ?: "student"
            requireContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE).edit()
                .putString("userRole", userRole).apply()
            updateUI()
        }.addOnFailureListener {
            Toast.makeText(activity, "Error fetching role, defaulting to student.", Toast.LENGTH_SHORT).show()
            userRole = "student"
            updateUI()
        }
    }

    private fun updateUI() {

        when (userRole) {
            "admin" -> toggleButtons(true, R.id.btnManageStudents, R.id.btnCreateExam, R.id.btnManageExams,
                R.id.btnAdminViewResults, R.id.btnAddQuestion, R.id.btnEditQuestion) // Admin can add/edit questions
            "student" -> toggleButtons(true, R.id.btnExamList, R.id.btnViewResults)
        }
    }

    private fun toggleButtons(visible: Boolean, vararg buttonIds: Int) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        buttonIds.forEach { rootView.findViewById<Button>(it).visibility = visibility }
    }

    private fun fetchExams() {
        db.collection("exams").get().addOnSuccessListener { documents ->
            val exams = documents.map { it.toObject(Exam::class.java) }
            val today = System.currentTimeMillis()

            val (upcoming, past) = exams.partition { isUpcoming(it.date, today) }

            tvTotalExams.text = exams.size.toString()
            tvUpcomingExams.text = upcoming.size.toString()
            tvPastExams.text = past.size.toString()

            rvUpcomingExams.adapter = ExamAdapter(upcoming) {}
            rvPastExams.adapter = ExamAdapter(past) {}
        }.addOnFailureListener {
            Toast.makeText(activity, "Failed to fetch exams", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isUpcoming(examDate: String, today: Long): Boolean {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(examDate)?.time ?: today > today
    }

    private fun setButtonClickListeners() {
        mapOf(
            R.id.btnLogin to LoginActivity::class.java,
            R.id.btnRegister to RegisterActivity::class.java,
            R.id.btnExamList to ExamListActivity::class.java,
            R.id.btnViewResults to ViewResultsActivity::class.java,
            R.id.btnManageStudents to ManageStudentsActivity::class.java,
            R.id.btnCreateExam to CreateExamActivity::class.java,
            R.id.btnManageExams to ManageExamsActivity::class.java,
            R.id.btnAdminViewResults to AdminViewResultsActivity::class.java,
            R.id.btnAddQuestion to AddQuestionActivity::class.java, // Added Add Question
            R.id.btnEditQuestion to EditQuestionActivity::class.java // Added Edit Question
        ).forEach { (id, activity) ->
            rootView.findViewById<Button>(id)?.setOnClickListener {
                startActivity(Intent(requireContext(), activity))
            }
        }
    }
}
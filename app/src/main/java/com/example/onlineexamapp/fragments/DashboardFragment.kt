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
    private lateinit var progressBar: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupUI()

        val user = auth.currentUser
        if (user != null) {
            fetchExams()
        }

        return rootView
    }

    private fun setupUI() {
        val user = auth.currentUser

        val welcomeText = "Welcome ${user?.displayName ?: user?.email ?: "Guest"}"
        rootView.findViewById<TextView>(R.id.tvWelcome)?.text = welcomeText

        rvUpcomingExams = rootView.findViewById(R.id.rvUpcomingExams)
        rvPastExams = rootView.findViewById(R.id.rvPastExams)
        tvTotalExams = rootView.findViewById(R.id.tvTotalExams)
        tvUpcomingExams = rootView.findViewById(R.id.tvPendingExams)
        tvPastExams = rootView.findViewById(R.id.tvCompletedExams)

        rvUpcomingExams.layoutManager = LinearLayoutManager(requireContext())
        rvPastExams.layoutManager = LinearLayoutManager(requireContext())
        progressBar = rootView.findViewById(R.id.progressBar)

        // ✅ Always hide everything first
        toggleButtons(false, R.id.btnLogin, R.id.btnRegister, R.id.btnExamList, R.id.btnViewResults,
            R.id.btnManageStudents, R.id.btnCreateExam, R.id.btnManageExams, R.id.btnAdminViewResults,
            R.id.btnAddQuestion, R.id.btnEditQuestion)

        if (user == null) {
            toggleButtons(true, R.id.btnLogin, R.id.btnRegister)
        } else {
            // Force refresh role from Firestore every time (avoid stale SharedPrefs)
            fetchUserRole(user.uid)
        }

        setButtonClickListeners()
    }

    private fun fetchUserRole(userId: String) {
        showLoading()
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            userRole = document.getString("role") ?: "student"

            updateUI()
        }.addOnFailureListener {
            Toast.makeText(activity, "Error fetching role, defaulting to student.", Toast.LENGTH_SHORT).show()
            userRole = "student"
            updateUI()
        }.addOnCompleteListener {
            hideLoading()
        }
    }

    private fun updateUI() {
        // Hide all first
        toggleButtons(false, R.id.btnLogin, R.id.btnRegister, R.id.btnExamList, R.id.btnViewResults,
            R.id.btnManageStudents, R.id.btnCreateExam, R.id.btnManageExams, R.id.btnAdminViewResults,
            R.id.btnAddQuestion, R.id.btnEditQuestion)

        when (userRole) {
            "admin" -> toggleButtons(true, R.id.btnManageStudents, R.id.btnCreateExam, R.id.btnManageExams,
                R.id.btnAdminViewResults, R.id.btnAddQuestion, R.id.btnEditQuestion)
            "student" -> toggleButtons(true, R.id.btnExamList, R.id.btnViewResults)
        }

    }


    private fun toggleButtons(visible: Boolean, vararg buttonIds: Int) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        buttonIds.forEach { rootView.findViewById<Button>(it)?.visibility = visibility }
    }

    private fun fetchExams() {
        db.collection("exams").get().addOnSuccessListener { documents ->
            val allExams = documents.map { it.toObject(Exam::class.java) }

            // Filter exams that have valid date/time and subject not "quiz"
            val exams = allExams.filter {
                !it.date.isNullOrBlank() &&
                        !it.time.isNullOrBlank() &&
                        !it.subject.equals("quiz", ignoreCase = true)
            }

            val currentTimeMillis = System.currentTimeMillis()

            val (upcoming, past) = exams.partition { isUpcoming(it.date, it.time, currentTimeMillis) }

            tvTotalExams.text = exams.size.toString()
            tvUpcomingExams.text = upcoming.size.toString()
            tvPastExams.text = past.size.toString()

            rvUpcomingExams.adapter = ExamAdapter(upcoming) {}
            rvPastExams.adapter = ExamAdapter(past) {}
        }.addOnFailureListener {
            Toast.makeText(activity, "Failed to fetch exams", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isUpcoming(date: String?, time: String?, currentTimeMillis: Long): Boolean {
        if (date.isNullOrBlank() || time.isNullOrBlank()) return false

        val dateTimeString = "$date $time"
        val formatter = SimpleDateFormat("yyyy-M-d hh:mm a", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()

        return try {
            val examTimeMillis = formatter.parse(dateTimeString)?.time
            examTimeMillis != null && examTimeMillis > currentTimeMillis
        } catch (e: Exception) {
            false
        }
    }

    private fun setButtonClickListeners() {
        mapOf(
            R.id.btnLogin to AuthActivity::class.java,
            R.id.btnRegister to AuthActivity::class.java,
            R.id.btnExamList to ExamListActivity::class.java,
            R.id.btnViewResults to ViewResultsActivity::class.java,
            R.id.btnManageStudents to ManageStudentsActivity::class.java,
            R.id.btnCreateExam to CreateExamActivity::class.java,
            R.id.btnManageExams to ManageExamsActivity::class.java,
            R.id.btnAdminViewResults to AdminViewResultsActivity::class.java,
            R.id.btnAddQuestion to AddQuestionActivity::class.java,
            R.id.btnEditQuestion to EditQuestionActivity::class.java
        ).forEach { (id, activity) ->
            rootView.findViewById<Button>(id)?.setOnClickListener {
                startActivity(Intent(requireContext(), activity))
            }
        }
    }
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        rootView.findViewById<ViewGroup>(R.id.dashboardMainContainer)?.visibility = View.GONE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
        rootView.findViewById<ViewGroup>(R.id.dashboardMainContainer)?.visibility = View.VISIBLE
    }

}

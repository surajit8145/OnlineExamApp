package com.example.onlineexamapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout // Import SwipeRefreshLayout
import com.example.onlineexamapp.activities.TakeExamActivity
import com.example.onlineexamapp.adapters.ExamAdapter
import com.example.onlineexamapp.databinding.FragmentExamsBinding
import com.example.onlineexamapp.models.Exam
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ExamsFragment : Fragment() {

    private var _binding: FragmentExamsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private val auth = FirebaseAuth.getInstance()

    private lateinit var upcomingAdapter: ExamAdapter
    private lateinit var runningAdapter: ExamAdapter
    private lateinit var attendedAdapter: ExamAdapter
    private lateinit var pastAdapter: ExamAdapter

    private val upcomingList = mutableListOf<Exam>()
    private val runningList = mutableListOf<Exam>()
    private val attendedList = mutableListOf<Exam>()
    private val pastList = mutableListOf<Exam>()

    // Reference to the SwipeRefreshLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        // Get reference to SwipeRefreshLayout from the binding
        swipeRefreshLayout = binding.swipeRefreshLayout

        setupRecyclerViews()
        fetchExams() // Initial data fetch

        // Set up the SwipeRefreshListener
        swipeRefreshLayout.setOnRefreshListener {
            fetchExams() // Fetch data when the user swipes down
        }
    }

    private fun setupRecyclerViews() {
        binding.rvUpcomingExams.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRunningExams.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAttendedExams.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPastExams.layoutManager = LinearLayoutManager(requireContext())

        upcomingAdapter = ExamAdapter(upcomingList) { }
        runningAdapter = ExamAdapter(runningList) { selectedExam ->
            showExamInstructionDialog(selectedExam)
        }
        attendedAdapter = ExamAdapter(attendedList) { }
        pastAdapter = ExamAdapter(pastList) { }

        binding.rvUpcomingExams.adapter = upcomingAdapter
        binding.rvRunningExams.adapter = runningAdapter
        binding.rvAttendedExams.adapter = attendedAdapter
        binding.rvPastExams.adapter = pastAdapter
    }

    private fun showExamInstructionDialog(exam: Exam) {
        val message = """
            📝 Exam: ${exam.title}
            📚 Subject: ${exam.subject}
            📅 Date: ${exam.date}
            ⏰ Time: ${exam.time}
            ⏳ Duration: ${exam.duration} minutes

            Please read all questions carefully.
            Once you start, you can't go back.
            Good luck!
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Exam Instructions")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Start") { dialog, _ ->
                val intent = Intent(activity, TakeExamActivity::class.java)
                intent.putExtra("examId", exam.id)
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun fetchExams() {
        // Show the refreshing indicator
        swipeRefreshLayout.isRefreshing = true

        val currentUser = auth.currentUser ?: run {
            // Hide the refreshing indicator if user is null
            swipeRefreshLayout.isRefreshing = false
            return
        }

        db.collection("results")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { resultSnapshots ->
                val attendedExamIds = resultSnapshots.mapNotNull { it.getString("examId") }

                db.collection("exams").get()
                    .addOnSuccessListener { documents ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                        val now = System.currentTimeMillis()

                        upcomingList.clear()
                        runningList.clear()
                        attendedList.clear()
                        pastList.clear()

                        for (document in documents) {
                            val exam = document.toObject(Exam::class.java).apply { id = document.id }

                            if (exam.date.isEmpty() || exam.time.isEmpty() || exam.subject.equals("quiz", ignoreCase = true)) continue

                            val startTime = try {
                                dateFormat.parse("${exam.date} ${exam.time}")?.time ?: continue
                            } catch (e: Exception) {
                                Log.e("ExamsFragment", "Error parsing date/time for exam ${exam.id}: ${e.message}")
                                continue
                            }
                            val durationInMillis = (exam.duration ?: 0) * 60 * 1000L
                            val endTime = startTime + durationInMillis

                            when {
                                attendedExamIds.contains(exam.id) -> attendedList.add(exam)
                                now < startTime -> upcomingList.add(exam)
                                now in startTime..endTime -> runningList.add(exam)
                                now > endTime -> pastList.add(exam)
                            }
                        }

                        activity?.runOnUiThread {
                            upcomingAdapter.notifyDataSetChanged()
                            runningAdapter.notifyDataSetChanged()
                            attendedAdapter.notifyDataSetChanged()
                            pastAdapter.notifyDataSetChanged()
                            // Hide the refreshing indicator after data is loaded
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to load exams!", Toast.LENGTH_SHORT).show()
                        Log.e("ExamsFragment", "Error fetching exams", e)
                        // Hide the refreshing indicator on failure
                        swipeRefreshLayout.isRefreshing = false
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to check attended exams!", Toast.LENGTH_SHORT).show()
                Log.e("ExamsFragment", "Error fetching results", e)
                // Hide the refreshing indicator on failure
                swipeRefreshLayout.isRefreshing = false
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.onlineexamapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.R
import com.example.onlineexamapp.adapters.ResultsAdapter
import com.example.onlineexamapp.models.ResultModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResultsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rvResults: RecyclerView
    private lateinit var tvNoResults: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth and Firestore here
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_results, container, false)

        // Initialize views from the inflated layout
        rvResults = view.findViewById(R.id.rvResults)
        tvNoResults = view.findViewById(R.id.tvNoResults)

        // Set up the RecyclerView layout manager
        rvResults.layoutManager = LinearLayoutManager(requireContext())

        // Fetch and display results
        fetchResults()

        return view
    }

    // Function to fetch exam details by exam ID
    private fun fetchExamDetails(examId: String, callback: (String, String) -> Unit) {
        db.collection("exams").document(examId)
            .get()
            .addOnSuccessListener { document ->
                // Extract exam title and subject, providing default values if null
                val examTitle = document.getString("title") ?: "Unknown Title"
                val subject = document.getString("subject") ?: "Unknown Subject"
                // Invoke the callback with the fetched details
                callback(examTitle, subject)
            }
            .addOnFailureListener {
                // Invoke the callback with default values on failure
                callback("Unknown Title", "Unknown Subject")
            }
    }

    // Function to fetch student name by student ID
    private fun fetchStudentName(studentId: String, callback: (String) -> Unit) {
        db.collection("users").document(studentId)
            .get()
            .addOnSuccessListener { document ->
                // Extract student name, providing a default value if null
                val studentName = document.getString("name") ?: "Unknown Student"
                // Invoke the callback with the fetched name
                callback(studentName)
            }
            .addOnFailureListener {
                // Invoke the callback with a default value on failure
                callback("Unknown Student")
            }
    }

    // Function to fetch results for the current logged-in student
    private fun fetchResults() {
        // Get the current user's ID, return if null
        val studentId = auth.currentUser?.uid ?: return

        db.collection("results")
            .whereEqualTo("userId", studentId) // Filter results by the current user ID
            .get()
            .addOnSuccessListener { result ->
                // Check if the result set is empty
                if (result.isEmpty) {
                    // Display "No results available" message
                    tvNoResults.text = "No results available."
                    tvNoResults.visibility = TextView.VISIBLE
                } else {
                    // Create a mutable list to hold ResultModel objects
                    val resultsList = mutableListOf<ResultModel>()

                    // Iterate through each result document
                    for (document in result) {
                        val id = document.id // Document ID
                        val examId = document.getString("examId") ?: "Unknown Exam" // Exam ID
                        val score = document.getDouble("score") ?: 0.0 // Score
                        val totalMarks = document.getDouble("totalMarks") ?: 0.0 // Total Marks

                        // Fetch exam details asynchronously
                        fetchExamDetails(examId) { examTitle, subject ->
                            // Fetch student name asynchronously
                            fetchStudentName(studentId) { studentName ->
                                // Add the combined data to the results list
                                resultsList.add(ResultModel(id, examId, studentId, score, totalMarks, examTitle, subject, studentName))

                                // Check if all results have been processed and details fetched
                                if (resultsList.size == result.size()) {
                                    // Update the RecyclerView adapter with the fetched data
                                    rvResults.adapter = ResultsAdapter(resultsList)
                                }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                // Show a toast message if fetching results fails
                Toast.makeText(requireContext(), "Error fetching results", Toast.LENGTH_SHORT).show()
            }
    }
}

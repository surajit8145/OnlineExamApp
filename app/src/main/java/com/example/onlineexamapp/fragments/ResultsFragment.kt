package com.example.onlineexamapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineexamapp.R
import com.example.onlineexamapp.adapters.ResultsAdapter
import com.example.onlineexamapp.models.ResultModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ResultsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rvResults: RecyclerView
    private lateinit var tvNoResults: TextView
    private lateinit var tvTotalExams: TextView
    private lateinit var tvAverageScore: TextView
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    private val examCache = mutableMapOf<String, Pair<String, String>>() // examId -> (title, subject)
    private var cachedStudentName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_results, container, false)

        rvResults = view.findViewById(R.id.rvResults)
        tvNoResults = view.findViewById(R.id.tvNoResults)
        tvTotalExams = view.findViewById(R.id.tvTotalExams)
        tvAverageScore = view.findViewById(R.id.tvAverageScore)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)

        rvResults.layoutManager = LinearLayoutManager(requireContext())

        loadUserProfile()
        fetchResults()

        return view
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "Unknown User"
                        val email = document.getString("email") ?: user.email ?: "No Email"
                        val rawPhotoUrl = document.getString("profile_picture") ?: user.photoUrl?.toString()
                        val photoUrl = if (rawPhotoUrl.isNullOrBlank() || rawPhotoUrl == "null") null else rawPhotoUrl

                        tvUserName.text = name
                        tvUserEmail.text = email

                        Glide.with(this)
                            .load(photoUrl ?: R.drawable.ic_profile_placeholder)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(ivProfilePic)
                    } else {
                        tvUserName.text = "Unknown User"
                        tvUserEmail.text = user.email ?: "No Email"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchResults() {
        val studentId = auth.currentUser?.uid ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resultDocs = db.collection("results")
                    .whereEqualTo("userId", studentId)
                    .get().await()

                if (resultDocs.isEmpty) {
                    launch(Dispatchers.Main) {
                        tvNoResults.text = "No results available."
                        tvNoResults.visibility = View.VISIBLE
                        tvTotalExams.text = "Total Exams Taken: 0"
                        tvAverageScore.text = "Average Score: N/A"
                    }
                    return@launch
                }

                val percentages = mutableListOf<Double>()
                val resultsList = resultDocs.documents.map { document ->
                    val id = document.id
                    val examId = document.getString("examId") ?: "Unknown Exam"
                    val score = document.getDouble("score") ?: 0.0
                    val totalMarks = document.getDouble("totalMarks") ?: 0.0
                    val percentage = document.getDouble("percentage") ?: 0.0
                    percentages.add(percentage)

                    async {
                        val (examTitle, subject, date) = getExamDetailsCached(examId)
                        val studentName = getStudentNameCached(studentId)

                        ResultModel(
                            id = id,
                            examId = examId,
                            userId = studentId,
                            score = score,
                            totalMarks = totalMarks,
                            percentage = percentage,
                            examTitle = examTitle,
                            subject = subject,
                            studentName = studentName,
                            examDate = date  // 👈 add this
                        )
                    }
                }.awaitAll()
                val sortedResults = resultsList.sortedByDescending { it.examDate }

                launch(Dispatchers.Main) {
                    rvResults.adapter = ResultsAdapter(sortedResults)
                    tvTotalExams.text = "Total Exams Taken: ${resultsList.size}"
                    val avg = percentages.average()
                    tvAverageScore.text = "Average Score: %.2f%%".format(avg)
                    tvNoResults.visibility = View.GONE
                    setupPieChart(avg)
                    setupBarChart(sortedResults)

                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching results", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getExamDetailsCached(examId: String): Triple<String, String, String> {
        if (examCache.containsKey(examId)) {
            val (title, subject) = examCache[examId]!!
            return Triple(title, subject, "") // fallback: date not cached
        }
        return try {
            val doc = db.collection("exams").document(examId).get().await()
            val title = doc.getString("title") ?: "Untitled Exam"
            val subject = doc.getString("subject") ?: "Unknown Subject"
            val date = doc.getString("date") ?: ""  // 👈 assuming 'date' is stored as a string like "2024-06-03"
            examCache[examId] = Pair(title, subject)
            Triple(title, subject, date)
        } catch (e: Exception) {
            Triple("Untitled Exam", "Unknown Subject", "")
        }
    }

    private suspend fun getStudentNameCached(userId: String): String {
        cachedStudentName?.let { return it }

        return try {
            val doc = db.collection("users").document(userId).get().await()
            val name = doc.getString("name") ?: "Unknown Student"
            cachedStudentName = name
            name
        } catch (e: Exception) {
            "Unknown Student"
        }
    }

    private fun setupPieChart(averageScore: Double) {
        val entries = listOf(
            PieEntry(averageScore.toFloat(), "Scored"),
            PieEntry((100 - averageScore).toFloat(), "Remaining")
        )

        val dataSet = PieDataSet(entries, "Performance")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Avg Score"
        pieChart.setCenterTextSize(16f)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun setupBarChart(resultsList: List<ResultModel>) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        resultsList.forEachIndexed { index, result ->
            entries.add(BarEntry(index.toFloat(), result.percentage.toFloat()))
            labels.add(result.examTitle.take(10)) // Shorten long titles
        }

        val dataSet = BarDataSet(entries, "Exam Scores")
        dataSet.color = resources.getColor(R.color.purple_500, null)
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)

        barData.barWidth = 0.3f  // 👈 Adjust bar width here (try 0.2f to 0.8f)

        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawLabels(true)
        barChart.xAxis.labelRotationAngle = -45f
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.axisMaximum = 100f
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }

}

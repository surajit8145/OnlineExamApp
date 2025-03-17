package com.example.onlineexamapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineexamapp.adapters.ResponseAdapter
import com.example.onlineexamapp.databinding.ActivityViewResponsesBinding
import com.example.onlineexamapp.models.Response
import com.example.onlineexamapp.repositories.ResponseRepository

class ViewResponsesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewResponsesBinding
    private lateinit var responseAdapter: ResponseAdapter
    private val responseList = mutableListOf<Response>()
    private val responseRepository = ResponseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewResponsesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val examId = intent.getStringExtra("examId")

        setupRecyclerView()

        examId?.let {
            loadResponses(it)
        } ?: run {
            Toast.makeText(this, "No exam ID provided", Toast.LENGTH_SHORT).show()
            Log.e("ViewResponsesActivity", "No exam ID provided")
        }
    }

    private fun setupRecyclerView() {
        binding.rvResponses.layoutManager = LinearLayoutManager(this)
        responseAdapter = ResponseAdapter(responseList)
        binding.rvResponses.adapter = responseAdapter
    }

    private fun loadResponses(examId: String) {
        responseRepository.getResponsesByExam(examId) { responses ->
            responseList.clear()
            responseList.addAll(responses)
            responseAdapter.notifyDataSetChanged()

            if (responses.isEmpty()) {
                Toast.makeText(this, "No responses found", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

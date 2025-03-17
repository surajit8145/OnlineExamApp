package com.example.onlineexamapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.databinding.ActivityResultBinding

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example usage: Set text in a TextView
        binding.textViewResult.text = "Your Score: 85%"
    }
}

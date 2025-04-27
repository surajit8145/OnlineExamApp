package com.example.onlineexamapp.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.onlineexamapp.R

import com.example.onlineexamapp.activities.TakeQuizActivity
import com.example.onlineexamapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardViewStudyMaterials.setOnClickListener {
            val studyMaterialLink = "https://drive.google.com/drive/folders/1zN01OPFaL3m_sQU5MrqhOb0_jpoIrG6c?usp=sharing"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(studyMaterialLink))
            startActivity(intent)
        }

        binding.cardViewTakeQuiz.setOnClickListener {
            showStartQuizDialog()
        }
    }

    private fun showStartQuizDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_start_quiz, null)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val rgDifficulty = dialogView.findViewById<RadioGroup>(R.id.rgDifficulty)
        val btnStart = dialogView.findViewById<Button>(R.id.btnStartQuiz)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnStart.setOnClickListener {
            val name = etName.text.toString().trim()
            val selectedDifficultyId = rgDifficulty.checkedRadioButtonId

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
            } else if (selectedDifficultyId == -1) {
                Toast.makeText(requireContext(), "Please select a difficulty level", Toast.LENGTH_SHORT).show()
            } else {
                val difficulty = when (selectedDifficultyId) {
                    R.id.rbEasy -> "easy"
                    R.id.rbMedium -> "medium"
                    R.id.rbHard -> "hard"
                    else -> "easy" // default fallback
                }

                // Start TakeQuizActivity and pass name and difficulty
                val intent = Intent(requireContext(), TakeQuizActivity::class.java)
                intent.putExtra("USER_NAME", name)
                intent.putExtra("DIFFICULTY_LEVEL", difficulty)
                startActivity(intent)

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

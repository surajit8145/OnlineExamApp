package com.example.onlineexamapp.utils

import android.content.Context
import android.util.Log
import com.example.onlineexamapp.models.Question
import com.google.firebase.firestore.FirebaseFirestore

object AddWebDesigningQuestions {

    private val firestore = FirebaseFirestore.getInstance()

    // This function will upload the questions only if they have not been uploaded before
    fun uploadQuestionsIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences("question_upload_prefs", Context.MODE_PRIVATE)
        val alreadyUploaded = prefs.getBoolean("web_designing_questions_uploaded", false)

        if (alreadyUploaded) {
            Log.d("AddQuestions", "Questions already uploaded.")
            return
        }

        // Upload questions if not already uploaded
        uploadQuestions()

        // Save the upload flag to ensure questions aren't uploaded again
        prefs.edit().putBoolean("web_designing_questions_uploaded", true).apply()
    }

    fun uploadQuestions() {
        val examId = "exam1745790793580" // Hard Level Exam ID

        val rawQuestions = listOf(
            Question(
                id = "",
                examId = examId,
                question = "What does HTML stand for?",
                options = mutableListOf("a) Hyperlinks and Text Markup Language", "b) HyperText Markup Language", "c) Home Tool Markup Language", "d) None"),
                correctAnswer = "b) HyperText Markup Language"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which tag is used to define an internal style sheet?",
                options = mutableListOf("a) <script>", "b) <css>", "c) <style>", "d) <link>"),
                correctAnswer = "c) <style>"
            ),
            Question(
                id = "",
                examId = examId,
                question = "In CSS, what does z-index control?",
                options = mutableListOf("a) Position", "b) Stack order", "c) Opacity", "d) Layout"),
                correctAnswer = "b) Stack order"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which HTML tag creates a dropdown list?",
                options = mutableListOf("a) <input>", "b) <dropdown>", "c) <select>", "d) <option>"),
                correctAnswer = "c) <select>"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which CSS property sets the background color?",
                options = mutableListOf("a) bgcolor", "b) background-color", "c) color", "d) style"),
                correctAnswer = "b) background-color"
            ),
            Question(
                id = "",
                examId = examId,
                question = "JavaScript is:",
                options = mutableListOf("a) Server-side", "b) Programming language", "c) Markup language", "d) OS"),
                correctAnswer = "b) Programming language"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which of these is a JavaScript library?",
                options = mutableListOf("a) Flask", "b) React", "c) Django", "d) Laravel"),
                correctAnswer = "b) React"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Bootstrap is used for:",
                options = mutableListOf("a) Backend", "b) Styling and responsiveness", "c) Hosting", "d) Testing"),
                correctAnswer = "b) Styling and responsiveness"
            ),
            Question(
                id = "",
                examId = examId,
                question = "What is DOM?",
                options = mutableListOf("a) Domain Object Model", "b) Document Object Model", "c) Data Object Method", "d) Document Order Method"),
                correctAnswer = "b) Document Object Model"
            ),
            Question(
                id = "",
                examId = examId,
                question = "CSS stands for:",
                options = mutableListOf("a) Cascading Style Sheets", "b) Custom Style Syntax", "c) Computer Style Sheet", "d) Colorful Style System"),
                correctAnswer = "a) Cascading Style Sheets"
            ),
            Question(
                id = "",
                examId = examId,
                question = "The id selector in CSS uses which symbol?",
                options = mutableListOf("a) . (dot)", "b) # (hash)", "c) @", "d) &"),
                correctAnswer = "b) # (hash)"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which HTML tag inserts a line break?",
                options = mutableListOf("a) <br>", "b) <hr>", "c) <p>", "d) <line>"),
                correctAnswer = "a) <br>"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Responsive design adjusts layout for:",
                options = mutableListOf("a) One device", "b) All screen sizes", "c) Printers only", "d) Tablets only"),
                correctAnswer = "b) All screen sizes"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which input type is used for email validation?",
                options = mutableListOf("a) text", "b) email", "c) password", "d) validate"),
                correctAnswer = "b) email"
            ),
            Question(
                id = "",
                examId = examId,
                question = "HTML5 introduced:",
                options = mutableListOf("a) <header>", "b) <font>", "c) <big>", "d) <marquee>"),
                correctAnswer = "a) <header>"
            ),
            Question(
                id = "",
                examId = examId,
                question = "The <canvas> element is used for:",
                options = mutableListOf("a) CSS", "b) Graphics", "c) Text only", "d) Video playback"),
                correctAnswer = "b) Graphics"
            ),
            Question(
                id = "",
                examId = examId,
                question = "In JS, === means:",
                options = mutableListOf("a) Assignment", "b) Strict equality", "c) Loose equality", "d) None"),
                correctAnswer = "b) Strict equality"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which HTTP status code means \"Not Found\"?",
                options = mutableListOf("a) 200", "b) 301", "c) 404", "d) 500"),
                correctAnswer = "c) 404"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which tag is used to play video in HTML5?",
                options = mutableListOf("a) <media>", "b) <movie>", "c) <video>", "d) <play>"),
                correctAnswer = "c) <video>"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which color code is red in Hex?",
                options = mutableListOf("a) #0000FF", "b) #00FF00", "c) #FF0000", "d) #FFFFFF"),
                correctAnswer = "c) #FF0000"
            )
        )

        val questionsList = rawQuestions.mapIndexed { index, question ->
            question.copy(id = "QuizHardQ${index + 1}")
        }

        for (question in questionsList) {
            val docId = "${question.id}-${question.examId}"
            FirebaseFirestore.getInstance().collection("questions")
                .document(docId)
                .set(question)
                .addOnSuccessListener {
                    Log.d("AddQuestions", "Question ${question.id} uploaded successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("AddQuestions", "Error uploading question ${question.id}", e)
                }
        }
    }
}

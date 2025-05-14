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
        val examId = "exam1746431706810" // Easy Level Exam ID

        val rawQuestions = listOf(
            Question(
                id = "",
                examId = examId,
                question = "Which service model gives the developer more control over apps?",
                options = mutableListOf("a) SaaS", "b) PaaS", "c) IaaS", "d) NaaS"),
                correctAnswer = "b) PaaS"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which of these is a PaaS platform?",
                options = mutableListOf("a) Heroku", "b) Dropbox", "c) GitHub", "d) Skype"),
                correctAnswer = "a) Heroku"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Cloud computing architecture includes:",
                options = mutableListOf("a) Front-end and back-end", "b) Only frontend", "c) Only backend", "d) None"),
                correctAnswer = "a) Front-end and back-end"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which service gives you control over OS and storage?",
                options = mutableListOf("a) SaaS", "b) PaaS", "c) IaaS", "d) None"),
                correctAnswer = "c) IaaS"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Load balancing is used to:",
                options = mutableListOf("a) Reduce cost", "b) Increase memory", "c) Distribute workload", "d) Increase bandwidth"),
                correctAnswer = "c) Distribute workload"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Cloud elasticity means:",
                options = mutableListOf("a) Fixed resources", "b) Resource expansion and reduction", "c) Bouncing services", "d) Sticky data"),
                correctAnswer = "b) Resource expansion and reduction"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which model is suitable for software development?",
                options = mutableListOf("a) SaaS", "b) IaaS", "c) PaaS", "d) DRaaS"),
                correctAnswer = "c) PaaS"
            ),
            Question(
                id = "",
                examId = examId,
                question = "SLA stands for:",
                options = mutableListOf("a) Secure Link Access", "b) Software Legal Agreement", "c) Service Level Agreement", "d) Storage Link Application"),
                correctAnswer = "c) Service Level Agreement"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which one is not a cloud-based application?",
                options = mutableListOf("a) Gmail", "b) Google Docs", "c) MS Word (offline)", "d) Zoom"),
                correctAnswer = "c) MS Word (offline)"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which layer includes apps in cloud?",
                options = mutableListOf("a) Application layer", "b) Data link layer", "c) Transport layer", "d) Physical layer"),
                correctAnswer = "a) Application layer"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Cloud bursting means:",
                options = mutableListOf("a) Cloud explosion", "b) Using private cloud until peak, then public", "c) Crashing", "d) None"),
                correctAnswer = "b) Using private cloud until peak, then public"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Data stored on multiple servers is called:",
                options = mutableListOf("a) Replication", "b) Duplication", "c) Mirroring", "d) Virtualization"),
                correctAnswer = "a) Replication"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which tool monitors cloud performance?",
                options = mutableListOf("a) Amazon CloudWatch", "b) MS Excel", "c) Google Meet", "d) GitHub"),
                correctAnswer = "a) Amazon CloudWatch"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Cloud storage is primarily used for:",
                options = mutableListOf("a) Hosting", "b) Storing data", "c) Processing", "d) Searching"),
                correctAnswer = "b) Storing data"
            ),
            Question(
                id = "",
                examId = examId,
                question = "On-demand self-service is a characteristic of:",
                options = mutableListOf("a) Traditional hosting", "b) Cloud computing", "c) LAN", "d) VPN"),
                correctAnswer = "b) Cloud computing"
            ),
            Question(
                id = "",
                examId = examId,
                question = "The layer responsible for resource abstraction is:",
                options = mutableListOf("a) Infrastructure", "b) Platform", "c) Virtualization", "d) Application"),
                correctAnswer = "c) Virtualization"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which database service is cloud-native?",
                options = mutableListOf("a) MySQL", "b) MongoDB Atlas", "c) Oracle 11g", "d) MS Access"),
                correctAnswer = "b) MongoDB Atlas"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Multi-tenancy refers to:",
                options = mutableListOf("a) Shared apps across users", "b) One app per user", "c) Virtual OS", "d) Isolated servers"),
                correctAnswer = "a) Shared apps across users"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which one is NOT an open-source cloud platform?",
                options = mutableListOf("a) OpenStack", "b) CloudStack", "c) Azure", "d) Eucalyptus"),
                correctAnswer = "c) Azure"
            ),
            Question(
                id = "",
                examId = examId,
                question = "Which company provides IBM Cloud?",
                options = mutableListOf("a) Oracle", "b) Microsoft", "c) IBM", "d) Google"),
                correctAnswer = "c) IBM"
            )
        )

        val questionsList = rawQuestions.mapIndexed { index, question ->
            question.copy(id = "QuizMediumQ${index + 1}")
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

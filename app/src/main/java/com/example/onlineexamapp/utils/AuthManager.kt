package com.example.onlineexamapp.utils

import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    private val auth = FirebaseAuth.getInstance()

    fun login(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun logout() {
        auth.signOut()
    }
}

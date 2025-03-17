// FirebaseHelper.kt (Updated)
package com.example.onlineexamapp.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getUserId(): String? = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getUserRole(callback: (String?) -> Unit) {
        getUserId()?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document -> callback(document.getString("role")) }
                .addOnFailureListener { callback(null) }
        } ?: callback(null)
    }

    fun logoutUser() { auth.signOut() }
}

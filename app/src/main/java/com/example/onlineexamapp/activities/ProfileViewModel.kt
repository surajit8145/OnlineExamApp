package com.example.onlineexamapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userProfile = MutableLiveData<DocumentSnapshot?>()
    val userProfile: LiveData<DocumentSnapshot?> get() = _userProfile

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.postValue("User not authenticated.")
            _loading.postValue(false)
            _userProfile.postValue(null)
            return
        }

        _loading.postValue(true)
        _errorMessage.postValue(null)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = db.collection("users").document(userId).get().await()
                withContext(Dispatchers.Main) {
                    _userProfile.value = document
                    _loading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Failed to fetch profile: ${e.localizedMessage}"
                    _loading.value = false
                    _userProfile.value = null
                }
            }
        }
    }
}

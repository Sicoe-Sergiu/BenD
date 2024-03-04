package com.example.bend.view_models

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val userData: Map<String, Any>) : ProfileState()
    data class Error(val errorMessage: String) : ProfileState()
}
class ProfileViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser
    private val collections = listOf("artist", "event_founder", "user")
    private var documentId = ""
    var accountType: LiveData<String> = MutableLiveData("")


    private val _userData = MutableLiveData<Map<String, Any>?>()
    private val _profileState = MutableLiveData<ProfileState>()
    val userData: LiveData<Map<String, Any>?> get() = _userData
    val profileState: LiveData<ProfileState> get() = _profileState
    init {
        if (currentUser != null) {
            documentId = currentUser.uid
            fetchDocumentData()
        }
    }

    fun fetchDocumentData() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val data = getDocumentById(collections, documentId)
                if (data != null) {
                    _profileState.value = ProfileState.Success(data)
                } else {
                    _profileState.value = ProfileState.Error("Data not found")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Error fetching data: ${e.message}")
            }
        }
    }

    private suspend fun getDocumentById(collectionNames: List<String>, documentId: String): Map<String, Any>? {
        val db = FirebaseFirestore.getInstance()

        for (collectionName in collectionNames) {
            try {
                val documentReference = db.collection(collectionName).document(documentId)
                val snapshot = documentReference.get().await()

                if (snapshot.exists()) {
                    accountType = collectionName
                    return snapshot.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return null
    }
}

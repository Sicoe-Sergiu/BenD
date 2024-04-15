package com.example.bend.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bend.model.Event
import com.example.bend.model.Review
import com.example.bend.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ReviewsViewModel: ViewModel(){
    var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val events: LiveData<List<Event>> = MutableLiveData(emptyList())

    companion object {
        suspend fun getReviewsForEventAndFounder(eventUUID: String, founderUUID: String): List<Review> {
            try {
                val task = FirebaseFirestore.getInstance().collection("review").whereEqualTo("eventUUID", eventUUID).whereEqualTo("userUUID", founderUUID).get().await()
                return task.toObjects(Review::class.java)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return emptyList()
        }
        suspend fun getUserByUUID(userUUID: String): User {
            try {
                val task = FirebaseFirestore.getInstance().collection("user").whereEqualTo("uuid", userUUID).get().await()
                val users = task.toObjects(User::class.java)
                if (users.isNotEmpty()) {
                    return users.first()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return User()
        }
    }

    fun loadData(founderUUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val accountTypeDeferred = async { MyEventsViewModel.getAccountType(founderUUID) }
            val accountTypeValue = accountTypeDeferred.await()

            if (accountTypeValue == "artist"){
                fetchArtistEvents(founderUUID)
            }else if (accountTypeValue == "event_founder"){
                fetchFounderEvents(founderUUID)
            }
        }
    }

    private suspend fun fetchFounderEvents(founderUUID: String) {
        _isLoading.value = true
        (events as MutableLiveData).postValue(MyEventsViewModel.getFounderEvents(founderUUID))
        _isLoading.value = false
    }
    private suspend fun fetchArtistEvents(artistUUID: String) {
        _isLoading.value = true
        (events as MutableLiveData).postValue(MyEventsViewModel.getArtistEvents(artistUUID))
        _isLoading.value = false
    }
}
package com.example.bend.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.example.bend.model.Review
import com.example.bend.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class ReviewsViewModel: ViewModel(){
    var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val events: LiveData<List<Event>> = MutableLiveData(emptyList())

    companion object {
        suspend fun getReviewsForEvent(eventUUID: String): List<Review> {
            try {
                val task = FirebaseFirestore.getInstance().collection("review").whereEqualTo("eventUUID", eventUUID).get().await()
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

    fun loadData() {

    }

    suspend fun fetchEvents(founderUUID: String) {
        _isLoading.value = true
        (events as MutableLiveData).postValue(MyEventsViewModel.getFounderEvents(founderUUID))
        _isLoading.value = false
    }
}
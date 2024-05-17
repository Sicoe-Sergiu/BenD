package com.example.bend.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
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
        suspend fun getReviewsForEventAndFounder(context: Context?, eventUUID: String, founderUUID: String): List<Review> {
            try {
                Log.e("params", "$eventUUID $founderUUID")

                val task = FirebaseFirestore.getInstance()
                    .collection("review")
                    .whereEqualTo("eventUUID", eventUUID)
                    .whereEqualTo("reviewedUserUUID", founderUUID)
                    .get()
                    .await()
                return task.toObjects(Review::class.java)

            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: "Error fetching reviews for event and founder."
                Log.e("getReviewsForEventAndFounder", errorMessage, e)
                e.printStackTrace()
                if (context != null) {
                    Toast.makeText(
                        context,
                        e.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return emptyList()
        }

        suspend fun getUserByUUID(context: Context?, userUUID: String): User {
            try {
                val task = FirebaseFirestore.getInstance()
                    .collection("user")
                    .whereEqualTo("uuid", userUUID)
                    .get()
                    .await()
                val users = task.toObjects(User::class.java)
                if (users.isNotEmpty()) {
                    return users.first()
                }
            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: "Error fetching user by UUID."
                Log.e("getUserByUUID", errorMessage, e)
                e.printStackTrace()
                if (context != null) {
                    Toast.makeText(
                        context,
                        e.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return User()
        }
    }

    fun loadData(context: Context, founderUUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val accountTypeDeferred = async { HomeViewModel.getAccountType(null, founderUUID) }
            val accountTypeValue = accountTypeDeferred.await()

            if (accountTypeValue == "artist"){
                fetchArtistEvents(context, founderUUID)
            }else if (accountTypeValue == "event_founder"){
                fetchFounderEvents(context, founderUUID)
            }
        }
    }

    private suspend fun fetchFounderEvents(context: Context, founderUUID: String) {
        _isLoading.value = true
        (events as MutableLiveData).postValue(MyEventsViewModel.getFounderEvents(context, founderUUID))
        _isLoading.value = false
    }
    private suspend fun fetchArtistEvents(context: Context, artistUUID: String) {
        _isLoading.value = true
        (events as MutableLiveData).postValue(MyEventsViewModel.getArtistEvents(context, artistUUID))
        _isLoading.value = false
    }
}
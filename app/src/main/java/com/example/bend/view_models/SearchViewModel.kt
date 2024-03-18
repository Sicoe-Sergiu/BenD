package com.example.bend.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.example.bend.model.User
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchViewModel : ViewModel(){

    val TAG = "SearchViewModel: "
    private val userCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("user")
    private val artistsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("artist")
    private val eventFounderCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event_founder")
    private val eventsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event")
    private val eventArtistCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event_artist")
    private val userEventCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("user_event")

    var events: LiveData<List<Event>> = MutableLiveData(emptyList())
    var founders: LiveData<List<EventFounder>> = MutableLiveData(emptyList())
    var artists: LiveData<List<Artist>> = MutableLiveData(emptyList())
    var users: LiveData<List<User>> = MutableLiveData(emptyList())

    var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    init {
        viewModelScope.launch (Dispatchers.IO){
            val fetchArtistsDeferred = async { fetchArtists() }
            val fetchEventsDeferred = async { searchEvents() }
            val fetchEventFoundersDeferred = async { fetchEventFounders() }
            val fetchUsersDeferred = async { fetchUsers() }

            awaitAll(
                fetchArtistsDeferred,
                fetchEventsDeferred,
                fetchEventFoundersDeferred,
                fetchUsersDeferred
            )
        }
    }
    fun search(queryString: String) {
        viewModelScope.launch {
            _isLoading.value = true




            _isLoading.value = false
        }
    }
    private suspend fun searchEvents() {
        try {
            val eventsList = eventsCollection.get().await().toObjects(Event::class.java)
            (events as MutableLiveData).postValue(eventsList)
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching events: $exception")
        }
    }
    private suspend fun fetchArtists() {
        try {
            val artistsList = artistsCollection.get().await().toObjects(Artist::class.java)
            (artists as MutableLiveData).postValue(artistsList)
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching artists: $exception")
        }
    }

    private suspend fun fetchEventFounders() {
        try {
            val eventFoundersList =
                eventFounderCollection.get().await().toObjects(EventFounder::class.java)
            (founders as MutableLiveData).postValue(eventFoundersList)
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching founders: $exception")
        }
    }
    private suspend fun fetchUsers() {
        try {
            val usersList =
                userCollection.get().await().toObjects(User::class.java)
            (users as MutableLiveData).postValue(usersList)
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching users: $exception")
        }
    }
}
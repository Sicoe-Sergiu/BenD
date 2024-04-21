package com.example.bend.viewmodel

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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchViewModel : ViewModel() {

    val TAG = SearchViewModel::class.simpleName

    private val userCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("user")
    private val artistsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("artist")
    private val eventFounderCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event_founder")
    private val eventsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event")

    var events: LiveData<List<Event>> = MutableLiveData(emptyList())
    var founders: LiveData<List<EventFounder>> = MutableLiveData(emptyList())
    var artists: LiveData<List<Artist>> = MutableLiveData(emptyList())
    var users: LiveData<List<User>> = MutableLiveData(emptyList())

    private lateinit var fetchArtistsDeferred: Deferred<List<Artist>>
    private lateinit var fetchEventsDeferred: Deferred<List<Event>>
    private lateinit var fetchEventFoundersDeferred: Deferred<List<EventFounder>>
    private lateinit var fetchUsersDeferred: Deferred<List<User>>

    var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val errorMessages: LiveData<String> = MutableLiveData()

    init {
        fetchData()
    }

    fun search(queryString: String) {
        viewModelScope.launch {
            _isLoading.value = true

            if (this@SearchViewModel::fetchArtistsDeferred.isInitialized) awaitAll(
                fetchArtistsDeferred,
                fetchEventsDeferred,
                fetchEventFoundersDeferred,
                fetchUsersDeferred
            )

            updateLiveData()
            filterResults(queryString)

            _isLoading.value = false
        }
    }

    private fun filterResults(queryString: String) {
        _isLoading.value = true
        val currentArtists = artists.value ?: emptyList()
        val currentEvents = events.value ?: emptyList()
        val currentFounders = founders.value ?: emptyList()
        val currentUsers = users.value ?: emptyList()

        val matchedArtists = currentArtists.filter {
            it.username.contains(queryString, ignoreCase = true) ||
                    it.stageName.contains(queryString, ignoreCase = true) ||
                    it.firstName.contains(queryString, ignoreCase = true) ||
                    it.lastName.contains(queryString, ignoreCase = true)
        }

        val matchedFounders = currentFounders.filter {
            it.username.contains(queryString, ignoreCase = true) ||
                    it.firstName.contains(queryString, ignoreCase = true) ||
                    it.lastName.contains(queryString, ignoreCase = true) ||
                    it.username.contains(queryString, ignoreCase = true)

        }

        val matchedFounderUUIDs = matchedFounders.map { it.uuid }

        val matchedEvents = currentEvents.filter {
            it.location.contains(queryString, ignoreCase = true) ||
                    matchedFounderUUIDs.contains(it.founderUUID)
        }

        val matchedUsers = currentUsers.filter {
            it.username.contains(queryString, ignoreCase = true) ||
                    it.firstName.contains(queryString, ignoreCase = true) ||
                    it.lastName.contains(queryString, ignoreCase = true)
        }

        Log.d(
            "SearchResults",
            "Artists found: ${matchedArtists.size}, Events found: ${matchedEvents.size}, Founders found: ${matchedFounders.size}, Users found: ${matchedUsers.size}"
        )
        (founders as MutableLiveData).postValue(matchedFounders)
        (events as MutableLiveData).postValue(matchedEvents)
        (artists as MutableLiveData).postValue(matchedArtists)
        (users as MutableLiveData).postValue(matchedUsers)

        _isLoading.value = false
    }

    private fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            fetchArtistsDeferred = async { fetchArtists() }
            fetchEventsDeferred = async { searchEvents() }
            fetchEventFoundersDeferred = async { fetchEventFounders() }
            fetchUsersDeferred = async { fetchUsers() }
        }
    }

    private fun updateLiveData() {
        if ((artists.value.isNullOrEmpty())) {
            (artists as MutableLiveData).value = fetchArtistsDeferred.getCompleted()
            (events as MutableLiveData).value = fetchEventsDeferred.getCompleted()
            (founders as MutableLiveData).value = fetchEventFoundersDeferred.getCompleted()
            (users as MutableLiveData).value = fetchUsersDeferred.getCompleted()
        }
    }

    private suspend fun searchEvents(): List<Event> {
        try {
            return eventsCollection.get().await().toObjects(Event::class.java)
        } catch (exception: Exception) {
            val errorMessage = "Error fetching events: ${exception.localizedMessage}"
            Log.e(TAG, errorMessage, exception)
            postError(errorMessage)
        }
        return emptyList()
    }

    private suspend fun fetchArtists(): List<Artist> {
        try {
            return artistsCollection.get().await().toObjects(Artist::class.java)
        } catch (exception: Exception) {
            val errorMessage = "Error fetching artists: ${exception.localizedMessage}"
            Log.e(TAG, errorMessage, exception)
            postError(errorMessage)
        }
        return emptyList()
    }

    private suspend fun fetchEventFounders(): List<EventFounder> {
        try {
            return eventFounderCollection.get().await().toObjects(EventFounder::class.java)
        } catch (exception: Exception) {
            val errorMessage = "Error fetching founders: ${exception.localizedMessage}"
            Log.e(TAG, errorMessage, exception)
            postError(errorMessage)
        }
        return emptyList()
    }

    private suspend fun fetchUsers(): List<User> {
        try {
            return userCollection.get().await().toObjects(User::class.java)
        } catch (exception: Exception) {
            val errorMessage = "Error fetching users: ${exception.localizedMessage}"
            Log.e(TAG, errorMessage, exception)
            postError(errorMessage)
        }
        return emptyList()
    }


    private fun postError(message: String) {
        (errorMessages as MutableLiveData).postValue(message)
    }

    fun clearError() {
        (errorMessages as MutableLiveData).postValue("")
    }
}
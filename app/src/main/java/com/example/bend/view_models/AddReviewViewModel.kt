package com.example.bend.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventArtist
import com.example.bend.model.EventFounder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AddReviewViewModel : ViewModel() {
    private val TAG = "ADD REVIEW SCREEN"
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser

    private val eventsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event")
    private val userCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("user")
    private val artistsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("artist")
    private val eventFounderCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event_founder")
    private val eventArtistCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event_artist")
    private val userEventCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("user_event")

    private var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    var event: LiveData<Event> = MutableLiveData()
    var founder: LiveData<EventFounder> = MutableLiveData()
    var artists: LiveData<List<Artist>> = MutableLiveData(emptyList())

    fun loadData( eventUUID: String){
        viewModelScope.launch (Dispatchers.IO){
            _isLoading.value = true

            val fetchEventDeferred = async { fetchEventByUUID(eventUUID) }
            val fetchArtistsDeferred = async { fetchEventArtists(eventUUID) }

            awaitAll(
                fetchArtistsDeferred,
                fetchEventDeferred,
            )
            val fetchFounderDeferred = async { fetchFounderByUUID(event.value!!.founderUUID) }
            fetchFounderDeferred.await()

            _isLoading.value = false
        }
    }

    private suspend fun fetchEventByUUID(eventUUID: String) {
        try {
            val task = eventsCollection.whereEqualTo("uuid", eventUUID).get().await()

            val events = task.toObjects(Event::class.java)

            if (events.isNotEmpty()) {
                (event as MutableLiveData).postValue(events.first())
            } else {
                Log.e("fetchAndPostEventByUUID", "No events found with UUID: $eventUUID")
            }
        } catch (e: Exception) {
            Log.e("fetchAndPostEventByUUID", "Error fetching event by UUID: $eventUUID", e)
        }
    }
    private suspend fun fetchFounderByUUID(founderUUID: String) {
        try {
            val task = eventFounderCollection.whereEqualTo("uuid", founderUUID).get().await()

            val founders = task.toObjects(EventFounder::class.java)

            if (founders.isNotEmpty()) {
                (founder as MutableLiveData).postValue(founders.first())
            } else {
                Log.e("fetchAndPostEventFounderByUUID", "No Founder found with UUID: $founderUUID")
            }
        } catch (e: Exception) {
            Log.e("fetchAndPostEventFounderByUUID", "Error fetching Founder by UUID: $founderUUID", e)
        }
    }

    private suspend fun fetchEventArtists(eventUUID: String) {
        val _artists: MutableList<Artist> = mutableListOf()
        try {
            val task = eventArtistCollection.whereEqualTo("eventUUID", eventUUID).get().await()

            val eventArtists = task.toObjects(EventArtist::class.java)
            for (eventArtist in eventArtists) {
                try {
                    val task1 =
                        artistsCollection.whereEqualTo("uuid", eventArtist.artistUUID).get().await()

                    _artists.add(task1.toObjects(Artist::class.java).first())

                } catch (e: Exception) {
                    Log.e("fetchEventArtists", "Error fetching artists by UUID: $eventUUID", e)
                }
            }

        } catch (e: Exception) {
            Log.e("fetchEventArtists", "Error fetching eventArtists by UUID: $eventUUID", e)
        }
        (artists as MutableLiveData).postValue(_artists)
    }


}
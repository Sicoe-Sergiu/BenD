package com.example.bend.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventArtist
import com.example.bend.model.EventFounder
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    val TAG = "HOME VIEW MODEL"
    var events: LiveData<List<Event>> = MutableLiveData(emptyList())
    var artists: LiveData<List<Artist>> = MutableLiveData(emptyList())
    var founders: LiveData<List<EventFounder>> = MutableLiveData(emptyList())
    var eventArtists: LiveData<List<EventArtist>> = MutableLiveData(emptyList())

    private val eventsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event")
    private val artistsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("artist")
    private val eventFounderCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event_founder")
    private val eventArtistCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event_artist")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun fetchArtists() {
        artistsCollection
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val artistsList = mutableListOf<Artist>()
                    for (document in task.result!!) {
                        val artist = document.toObject(Artist::class.java)
                        artistsList.add(artist)
                    }
                    (artists as MutableLiveData).postValue(artistsList)
                } else {
                    Log.e("FETCH_ARTISTS", "Error fetching artists: ${task.exception}")
                }
            }
    }

    private fun fetchEvents() {
        eventsCollection
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val eventsList = mutableListOf<Event>()
                    for (document in task.result!!) {
                        val event = document.toObject(Event::class.java)
                        eventsList.add(event)
                    }
                    (events as MutableLiveData).postValue(eventsList)
                } else {
                    Log.e("FETCH_EVENTS", "Error fetching events: ${task.exception}")
                }
            }
    }

    private fun fetchEventFounders() {
        eventFounderCollection
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val eventFoundersList = mutableListOf<EventFounder>()
                    for (document in task.result!!) {
                        val founder = document.toObject(EventFounder::class.java)
                        eventFoundersList.add(founder)
                    }
                    (founders as MutableLiveData).postValue(eventFoundersList)
                } else {
                    Log.e("FETCH_FOUNDERS", "Error fetching founders: ${task.exception}")
                }
            }
    }

    private fun fetchEventArtists() {
        eventArtistCollection
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val eventArtistList = mutableListOf<EventArtist>()
                    for (document in task.result!!) {
                        val eventArtist = document.toObject(EventArtist::class.java)
                        eventArtistList.add(eventArtist)
                    }
                    (eventArtists as MutableLiveData).postValue(eventArtistList)
                } else {
                    Log.e("FETCH_EVENT_ARTISTS", "Error fetching event artists: ${task.exception}")
                }
            }
    }
    fun loadData() {
        _isLoading.value = true
        Log.d(TAG, "loading data...")
        fetchEvents()
        fetchArtists()
        fetchEventFounders()
        fetchEventArtists()
        Log.d(TAG, "loading data DONE")
        _isLoading.value = false
    }

    fun getFounderByUUID(founderUUID: String): EventFounder? {
        val founderList = founders.value
        return founderList?.find { founder -> founder.uuid == founderUUID }
    }

    fun getEventArtists(event: Event): List<Artist> {
        val eventArtists = eventArtists.value?.filter { it.eventUUID == event.uuid } ?: emptyList()
        Log.d(TAG, eventArtists.toString())
        val artistsUUIDS = eventArtists.map { it.artistUUID }
        Log.d(TAG, artistsUUIDS.toString())
        val artists = artists.value?.filter { it.uuid in artistsUUIDS } ?: emptyList()
        Log.d(TAG, artists.toString())
        return artists
    }
}

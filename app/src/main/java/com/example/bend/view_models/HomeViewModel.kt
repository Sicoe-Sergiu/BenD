package com.example.bend.view_models

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventArtist
import com.example.bend.model.EventFounder
import com.example.bend.model.User
import com.example.bend.model.UserEvent
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class HomeViewModel : ViewModel() {

    private val TAG = "HOME VIEW MODEL"
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    var events: LiveData<List<Event>> = MutableLiveData(emptyList())
    var artists: LiveData<List<Artist>> = MutableLiveData(emptyList())
    var founders: LiveData<List<EventFounder>> = MutableLiveData(emptyList())
    var eventArtists: LiveData<List<EventArtist>> = MutableLiveData(emptyList())
    var eventsAttendees: LiveData<List<Pair<Event, Int>>> = MutableLiveData(emptyList())
    var accountType: LiveData<String> = MutableLiveData("")

    var homeScreenScrollState: LazyListState by mutableStateOf(LazyListState(0, 0))
    private val usersCollectionNames = listOf<String>("artist", "event_founder", "user")

    init {
        viewModelScope.launch {
            loadData()
            (accountType as MutableLiveData).postValue(getAccountType(currentUser?.uid.toString()))
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "loading data...")
            fetchEvents().await()
            fetchArtists().await()
            fetchEventFounders().await()
            fetchEventArtists().await()
            Log.d(TAG, "loading data DONE")
            _isLoading.value = false
        }
    }

    private suspend fun getAccountType(userUUID: String): String {
        try {
            val artistSnapshot =
                artistsCollection.document(userUUID).get().await()
            val founderSnapshot =
                eventFounderCollection.document(userUUID).get().await()
            val userSnapshot = userCollection.document(userUUID).get().await()

            if (artistSnapshot.exists()) {
                return "artist"
            } else if (founderSnapshot.exists()) {
                return "event_founder"

            } else if (userSnapshot.exists()) {
                return "user"
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., log, report, or throw)
        }
        return ""
    }

    private fun fetchArtists(): Deferred<Unit> = viewModelScope.async {
        artistsCollection.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val artistsList = task.result!!.toObjects(Artist::class.java)
                    (artists as MutableLiveData).postValue(artistsList)
                } else {
                    Log.e(TAG, "Error fetching artists: ${task.exception}")
                }
            }
    }

    private fun fetchEvents(): Deferred<Unit> = viewModelScope.async {
        eventsCollection.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val eventsList = task.result!!.toObjects(Event::class.java)
                    (events as MutableLiveData).postValue(eventsList)

                    fetchEventAttendees(eventsList)
                } else {
                    Log.e(TAG, "Error fetching events: ${task.exception}")
                }
            }
    }

    private fun fetchEventAttendees(eventsList: List<Event>) {
        val eventTasks = eventsList.map { event ->
            userEventCollection.whereEqualTo("event", event).get()
        }

        Tasks.whenAllSuccess<QuerySnapshot>(eventTasks)
            .addOnSuccessListener { snapshots ->
                val attendees = eventsList.zip(snapshots.map { it.size() })
                (eventsAttendees as MutableLiveData).postValue(attendees)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching attendees", exception)
            }
    }

    private fun fetchEventFounders(): Deferred<Unit> = viewModelScope.async {
        eventFounderCollection.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val eventFoundersList = task.result!!.toObjects(EventFounder::class.java)
                    (founders as MutableLiveData).postValue(eventFoundersList)
                } else {
                    Log.e(TAG, "Error fetching founders: ${task.exception}")
                }
            }
    }

    private fun fetchEventArtists(): Deferred<Unit> = viewModelScope.async {
        eventArtistCollection.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val eventArtistList = task.result!!.toObjects(EventArtist::class.java)
                    (eventArtists as MutableLiveData).postValue(eventArtistList)
                } else {
                    Log.e(TAG, "Error fetching event artists: ${task.exception}")
                }
            }
    }

    fun getFounderByUUID(founderUUID: String): EventFounder? {
        return founders.value?.find { founder -> founder.uuid == founderUUID }
    }

    fun getEventArtists(event: Event): List<Artist> {
        val eventArtists = eventArtists.value?.filter { it.eventUUID == event.uuid } ?: emptyList()
        val artistsUUIDS = eventArtists.map { it.artistUUID }
        return artists.value?.filter { it.uuid in artistsUUIDS } ?: emptyList()
    }

    private fun updateAttendeesCountForEvent(event: Event, increment: Boolean) {
        val currentList = eventsAttendees.value.orEmpty()
        val updatedList = currentList.map {
            if (it.first == event) {
                Pair(event, if (increment) it.second + 1 else it.second - 1)
            } else {
                it
            }
        }
        Log.d(if (increment) "INCREMENT" else "DECREMENT", updatedList.toString())
        (eventsAttendees as MutableLiveData).postValue(updatedList)
    }

    fun addEventToUserList(event: Event) {
        Log.e("ADD EVENT", "user id :${currentUser?.uid}")

        userCollection.whereEqualTo("uuid", currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    val userEvent = UserEvent(UUID.randomUUID().toString(), user, event)
                    userEventCollection.document(userEvent.uuid)
                        .set(userEvent)
                        .addOnSuccessListener {
                            updateAttendeesCountForEvent(event, increment = true)
                            // TODO: Handle success
                        }
                        .addOnFailureListener { exception ->
                            // TODO: Handle failure
                        }
                }
            }
            .addOnFailureListener { exception ->
                // TODO: Handle failure
                Log.e(TAG, "Error getting documents: $exception")
            }
    }

    fun removeEventFromUserList(event: Event) {

        userEventCollection
            .whereEqualTo("user.uuid", currentUser?.uid)
            .whereEqualTo("event.uuid", event.uuid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            updateAttendeesCountForEvent(event, increment = false)
                            Log.e("DELETE EVENT", "Document successfully deleted")
                        }
                        .addOnFailureListener { e ->
                            Log.e("DELETE EVENT", "Error deleting document: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("DELETE EVENT", "Error querying documents: $e")
            }
    }

    fun repostEvent(event: Event) {
        // TODO: Implement reposting logic
    }

    suspend fun ifAttend(event: Event): Boolean {
        return try {
            val eventDocuments = userEventCollection
                .whereEqualTo("event", event)
                .whereEqualTo("user.uuid", currentUser?.uid)
                .get()
                .await()

            !eventDocuments.isEmpty
        } catch (e: Exception) {
            // TODO: Handle exception (e.g., log, report, or throw)
            false
        }
    }

    suspend fun getUserMapById(userId: String): Pair<String, MutableMap<String, Any>?>? {
        val db = FirebaseFirestore.getInstance()

        for (collectionName in usersCollectionNames) {
            try {
                val documentReference = db.collection(collectionName).document(userId)
                val snapshot = documentReference.get().await()

                if (snapshot.exists()) {
                    return Pair(collectionName, snapshot.data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return null
    }

    fun follow(followedUserUUID: String) {
        TODO("Not yet implemented")
    }

    fun unfollow(unfollowedUserUUID: String) {
        TODO("Not yet implemented")
    }

    suspend fun getUserEvents(userUUID: String): List<Event> {
        val _accountType = getAccountType(userUUID)

        if (_accountType == "artist"){
            eventArtistCollection
                .whereEqualTo("artistUUID", userUUID)
                .get()
                .addOnSuccessListener {

                }
                .addOnFailureListener{
//                    TODO:implement
                }

        }else if (_accountType == "event_founder"){

        }else if (_accountType == "user"){

        }



        return emptyList()
    }
}
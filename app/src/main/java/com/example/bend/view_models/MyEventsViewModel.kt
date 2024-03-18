package com.example.bend.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MyEventsViewModel : ViewModel() {
    val TAG = "MyEventViewModel"
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser

    var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    var accountType: LiveData<String> = MutableLiveData("")

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
    private val followersCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("followers")

    var events: LiveData<List<Event>> = MutableLiveData(emptyList())

    init {
        loadMyEvents()
    }

    fun loadMyEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "loading my events...")

            val accountTypeDeferred = async { getAccountType(currentUser?.uid.toString()) }
            val accountTypeValue = accountTypeDeferred.await()

            val eventsDeferred = when (accountTypeValue) {
                "user" -> async { getRegularUserEvents(currentUser?.uid ?: "") }
                "artist" -> async { getArtistEvents(currentUser?.uid ?: "") }
                "event_founder" -> async { getFounderEvents(currentUser?.uid ?: "") }
                else -> null
            }

            withContext(Dispatchers.Main) {
                (accountType as MutableLiveData).postValue(accountTypeValue)
                eventsDeferred?.let {
                    (events as MutableLiveData).postValue(it.await())
                }
            }

            Log.d(TAG, "loading my events DONE")
            _isLoading.value = false
        }
    }


    private suspend fun getRegularUserEvents(userID: String): List<Event> {
        val events = mutableListOf<Event>()

        try {
            val eventUserRecords =
                userEventCollection.whereEqualTo("userUUID", userID).get().await()
            for (record in eventUserRecords) {
                val eventUUID = record.getString("eventUUID")
                val eventRecords = eventsCollection.whereEqualTo("uuid", eventUUID).get().await()
                for (event in eventRecords) {
                    events.add(event.toObject(Event::class.java))
                }
            }
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        }

        return events
    }

    private suspend fun getArtistEvents(userID: String): List<Event> {
        val events = mutableListOf<Event>()

        try {
            val eventArtistRecords =
                eventArtistCollection.whereEqualTo("artistUUID", userID).get().await()
            for (record in eventArtistRecords) {
                val eventUUID = record.getString("eventUUID")
                val eventRecords = eventsCollection.whereEqualTo("uuid", eventUUID).get().await()
                for (event in eventRecords) {
                    events.add(event.toObject(Event::class.java))
                }
            }
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        }

        return events
    }

    private suspend fun getFounderEvents(userID: String): List<Event> {
        try {
            val task = eventsCollection.whereEqualTo("founderUUID", userID).get().await()
            return task.toObjects(Event::class.java)
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        }

        return emptyList()
    }

    private suspend fun getAccountType(userUUID: String): String = coroutineScope {
        try {
            val artistSnapshotDeferred =
                async { artistsCollection.document(userUUID).get().await() }
            val founderSnapshotDeferred =
                async { eventFounderCollection.document(userUUID).get().await() }
            val userSnapshotDeferred = async { userCollection.document(userUUID).get().await() }

            val (artistSnapshot, founderSnapshot, userSnapshot) = awaitAll(
                artistSnapshotDeferred,
                founderSnapshotDeferred,
                userSnapshotDeferred
            )

            return@coroutineScope when {
                artistSnapshot.exists() -> "artist"
                founderSnapshot.exists() -> "event_founder"
                userSnapshot.exists() -> "user"
                else -> ""
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., log, report, or throw)
            ""
        }
    }

    companion object {
        suspend fun getEventFounderByUuid(uuid: String): EventFounder? {
            try {
                val documents = FirebaseFirestore.getInstance().collection("event_founder")
                    .whereEqualTo("uuid", uuid)
                    .get()
                    .await()

                if (documents.isEmpty) {
                    println("No matching documents found")
                    return null
                }

                val document = documents.documents.firstOrNull()
                document?.let {
                    return document.toObject(EventFounder::class.java)
                }
            } catch (e: Exception) {
                println("Error getting documents: $e")
            }
            return null
        }
    }

    suspend fun removeEvent(event: Event) {
        when (accountType.value) {
            "artist" -> {
                deleteFromEventArtist(event.uuid, currentUser?.uid)
            }

            "user" -> {
                deleteFromEventUser(event.uuid, currentUser?.uid)
            }

            "event_founder" -> {
                deleteEvent(event.uuid, currentUser?.uid)
            }
        }

    }

    private suspend fun deleteFromEventArtist(eventUUID: String, artistUUID: String? = null){
        try {
            val query = if (artistUUID != null) {
                eventArtistCollection
                    .whereEqualTo("artistUUID", artistUUID)
                    .whereEqualTo("eventUUID", eventUUID)
            } else {
                eventArtistCollection
                    .whereEqualTo("eventUUID", eventUUID)
            }

            val documents = query.get().await()

            documents.forEach { document ->
                eventArtistCollection.document(document.id).delete().await()
            }

            println("All matching documents successfully deleted.")
            loadMyEvents()
        } catch (e: Exception) {
            println("Error deleting documents: ${e.message}")
        }
    }

    private suspend fun deleteFromEventUser(eventUUID: String, userUUID: String? = null){
        try {
            val query = if (userUUID != null) {
                userEventCollection
                    .whereEqualTo("userUUID", userUUID)
                    .whereEqualTo("eventUUID", eventUUID)
            } else {
                userEventCollection
                    .whereEqualTo("eventUUID", eventUUID)
            }

            val documents = query.get().await()

            documents.forEach { document ->
                userEventCollection.document(document.id).delete().await()
            }

            println("All matching documents successfully deleted.")
            loadMyEvents()
        } catch (e: Exception) {
            println("Error deleting documents: ${e.message}")
        }
    }

    private suspend fun deleteEvent(eventUUID: String, userUUID: String?) {
        try {
            val documents = eventsCollection
                .whereEqualTo("founderUUID", userUUID)
                .whereEqualTo("uuid", eventUUID)
                .get()
                .await()

            documents.forEach { document ->
                eventsCollection.document(document.id).delete().await()
                deleteFromEventUser(document.id)
                deleteFromEventArtist(document.id)
            }

            println("All matching documents successfully deleted.")
            loadMyEvents()
        } catch (e: Exception) {
            println("Error deleting documents: ${e.message}")
        }
    }


}
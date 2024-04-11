package com.example.bend.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bend.Constants
import com.example.bend.Notifications
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
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

    private val eventsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event")
    private val eventArtistCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event_artist")
    private val userEventCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("user_event")

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
        suspend fun getFounderEvents(userID: String): List<Event> {
            try {
                val task = FirebaseFirestore.getInstance().collection("event").whereEqualTo("founderUUID", userID).get().await()
                return task.toObjects(Event::class.java)
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }

            return emptyList()
        }
        suspend fun getArtistEvents(userID: String): List<Event> {
            val events = mutableListOf<Event>()

            try {
                val eventArtistRecords =
                    FirebaseFirestore.getInstance().collection("event_artist").whereEqualTo("artistUUID", userID).get().await()
                for (record in eventArtistRecords) {
                    val eventUUID = record.getString("eventUUID")
                    val eventRecords = FirebaseFirestore.getInstance().collection("event").whereEqualTo("uuid", eventUUID).get().await()
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
        suspend fun getAccountType(userUUID: String): String = coroutineScope {
            try {
                val artistSnapshotDeferred =
                    async { FirebaseFirestore.getInstance().collection("artist").document(userUUID).get().await() }
                val founderSnapshotDeferred =
                    async { FirebaseFirestore.getInstance().collection("event_founder").document(userUUID).get().await() }
                val userSnapshotDeferred = async { FirebaseFirestore.getInstance().collection("user").document(userUUID).get().await() }

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
        suspend fun checkReviewExists(eventUUID: String): Boolean {
            val db = FirebaseFirestore.getInstance()

            return try {
                val querySnapshot = db.collection("review")
                    .whereEqualTo("eventUUID", eventUUID)
                    .whereEqualTo("writerUUID", FirebaseAuth.getInstance().currentUser?.uid)
                    .get()
                    .await()

                !querySnapshot.isEmpty
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun removeEvent(event: Event) {
        when (accountType.value) {
            "artist" -> {
                deleteFromEventArtist(event, currentUser?.uid)
            }

            "user" -> {
                deleteFromEventUser(event.uuid, currentUser?.uid)
            }

            "event_founder" -> {
                Log.e("INSIDE event founder delete", event.uuid)

                deleteEvent(event, currentUser?.uid)
            }
        }

    }

    private suspend fun deleteFromEventArtist(event: Event, artistUUID: String? = null){
        try {
            val query = if (artistUUID != null) {
                eventArtistCollection
                    .whereEqualTo("artistUUID", artistUUID)
                    .whereEqualTo("eventUUID", event.uuid)
            } else {
                eventArtistCollection
                    .whereEqualTo("eventUUID", event.uuid)
            }

            val documents = query.get().await()

            documents.forEach { document ->
                eventArtistCollection.document(document.id).delete().await()
            }
            viewModelScope.launch(Dispatchers.IO) {
                Notifications.notifySingleUser(
                    toUserUUID = event.founderUUID,
                    fromUser = artistUUID!!,
                    event = event,
                    notificationText = Constants.ARTIST_NO_MORE_PERFORM_TO_ORGANIZER_EVENT,
                    sensitive = true
                )
                Notifications.notifyAllAttendees(
                    event = event,
                    notificationText = Constants.ARTIST_NO_MORE_PERFORM,
                    sensitive = true
                )
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

    private suspend fun deleteEvent(event: Event, userUUID: String?) {
        try {
            val documents = eventsCollection
                .whereEqualTo("founderUUID", userUUID)
                .whereEqualTo("uuid", event.uuid)
                .get()
                .await()

            documents.forEachIndexed() {i , document ->
                eventsCollection.document(document.id).delete().await()
                if (i == 0){
                    viewModelScope.launch(Dispatchers.IO) {
                        Notifications.notifyAllAttendees(
                            event = event,
                            notificationText = Constants.DELETED_EVENT,
                            sensitive = true
                        )
                        Notifications.notifyArtistsOfEvent(
                            event = event,
                            notificationText = Constants.DELETED_EVENT_FOR_ARTISTS,
                            sensitive = true
                        )
                    }
                }
                deleteFromEventUser(document.id)
                deleteFromEventArtist(document.toObject(Event::class.java))
            }
            println("All matching documents successfully deleted.")
            loadMyEvents()
        } catch (e: Exception) {
            println("Error deleting documents: ${e.message}")
        }
    }


}
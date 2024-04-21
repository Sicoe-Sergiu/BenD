package com.example.bend.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bend.Constants
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
    private val TAG = MyEventsViewModel::class.simpleName

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
    val errorMessages: LiveData<String> = MutableLiveData()

    init {
        loadMyEvents()
    }

    fun loadMyEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "loading my events...")

            val accountTypeDeferred = async { HomeViewModel.getAccountType(null, currentUser?.uid.toString()) }
            val accountTypeValue = accountTypeDeferred.await()

            val eventsDeferred = when (accountTypeValue) {
                "user" -> async { getRegularUserEvents(currentUser?.uid ?: "") }
                "artist" -> async { getArtistEvents(null, currentUser?.uid ?: "") }
                "event_founder" -> async { getFounderEvents(null, currentUser?.uid ?: "") }
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




    companion object {
        suspend fun getFounderEvents(context: Context?, userID: String): List<Event> {
            try {
                val task = FirebaseFirestore.getInstance().collection("event").whereEqualTo("founderUUID", userID).get().await()
                return task.toObjects(Event::class.java)
            } catch (e: Exception) {
                Log.e("getFounderEvents: ", "Error fetching founder events for user ID: $userID", e)
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

        suspend fun getArtistEvents(context: Context?, userID: String): List<Event> {
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
                Log.e("getArtistEvents: ", "Error fetching artist events for user ID: $userID", e)
                e.printStackTrace()
                if (context != null) {
                    Toast.makeText(
                        context,
                        e.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            return events
        }
        suspend fun checkReviewExists(context: Context?, eventUUID: String): Boolean {
            val db = FirebaseFirestore.getInstance()

            return try {
                val querySnapshot = db.collection("review")
                    .whereEqualTo("eventUUID", eventUUID)
                    .whereEqualTo("writerUUID", FirebaseAuth.getInstance().currentUser?.uid)
                    .get()
                    .await()

                !querySnapshot.isEmpty
            } catch (e: Exception) {
                Log.e("checkReviewExists: ", "Error checking if review exists for event UUID: $eventUUID", e)
                e.printStackTrace()
                if (context != null) {
                    Toast.makeText(
                        context,
                        e.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
                false
            }
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
            Log.d(TAG, e.printStackTrace().toString())
            postError(e.localizedMessage ?: "Fetch events error:")
            e.printStackTrace()
        }

        return events
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

    private suspend fun deleteFromEventArtist(event: Event, artistUUID: String? = null) {
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
                try {
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
                } catch (e: Exception) {
                    Log.e(TAG, "Error in sending notifications: ${e.localizedMessage}")
                    postError("Notification error: ${e.localizedMessage ?: "unknown error"}")
                }
            }

            Log.d(TAG, "All matching documents successfully deleted.")
            loadMyEvents()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting documents: ${e.localizedMessage}")
            postError(e.localizedMessage ?: "Delete event error")
        }
    }

    private suspend fun deleteFromEventUser(eventUUID: String, userUUID: String? = null) {
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

            Log.d(TAG, "All matching documents successfully deleted.")
            loadMyEvents()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting documents: ${e.localizedMessage}")
            postError(e.localizedMessage ?: "Delete event error:")
        }
    }

    private suspend fun deleteEvent(event: Event, userUUID: String?) {
        try {
            val documents = eventsCollection
                .whereEqualTo("founderUUID", userUUID)
                .whereEqualTo("uuid", event.uuid)
                .get()
                .await()

            documents.forEachIndexed { i, document ->
                try {
                    eventsCollection.document(document.id).delete().await()
                    if (i == 0) {
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
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
                            } catch (e: Exception) {
                                Log.e(TAG, "Error sending notifications: ${e.localizedMessage}")
                                postError("Notification error: ${e.localizedMessage ?: "unknown error"}")
                            }
                        }
                    }
                    deleteFromEventUser(event.uuid, null)  // Assuming correct signature and context
                    deleteFromEventArtist(event, null)     // Assuming correct signature and context
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting event document: ${e.localizedMessage}")
                    postError("Document deletion error: ${e.localizedMessage ?: "unknown error"}")
                }
            }
            Log.d(TAG, "All matching documents successfully deleted.")
            loadMyEvents()
        } catch (e: Exception) {
            Log.e(TAG, "Error querying documents: ${e.localizedMessage}")
            postError(e.localizedMessage ?: "Error querying documents.")
        }
    }

    private fun postError(message: String) {
        (errorMessages as MutableLiveData).postValue(message)
    }

    fun clearError() {
        (errorMessages as MutableLiveData).postValue("")
    }

}
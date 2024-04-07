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
import com.example.bend.model.Notification
import com.example.bend.model.User
import com.example.bend.model.UserEvent
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
    private val notificationCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("notification")

    var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    var events: LiveData<List<Event>> = MutableLiveData(emptyList())
    private var artists: LiveData<List<Artist>> = MutableLiveData(emptyList())
    private var founders: LiveData<List<EventFounder>> = MutableLiveData(emptyList())
    private var eventArtists: LiveData<List<EventArtist>> = MutableLiveData(emptyList())
    var eventsAttendees: LiveData<List<Pair<Event, Int>>> = MutableLiveData(emptyList())
    var accountType: LiveData<String> = MutableLiveData("")

    var homeScreenScrollState: LazyListState by mutableStateOf(LazyListState(0, 0))

    val operationCompletedMessage = MutableLiveData<String?>()

    var newNotifications: LiveData<Boolean> = MutableLiveData(false)
    var notifications: LiveData<List<Notification>> = MutableLiveData(emptyList()) // emptyList()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadData()
        }
    }


    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "loading data...")

            val fetchArtistsDeferred = async { fetchArtists() }
            val fetchEventsDeferred = async { fetchEvents() }
            val fetchEventFoundersDeferred = async { fetchEventFounders() }
            val fetchEventArtistsDeferred = async { fetchEventArtists() }
            val accountTypeDeferred = async { getAccountType(currentUser?.uid.toString()) }
            val fetchNotificationsDeferred = async { fetchNotifications() }

            awaitAll(
                fetchArtistsDeferred,
                fetchEventsDeferred,
                fetchEventFoundersDeferred,
                fetchEventArtistsDeferred,
                accountTypeDeferred,
                fetchNotificationsDeferred
            )

            withContext(Dispatchers.Main) {
                (accountType as MutableLiveData).postValue(accountTypeDeferred.await())
            }

            Log.d(TAG, "loading data DONE")
            _isLoading.value = false
        }
    }
    suspend fun getAccountType(userUUID: String): String = coroutineScope {
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

    private suspend fun fetchArtists() {
        try {
            val artistsList = artistsCollection.get().await().toObjects(Artist::class.java)
            (artists as MutableLiveData).postValue(artistsList)
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching artists: $exception")
        }
    }
    private fun fetchNotifications() {
        val notificationsListener = notificationCollection
            .whereEqualTo("toUserUUID", currentUser?.uid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error listening for notification changes: $exception")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val localNotifications = snapshot.toObjects(Notification::class.java)
                    (notifications as MutableLiveData).postValue(localNotifications)
                    Log.d(TAG, "Notifications updated: $localNotifications")
                } else {
                    Log.e(TAG, "No notifications found")
                }
            }
    }

    private suspend fun fetchEvents() {
        try {
            val eventsList = eventsCollection.get().await().toObjects(Event::class.java)
            (events as MutableLiveData).postValue(eventsList)
            fetchEventAttendees(eventsList) // Call to fetch attendees
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching events: $exception")
        }
    }

    private suspend fun fetchEventAttendees(eventsList: List<Event>) {
        try {
            val eventTasks = eventsList.map { event ->
                userEventCollection.whereEqualTo("eventUUID", event.uuid).get().await()
            }

            val attendees = eventsList.zip(eventTasks.map { it.size() })
            (eventsAttendees as MutableLiveData).postValue(attendees)
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching attendees", exception)
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

    private suspend fun fetchEventArtists() {
        try {
            val eventArtistList =
                eventArtistCollection.get().await().toObjects(EventArtist::class.java)
            (eventArtists as MutableLiveData).postValue(eventArtistList)
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching event artists: $exception")
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

    fun addEventToUserList(event: Event) = viewModelScope.launch {
        try {
            val user = userCollection.whereEqualTo("uuid", currentUser?.uid).get().await()
                .toObjects(User::class.java).firstOrNull()
            user?.let { user ->
                val userEvent = UserEvent(UUID.randomUUID().toString(), user.uuid, event.uuid)
                userEventCollection.document(userEvent.uuid).set(userEvent).await()
                updateAttendeesCountForEvent(event, increment = true)
            }
            operationCompletedMessage.postValue("Event added to your list.")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding event to user list: $e")
            operationCompletedMessage.postValue("Error adding event to user list.")
        }
    }

    fun removeEventFromUserList(event: Event) = viewModelScope.launch {
        try {
            val documents = userEventCollection
                .whereEqualTo("userUUID", currentUser?.uid)
                .whereEqualTo("eventUUID", event.uuid).get().await()

            documents.forEach { document ->
                document.reference.delete().await()
            }
            updateAttendeesCountForEvent(event, increment = false)
            operationCompletedMessage.postValue("Event removed from your list.")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing event from user list: $e")
            operationCompletedMessage.postValue("Error removing event from user list.")

        }
    }

    suspend fun ifAttend(event: Event): Boolean {
        return try {
            val eventDocuments = userEventCollection
                .whereEqualTo("eventUUID", event.uuid)
                .whereEqualTo("userUUID", currentUser?.uid)
                .get()
                .await()

            !eventDocuments.isEmpty
        } catch (e: Exception) {
            // TODO: Handle exception (e.g., log, report, or throw)
            false
        }
    }

    companion object {
        suspend fun getEventByUUID(eventUUID: String): Event? {
            try {
                val task = FirebaseFirestore.getInstance().collection("event").whereEqualTo("uuid", eventUUID).get().await()

                val events = task.toObjects(Event::class.java)
                return events.first()
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
            return null
        }
        suspend fun getUserByUUID(userUUID: String): User? {
            try {
                val task = FirebaseFirestore.getInstance().collection("user").whereEqualTo("uuid", userUUID).get().await()

                val users = task.toObjects(User::class.java)
                return users.first()
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
            return null
        }
        suspend fun getFounderByUUID(founderUUID: String): EventFounder? {
            try {
                val task = FirebaseFirestore.getInstance().collection("event_founder").whereEqualTo("uuid", founderUUID).get().await()

                val founders = task.toObjects(EventFounder::class.java)
                return founders.first()
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
            return null
        }
        suspend fun getArtistByUUID(artistUUID: String): Artist? {
            try {
                val task = FirebaseFirestore.getInstance().collection("artist").whereEqualTo("uuid", artistUUID).get().await()

                val artists = task.toObjects(Artist::class.java)
                return artists.first()
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
            return null
        }
    }

    fun repostEvent(event: Event) {
        // TODO: Implement reposting logic
    }
    fun getTimeDifferenceDisplay(timestamp: Long): String {
        val timeDiffMillis = System.currentTimeMillis() - timestamp
        val timeDiffHours = timeDiffMillis / (1000 * 60 * 60)
        val timeDiffDays = timeDiffHours / 24

        return when {
            timeDiffHours < 24 -> "${timeDiffHours}h"
            else -> "${timeDiffDays}d"
        }
    }

}
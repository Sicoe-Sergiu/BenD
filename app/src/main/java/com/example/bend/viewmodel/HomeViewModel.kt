package com.example.bend.viewmodel

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bend.Constants
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventArtist
import com.example.bend.model.EventFounder
import com.example.bend.model.Notification
import com.example.bend.model.Repost
import com.example.bend.model.User
import com.example.bend.model.UserEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class HomeViewModel : ViewModel() {

    private val TAG = "HOME VIEW MODEL"
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser

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

    val isLoading: LiveData<Boolean> = MutableLiveData(false)

    var events: LiveData<List<Pair<String?, Event>>> = MutableLiveData(emptyList())
    var artists: LiveData<List<Artist>> = MutableLiveData(emptyList())
    var founders: LiveData<List<EventFounder>> = MutableLiveData(emptyList())
    var eventArtists: LiveData<List<EventArtist>> = MutableLiveData(emptyList())
    var eventsAttendees: LiveData<List<Pair<Event, Int>>> = MutableLiveData(emptyList())
    var accountType: LiveData<String> = MutableLiveData("")

    var homeScreenScrollState: LazyListState by mutableStateOf(LazyListState(0, 0))

    val operationCompletedMessage = MutableLiveData<String?>()

    var newNotifications: LiveData<List<Notification>> = MutableLiveData(emptyList())
    var notifications: LiveData<List<Notification>> = MutableLiveData(emptyList())

    init {
        loadEvents()
    }
    companion object {
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
        suspend fun getEventByUUID(eventUUID: String): Event? {
            try {
                val task = FirebaseFirestore.getInstance().collection("event")
                    .whereEqualTo("uuid", eventUUID).get().await()

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
                val task = FirebaseFirestore.getInstance().collection("user")
                    .whereEqualTo("uuid", userUUID).get().await()

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
                val task = FirebaseFirestore.getInstance().collection("event_founder")
                    .whereEqualTo("uuid", founderUUID).get().await()

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
                val task = FirebaseFirestore.getInstance().collection("artist")
                    .whereEqualTo("uuid", artistUUID).get().await()

                val artists = task.toObjects(Artist::class.java)
                return artists.first()
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
            return null
        }
        suspend fun getEventArtistsFromFirebase(event: Event): List<Artist> {
            val db = FirebaseFirestore.getInstance()
            val artists = mutableListOf<Artist>()

            try {
                val eventArtists = db.collection("event_artist")
                    .whereEqualTo("eventUUID", event.uuid)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(EventArtist::class.java) }
                    .map { it.artistUUID }

                eventArtists.forEach { artistUUID ->
                    val artist = db.collection("artist")
                        .document(artistUUID)
                        .get()
                        .await()
                        .toObject(Artist::class.java)

                    artist?.let { artists.add(it) }
                }
            } catch (e: Exception) {
                println("Error fetching artists for event: ${event.uuid}, ${e.message}")
            }

            return artists
        }


    }

    fun loadEvents(){
        viewModelScope.launch(Dispatchers.IO) {
            fetchEvents()
        }
    }
    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val fetchEventFoundersDeferred = async { fetchEventFounders() }
            val fetchEventArtistsDeferred = async { fetchEventArtists() }
            val accountTypeDeferred = async { getAccountType(currentUser?.uid.toString()) }
            val fetchNotificationsDeferred = async { fetchNotifications() }

            awaitAll(
                fetchEventFoundersDeferred,
                fetchEventArtistsDeferred,
                accountTypeDeferred,
                fetchNotificationsDeferred
            )

            withContext(Dispatchers.Main) {
                (accountType as MutableLiveData).postValue(accountTypeDeferred.await())
            }

            Log.d(TAG, "loading data DONE")
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
                    (newNotifications as MutableLiveData).postValue(localNotifications.filter { !it.seen })
                    Log.d(TAG, "Notifications updated: $localNotifications")
                } else {
                    Log.e(TAG, "No notifications found")
                }
            }
    }

    private suspend fun fetchEvents() {
        val db = FirebaseFirestore.getInstance()
        val userUUID = currentUser?.uid ?: return

        try {
            val followedUUIDs = withContext(Dispatchers.IO) {
                try {
                    db.collection("followers")
                        .whereEqualTo("userUUID", userUUID)
                        .get()
                        .await()
                        .documents
                        .mapNotNull { it["followedUserUUID"] as? String }
                        .toSet()
                } catch (e: Exception) {
                    Log.e("FETCH ERROR", "Error fetching followed UUIDs: $e")
                    emptySet<String>()
                }
            }

            val organizedEvents = mutableListOf<Event>()
            val founderEvents = withContext(Dispatchers.IO) {
                try {
                    db.collection("event")
                        .whereIn("founderUUID", followedUUIDs.toList())
                        .get()
                        .await()
                        .documents
                        .mapNotNull { it.toObject(Event::class.java) }
                } catch (e: Exception) {
                    Log.e("FETCH ERROR", "Error fetching founder events: $e")
                    emptyList<Event>()
                }
            }
            organizedEvents.addAll(founderEvents)

            val artistEventUUIDs = withContext(Dispatchers.IO) {
                try {
                    db.collection("event_artist")
                        .whereIn("artistUUID", followedUUIDs.toList())
                        .get()
                        .await()
                        .documents
                        .mapNotNull { it["eventUUID"] as? String }
                        .toSet()
                } catch (e: Exception) {
                    Log.e("FETCH ERROR", "Error fetching artist event UUIDs: $e")
                    emptySet<String>()
                }
            }

            val artistEvents = withContext(Dispatchers.IO) {
                try {
                    db.collection("event")
                        .whereIn(FieldPath.documentId(), artistEventUUIDs.toList())
                        .get()
                        .await()
                        .documents
                        .mapNotNull { it.toObject(Event::class.java) }
                } catch (e: Exception) {
                    Log.e("FETCH ERROR", "Error fetching artist events: $e")
                    emptyList<Event>()
                }
            }
            organizedEvents.addAll(artistEvents.filter { it.uuid !in organizedEvents.map { event -> event.uuid } })

            val reposts = withContext(Dispatchers.IO) {
                try {
                    db.collection("repost")
                        .whereIn("userUUID", followedUUIDs.toList())
                        .get()
                        .await()
                        .documents
                        .mapNotNull { it.toObject(Repost::class.java) }
                } catch (e: Exception) {
                    Log.e("FETCH ERROR", "Error fetching reposts: $e")
                    emptyList<Repost>()
                }
            }

            val repostedEvents = withContext(Dispatchers.IO) {
                try {
                    db.collection("event")
                        .whereIn("uuid", reposts.map { it.eventUUID })
                        .get()
                        .await()
                        .documents
                        .mapNotNull { document ->
                            document.toObject(Event::class.java)?.apply {
                                creationTimestamp = reposts.associate { it.eventUUID to it.timestamp }[uuid] ?: creationTimestamp
                            }
                        }
                } catch (e: Exception) {
                    Log.e("FETCH ERROR", "Error fetching reposted events: $e")
                    emptyList<Event>()
                }
            }

            val eventsPair = organizedEvents.map { Pair(null, it) }
            val repostedEventsList = repostedEvents.map { event ->
                Pair(reposts.associate { it.eventUUID to it.userUUID }[event.uuid]!!, event)
            }
            val sortedEventsList = (eventsPair + repostedEventsList).sortedBy { it.second.creationTimestamp }

            withContext(Dispatchers.Main) {
                (events as MutableLiveData).postValue(sortedEventsList)
                delay(500)
                loadData()
            }
            viewModelScope.launch(Dispatchers.IO) {
                fetchEventAttendees(sortedEventsList.map { it.second })
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Error in overall event fetching process: $exception")
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
                eventFounderCollection.whereIn("uuid", events.value!!.map{ it.second.founderUUID }).get().await().toObjects(EventFounder::class.java)
            (founders as MutableLiveData).postValue(eventFoundersList)


        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching founders: $exception")
        }
    }
    private suspend fun fetchEventArtists() {
        try {
            val eventArtistList =
                eventArtistCollection.whereIn("eventUUID", events.value!!.map{ it.second.uuid }).get().await().toObjects(EventArtist::class.java)
            (eventArtists as MutableLiveData).postValue(eventArtistList)
            fetchArtists(eventArtistList.map { it.artistUUID })
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching event artists: $exception")
        }
    }
    private suspend fun fetchArtists(artistsUUIDs: List<String>) {
        try {
            val artistsList = artistsCollection
                .whereIn("uuid", artistsUUIDs)
                .get().await().toObjects(Artist::class.java)
            (artists as MutableLiveData).postValue(artistsList)
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching artists: $exception")
        }
    }

    fun getEventArtists(event: Event): List<Artist> {
        val eventArtists = eventArtists.value?.filter { it.eventUUID == event.uuid } ?: emptyList()
        val artistsUUIDS = eventArtists.map { it.artistUUID }
        return artists.value?.filter { it.uuid in artistsUUIDS } ?: emptyList()
    }

    private fun updateAttendeesCountForEvent(event: Event, increment: Boolean) {
        val currentList = eventsAttendees.value.orEmpty()
        val updatedList = currentList.map {
            if (it.first.uuid == event.uuid) {
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
                Notifications.notifyAllFollowers(user.uuid, event, Constants.FOLLOWED_USER_ATTEND)
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

    fun repostEvent(event: Event) {
        val repost = Repost(UUID.randomUUID().toString(), currentUser!!.uid, event.uuid, System.currentTimeMillis())

        viewModelScope.launch(Dispatchers.IO) {
            try {
                FirebaseFirestore.getInstance().collection("repost")
                    .document(repost.uuid).set(repost).await()
                Notifications.notifyAllFollowers(
                    userUUID = currentUser.uid,
                    event = event,
                    notificationText = Constants.EVENT_REPOST
                    )
                Notifications.notifySingleUser(
                    fromUser = currentUser.uid,
                    toUserUUID = event.founderUUID,
                    event = event,
                    notificationText = Constants.FOUNDER_EVENT_REPOST
                )
            }catch (e: Exception){
                Log.d("Repost error", e.printStackTrace().toString())
            }
        }
    }

    fun getTimeDifferenceDisplay(timestamp: Long): String {
        val timeDiffMillis = System.currentTimeMillis() - timestamp
        val timeDiffMinutes = timeDiffMillis / (1000 * 60)
        val timeDiffHours = timeDiffMinutes / 60
        val timeDiffDays = timeDiffHours / 24

        return when {
            timeDiffMinutes < 60 -> "${timeDiffMinutes}m"
            timeDiffHours < 24 -> "${timeDiffHours}h"
            else -> "${timeDiffDays}d"
        }
    }


    fun seeNotification(notificationUUID: String) {

        FirebaseFirestore
            .getInstance()
            .collection("notification")
            .document(notificationUUID)
            .update("seen", true)
            .addOnSuccessListener {
                // Handle success (optional)
                println("Notification successfully updated")
            }
            .addOnFailureListener { e ->
                // Handle failure (optional)
                e.printStackTrace()
            }

    }

}
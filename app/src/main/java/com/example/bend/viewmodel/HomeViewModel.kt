package com.example.bend.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.google.firebase.firestore.DocumentChange
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

class HomeViewModel(
    private val appContext: Context?,
) : ViewModel() {

    private val TAG = HomeViewModel::class.simpleName

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

    //    var eventsAttendees: LiveData<List<Pair<Event, Int>>> = MutableLiveData(emptyList())
    var accountType: LiveData<String> = MutableLiveData("")
    var userEvent: LiveData<List<UserEvent>> = MutableLiveData(emptyList())

    var homeScreenScrollState: LazyListState by mutableStateOf(LazyListState(0, 0))

    val operationCompletedMessage = MutableLiveData<String?>()

    var newNotifications: LiveData<List<Notification>> = MutableLiveData(emptyList())
    var notifications: LiveData<List<Notification>> = MutableLiveData(emptyList())

    val errorMessages: LiveData<String> = MutableLiveData()
    var userEventsInitialized = false

    init {
        (isLoading as MutableLiveData).postValue(true)
        loadEvents()
        notificationCollection
            .whereEqualTo("toUserUUID", currentUser?.uid)
            .addSnapshotListener { snapshot, exception ->
                try {
                    if (exception != null) {
                        throw exception
                    }

                    if (snapshot != null) {
                        val changes = snapshot.documentChanges.toSet()

                        for (change in changes) {

                            if (change.type == DocumentChange.Type.ADDED) {
                                val notification =
                                    change.document.toObject(Notification::class.java)
                                if (!notification.seen) {
                                    Log.d("CHANGES", change.document.toString())

                                    viewModelScope.launch {
                                        var userUsername = ""

                                        when (getAccountType(null, notification.fromUserUUID)) {
                                            "user" -> {
                                                val user =
                                                    getUserByUUID(null, notification.fromUserUUID)
                                                userUsername = user?.username ?: ""
                                            }

                                            "event_founder" -> {
                                                val user = getFounderByUUID(
                                                    null,
                                                    notification.fromUserUUID
                                                )
                                                userUsername = user?.username ?: ""
                                            }

                                            "artist" -> {
                                                val user =
                                                    getArtistByUUID(null, notification.fromUserUUID)
                                                userUsername = user?.username ?: ""
                                            }
                                        }

                                        val formattedText = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append(userUsername)
                                            }
                                            append(" " + notification.text + "\n")
                                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                                append(getTimeDifferenceDisplay(notification.timestamp))
                                            }
                                        }

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                appContext,
                                                formattedText,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }

                                }
                            }
                        }

                        if (!snapshot.isEmpty) {
                            val localNotifications = snapshot.toObjects(Notification::class.java)

                            (notifications as MutableLiveData).postValue(localNotifications)
                            (newNotifications as MutableLiveData).postValue(localNotifications.filter { !it.seen })

                            Log.d(TAG, "Notifications updated: $localNotifications")
                        }
                    } else {
                        Log.e(TAG, "No notifications found")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing notifications: $e")
                    postError("Error processing notifications: ${e.localizedMessage}")
                }
            }
    }

    companion object {
        suspend fun getAccountType(context: Context? = null, userUUID: String): String =
            coroutineScope {
                try {
                    val artistSnapshotDeferred = async {
                        FirebaseFirestore.getInstance().collection("artist").document(userUUID)
                            .get().await()
                    }
                    val founderSnapshotDeferred = async {
                        FirebaseFirestore.getInstance().collection("event_founder")
                            .document(userUUID).get().await()
                    }
                    val userSnapshotDeferred = async {
                        FirebaseFirestore.getInstance().collection("user").document(userUUID).get()
                            .await()
                    }

                    val (artistSnapshot, founderSnapshot, userSnapshot) = awaitAll(
                        artistSnapshotDeferred,
                        founderSnapshotDeferred,
                        userSnapshotDeferred
                    )

                    return@coroutineScope when {
                        artistSnapshot.exists() -> "artist"
                        founderSnapshot.exists() -> "event_founder"
                        userSnapshot.exists() -> "user"
                        else -> "unknown"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (context != null) {
                            Toast.makeText(
                                context,
                                "Error getting the account type: ${e.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    return@coroutineScope "error"
                }
            }

        suspend fun getEventByUUID(context: Context?, eventUUID: String): Event? {
            return try {
                val task = FirebaseFirestore.getInstance().collection("event")
                    .whereEqualTo("uuid", eventUUID).get().await()
                val events = task.toObjects(Event::class.java)
                events.firstOrNull()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (context != null) {
                        Toast.makeText(
                            context,
                            "Failed to fetch event: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                null
            }
        }

        suspend fun getUserByUUID(context: Context?, userUUID: String): User? {
            return try {
                val task = FirebaseFirestore.getInstance().collection("user")
                    .whereEqualTo("uuid", userUUID).get().await()
                val users = task.toObjects(User::class.java)
                users.firstOrNull()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (context != null) {
                        Toast.makeText(
                            context,
                            "Failed to fetch user: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                null
            }
        }


        suspend fun getFounderByUUID(context: Context?, founderUUID: String): EventFounder? {
            return try {
                val task = FirebaseFirestore.getInstance().collection("event_founder")
                    .whereEqualTo("uuid", founderUUID).get().await()
                val founders = task.toObjects(EventFounder::class.java)
                founders.firstOrNull()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (context != null) {
                        Toast.makeText(
                            context,
                            "Failed to fetch founder: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                null
            }
        }


        suspend fun getArtistByUUID(context: Context?, artistUUID: String): Artist? {
            return try {
                val task = FirebaseFirestore.getInstance().collection("artist")
                    .whereEqualTo("uuid", artistUUID).get().await()
                val artists = task.toObjects(Artist::class.java)
                artists.firstOrNull()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (context != null) {
                        Toast.makeText(
                            context,
                            "Failed to fetch artist: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                null
            }
        }


        suspend fun getEventArtistsFromFirebase(context: Context?, event: Event): List<Artist> {
            val db = FirebaseFirestore.getInstance()
            val artists = mutableListOf<Artist>()
            return try {
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
                artists
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (context != null) {
                        Toast.makeText(
                            context,
                            "Error fetching artists for event: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                emptyList()
            }
        }

    }

    fun loadEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            if (appContext?.let { isNetworkAvailable(it) } == true){
                fetchEvents()
            }else{
                postError("You're offline right now. Please check your internet connection and try again.")
            }
        }
    }
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {

            val fetchEventFoundersDeferred = async { fetchEventFounders() }
            val fetchEventArtistsDeferred = async { fetchEventArtists() }
            val accountTypeDeferred = async { getAccountType(null, currentUser?.uid.toString()) }

            awaitAll(
                fetchEventFoundersDeferred,
                fetchEventArtistsDeferred,
                accountTypeDeferred,
            )
            (isLoading as MutableLiveData).postValue(false)

            withContext(Dispatchers.Main) {
                (accountType as MutableLiveData).postValue(accountTypeDeferred.await())
            }

            Log.d(TAG, "loading data DONE")
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
                    val errorMessage = e.localizedMessage ?: "Failed to fetch followed UUIDs."
                    Log.e(TAG, "Error fetching followed UUIDs: $errorMessage")
                    postError("Error fetching followed UUIDs: $errorMessage")
                    return@withContext emptySet<String>()
                }
            }

            val organizedEvents = mutableListOf<Event>()
            val founderEvents = withContext(Dispatchers.IO) {
                try {
                    if (followedUUIDs.isNotEmpty()) {
                        db.collection("event")
                            .whereIn("founderUUID", followedUUIDs.toList())
                            .get()
                            .await()
                            .documents
                            .mapNotNull { it.toObject(Event::class.java) }
                    } else {
                        return@withContext emptyList<Event>()
                    }
                } catch (e: Exception) {
                    val errorMessage = e.localizedMessage ?: "Failed to fetch founder events."
                    Log.e(TAG, "Error fetching founder events: $errorMessage")
                    postError("Error fetching founder events: $errorMessage")
                    return@withContext emptyList<Event>()
                }
            }
            organizedEvents.addAll(founderEvents)

            val artistEventUUIDs = withContext(Dispatchers.IO) {
                try {
                    if (followedUUIDs.isNotEmpty()) {
                        db.collection("event_artist")
                            .whereIn("artistUUID", followedUUIDs.toList())
                            .get()
                            .await()
                            .documents
                            .mapNotNull { it["eventUUID"] as? String }
                            .toSet()
                    } else {
                        return@withContext emptySet<String>()
                    }
                } catch (e: Exception) {
                    val errorMessage = e.localizedMessage ?: "Failed to fetch artist event UUIDs."
                    Log.e(TAG, "Error fetching artist event UUIDs: $errorMessage")
                    postError("Error fetching artist event UUIDs: $errorMessage")
                    return@withContext emptySet<String>()
                }
            }

            val artistEvents = withContext(Dispatchers.IO) {
                try {
                    if (artistEventUUIDs.isNotEmpty()) {
                        db.collection("event")
                            .whereIn(FieldPath.documentId(), artistEventUUIDs.toList())
                            .get()
                            .await()
                            .documents
                            .mapNotNull { it.toObject(Event::class.java) }
                    } else {
                        return@withContext emptyList<Event>()
                    }
                } catch (e: Exception) {
                    val errorMessage = e.localizedMessage ?: "Failed to fetch artist events."
                    Log.e(TAG, "Error fetching artist events: $errorMessage")
                    postError("Error fetching artist events: $errorMessage")
                    return@withContext emptyList<Event>()
                }
            }
            organizedEvents.addAll(artistEvents.filter { it.uuid !in organizedEvents.map { event -> event.uuid } })

            val reposts = withContext(Dispatchers.IO) {
                try {
                    if (followedUUIDs.isNotEmpty()) {
                        db.collection("repost")
                            .whereIn("userUUID", followedUUIDs.toList())
                            .get()
                            .await()
                            .documents
                            .mapNotNull { it.toObject(Repost::class.java) }
                    } else {
                        return@withContext emptyList<Repost>()
                    }
                } catch (e: Exception) {
                    val errorMessage = e.localizedMessage ?: "Failed to fetch reposts."
                    Log.e(TAG, "Error fetching reposts: $errorMessage")
                    postError("Error fetching reposts: $errorMessage")
                    return@withContext emptyList<Repost>()
                }
            }

            val repostedEvents = withContext(Dispatchers.IO) {
                try {
                    if (reposts.isNotEmpty()) {
                        db.collection("event")
                            .whereIn("uuid", reposts.map { it.eventUUID })
                            .get()
                            .await()
                            .documents
                            .mapNotNull { document ->
                                document.toObject(Event::class.java)?.apply {
                                    creationTimestamp =
                                        reposts.associate { it.eventUUID to it.timestamp }[uuid]
                                            ?: creationTimestamp
                                }
                            }
                    } else {
                        return@withContext emptyList<Event>()
                    }
                } catch (e: Exception) {
                    val errorMessage = e.localizedMessage ?: "Failed to fetch reposted events."
                    Log.e(TAG, "Error fetching reposted events: $errorMessage")
                    postError("Error fetching reposted events: $errorMessage")
                    return@withContext emptyList<Event>()
                }
            }

            val eventsPair = organizedEvents.map { Pair(null, it) }
            val repostedEventsList = repostedEvents.map { event ->
                Pair(reposts.associate { it.eventUUID to it.userUUID }[event.uuid]!!, event)
            }
            val sortedEventsList =
                (eventsPair + repostedEventsList).sortedByDescending { it.second.creationTimestamp }

            withContext(Dispatchers.Main) {
                (events as MutableLiveData).postValue(sortedEventsList)
                delay(500)
                loadData()
            }

            if(!userEventsInitialized){
                Log.e("USER EVENT INIT", "111")
                userEventCollection
                    .whereIn("eventUUID", sortedEventsList.map { it.second.uuid })
                    .addSnapshotListener { snapshot, exception ->
                        try {
                            if (exception != null) {
                                throw exception
                            }
                            if (snapshot != null) {
                                val localUserEvent = snapshot.toObjects(UserEvent::class.java)
                                (userEvent as MutableLiveData).postValue(localUserEvent)
                                userEventsInitialized = true
                            }

                        } catch (exception: Exception) {
                            val errorMessage =
                                "Error setting up listeners for event attendees: ${exception.localizedMessage ?: "Unknown error"}"
                            Log.e(TAG, errorMessage, exception)
                            postError(errorMessage)
                        }
                    }
            }

        } catch (exception: Exception) {
            val overallError =
                exception.localizedMessage ?: "Error in overall event fetching process."
            Log.e(TAG, "Error in overall event fetching process: $overallError")
            postError("Error in overall event fetching process: $overallError")
        }
    }

    private suspend fun fetchEventFounders() {
        try {
            val currentEvents =
                events.value ?: throw IllegalStateException("Events data not available")

            if (events.value?.isNotEmpty() == true) {
                val eventFoundersList =
                    eventFounderCollection.whereIn(
                        "uuid",
                        currentEvents.map { it.second.founderUUID })
                        .get().await().toObjects(EventFounder::class.java)

                withContext(Dispatchers.Main) {
                    (founders as MutableLiveData).postValue(eventFoundersList)
                }
            }
        } catch (exception: Exception) {
            val errorMessage =
                "Error fetching event founders: ${exception.localizedMessage ?: "Unknown error"}"
            Log.e(TAG, errorMessage, exception)
            postError(errorMessage)
        }
    }

    private suspend fun fetchEventArtists() {
        try {
            val currentEvents =
                events.value ?: throw IllegalStateException("Events data not available")
            if (events.value!!.isNotEmpty()) {
                val eventArtistList =
                    eventArtistCollection.whereIn("eventUUID", currentEvents.map { it.second.uuid })
                        .get().await().toObjects(EventArtist::class.java)

                withContext(Dispatchers.Main) {
                    (eventArtists as MutableLiveData).postValue(eventArtistList)
                }
                fetchArtists(eventArtistList.map { it.artistUUID })
            }
        } catch (exception: Exception) {
            val errorMessage =
                "Error fetching event artists: ${exception.localizedMessage ?: "Unknown error"}"
            Log.e(TAG, errorMessage, exception)
            postError(errorMessage)
        }
    }

    private suspend fun fetchArtists(artistUUIDs: List<String>) {
        try {
            val artistsList = artistsCollection.whereIn("uuid", artistUUIDs).get().await()
                .toObjects(Artist::class.java)
            withContext(Dispatchers.Main) {
                (artists as MutableLiveData).postValue(artistsList)
            }
        } catch (exception: Exception) {
            val errorMessage =
                "Error fetching artists: ${exception.localizedMessage ?: "Unknown error"}"
            Log.e(TAG, errorMessage, exception)
            postError(errorMessage)
        }
    }


    fun addEventToUserList(event: Event) = viewModelScope.launch {
        try {
            val user = userCollection.whereEqualTo("uuid", currentUser?.uid).get().await()
                .toObjects(User::class.java).firstOrNull()
            user?.let { user ->
                val userEvent = UserEvent(UUID.randomUUID().toString(), user.uuid, event.uuid)
                userEventCollection.document(userEvent.uuid).set(userEvent).await()
                Notifications.notifyAllFollowers(user.uuid, event, Constants.FOLLOWED_USER_ATTEND)
            } ?: throw IllegalStateException("User not found")
            operationCompletedMessage.postValue("Event added to your list.")
        } catch (e: Exception) {
            val errorMessage =
                "Error adding event to user list: ${e.localizedMessage ?: "Unknown error"}"
            Log.e(TAG, errorMessage, e)
            operationCompletedMessage.postValue(errorMessage)
        }
    }

    fun removeEventFromUserList(event: Event) = viewModelScope.launch {
        try {
            val documents = userEventCollection
                .whereEqualTo("userUUID", currentUser?.uid)
                .whereEqualTo("eventUUID", event.uuid).get().await()

            if (documents.isEmpty) {
                throw IllegalStateException("No such event found in user's list")
            }

            documents.forEach { document ->
                document.reference.delete().await()
            }
            operationCompletedMessage.postValue("Event removed from your list.")
        } catch (e: Exception) {
            val errorMessage =
                "Error removing event from user list: ${e.localizedMessage ?: "Unknown error"}"
            Log.e(TAG, errorMessage, e)
            operationCompletedMessage.postValue(errorMessage)
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
            val errorMessage =
                "Error checking attendance for event: ${e.localizedMessage ?: "Unknown error"}"
            Log.e(TAG, errorMessage, e)
            postError(errorMessage)
            false
        }
    }

    fun repostEvent(event: Event) {
        val repost = Repost(
            UUID.randomUUID().toString(),
            currentUser!!.uid,
            event.uuid,
            System.currentTimeMillis()
        )

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
            } catch (e: Exception) {
                val errorMessage = "Error reposting event: ${e.localizedMessage ?: "Unknown error"}"
                Log.e(TAG, errorMessage, e)
                postError(errorMessage)
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
                println("Notification successfully updated")
            }
            .addOnFailureListener { e ->
                val errorMessage =
                    "Error updating notification: ${e.localizedMessage ?: "Unknown error"}"
                Log.e(TAG, errorMessage, e)
                postError(errorMessage)
            }
    }

    private fun postError(message: String) {
        (errorMessages as MutableLiveData).postValue(message)
    }

    fun clearError() {
        (errorMessages as MutableLiveData).postValue("")
    }
}

class HomeViewModelFactory(
    private val appContext: Context?,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            appContext = appContext,
        ) as T
    }
}
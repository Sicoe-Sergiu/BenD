package com.example.bend.view_models

import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bend.model.Event
import com.example.bend.model.Followers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser

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


    val userData: MutableLiveData<Pair<String, MutableMap<String, Any>?>> = MutableLiveData()
    val userEvents: MutableLiveData<List<Event>> = MutableLiveData()
    val userFollowers: MutableLiveData<Int> = MutableLiveData()
    val userFollowing: MutableLiveData<Int> = MutableLiveData()

    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)

    private val usersCollectionNames = listOf("artist", "event_founder", "user")

    private val _followState = MutableStateFlow<Boolean?>(null)
    val followState: StateFlow<Boolean?> = _followState

    private fun refreshFollowersAndFollowing(userUUID: String) {
        viewModelScope.launch {
            userFollowers.value = getUserFollowers(userUUID)
            userFollowing.value = getUserFollowing(userUUID)
        }
    }

    fun refreshUserData(userUUID: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val userDataDeferred = async { getUserDataPair(userUUID) }
                val userEventsDeferred = async { getUserEvents(userUUID) }

                val results = awaitAll(
                    userDataDeferred,
                    userEventsDeferred,
                )
                checkFollowStatus(userUUID)
                refreshFollowersAndFollowing(userUUID)
                userData.value = results[0] as Pair<String, MutableMap<String, Any>?>
                userEvents.value = results[1] as List<Event>
                isLoading.value = false
            } finally {
                isLoading.value = false
            }
            //ADD CATCH ERRORS
        }
    }

    private suspend fun getUserDataMap(userId: String): MutableMap<String, Any>? {
        val db = FirebaseFirestore.getInstance()

        for (collectionName in usersCollectionNames) {
            try {
                val documentReference = db.collection(collectionName).document(userId)
                val snapshot = documentReference.get().await()

                if (snapshot.exists()) {
                    return snapshot.data?.toMutableMap() ?: mutableMapOf()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }


    private suspend fun getUserEvents(userUUID: String): List<Event> {

        return when (getAccountType(userUUID)) {
            "artist" -> getArtistEvents(userUUID)
            "event_founder" -> getFounderEvents(userUUID)
            "user" -> getRegularUserEvents(userUUID)
            else -> emptyList()
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

    private suspend fun getUserFollowers(userUUID: String): Int {
        try {
            val task = followersCollection.whereEqualTo("followedUserUUID", userUUID).get().await()
            return task.toObjects(Followers::class.java).size
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        }

        return 0;
    }

    private suspend fun getUserFollowing(userUUID: String): Int {
        try {
            val task = followersCollection.whereEqualTo("userUUID", userUUID).get().await()
            return task.toObjects(Followers::class.java).size
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        }

        return 0;
    }

    private fun checkFollowStatus(userUUID: String) {
        viewModelScope.launch {
            val isFollowing = ifFollow(userUUID)
            _followState.value = isFollowing
        }
    }

    fun follow(followedUserUUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val follow = Followers(
                    UUID.randomUUID().toString(),
                    currentUser!!.uid,
                    followedUserUUID
                )
                followersCollection
                    .document(follow.uuid)
                    .set(follow)
                    .await()
                // Successfully followed
                withContext(Dispatchers.Main) {
                    _followState.value = true
                    refreshFollowersAndFollowing(followedUserUUID)
                    // TODO: Show success feedback to user
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // TODO: Show error feedback to user
                }
            }
        }
    }

    fun unfollow(unfollowedUserUUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documents = followersCollection
                    .whereEqualTo("userUUID", currentUser?.uid)
                    .whereEqualTo("followedUserUUID", unfollowedUserUUID)
                    .get()
                    .await()
                for (document in documents) {
                    document.reference.delete().await()
                }
                // Successfully unfollowed
                withContext(Dispatchers.Main) {
                    _followState.value = false
                    refreshFollowersAndFollowing(unfollowedUserUUID)

                    // TODO: Show success feedback to user
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // TODO: Show error feedback to user
                }
            }
        }
    }

    private suspend fun ifFollow(userUUID: String): Boolean {
        return try {
            val followersDocuments = followersCollection
                .whereEqualTo("userUUID", currentUser?.uid)
                .whereEqualTo("followedUserUUID", userUUID)
                .get()
                .await()

            followersDocuments.documents.isNotEmpty()
        } catch (e: Exception) {
            // Handle exception
            false
        }
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

    suspend fun getUserDataPair(userUUID: String): Pair<String, MutableMap<String, Any>?> {
        return Pair(getAccountType(userUUID), getUserDataMap(userUUID))
    }
}
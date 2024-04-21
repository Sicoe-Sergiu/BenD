package com.example.bend.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bend.Constants
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val TAG = ProfileViewModel::class.simpleName

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser


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
    val errorMessages: LiveData<String> = MutableLiveData()


    private fun refreshFollowersAndFollowing(userUUID: String) {
        viewModelScope.launch {
            userFollowers.value = getUserFollowers(userUUID)
            userFollowing.value = getUserFollowing(userUUID)
        }
    }

    fun refreshUserData(userUUID: String) {
        viewModelScope.launch {
            isLoading.value = true
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
                val errorMessage = e.localizedMessage ?: "Error fetching user data."
                Log.e(TAG, errorMessage, e)
                e.printStackTrace()
                postError(errorMessage)
            }
        }
        return null
    }



    private suspend fun getUserEvents(userUUID: String): List<Event> {

        return when (HomeViewModel.getAccountType(null, userUUID)) {
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
            val errorMessage = e.localizedMessage ?: "Error fetching regular user events."
            Log.e(TAG, errorMessage, e)
            e.printStackTrace()
            postError(errorMessage)
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
            val errorMessage = e.localizedMessage ?: "Error fetching artist events."
            Log.e(TAG, errorMessage, e)
            e.printStackTrace()
            postError(errorMessage)
        }

        return events
    }

    private suspend fun getFounderEvents(userID: String): List<Event> {
        try {
            val task = eventsCollection.whereEqualTo("founderUUID", userID).get().await()
            return task.toObjects(Event::class.java)
        } catch (e: Exception) {
            val errorMessage = e.localizedMessage ?: "Error fetching founder events."
            Log.e(TAG, errorMessage, e)
            e.printStackTrace()
            postError(errorMessage)
        }

        return emptyList()
    }


    private suspend fun getUserFollowers(userUUID: String): Int {
        try {
            val task = followersCollection.whereEqualTo("followedUserUUID", userUUID).get().await()
            return task.toObjects(Followers::class.java).size
        } catch (e: Exception) {
            val errorMessage = e.localizedMessage ?: "Error fetching user followers."
            Log.e(TAG, errorMessage, e)
            e.printStackTrace()
            postError(errorMessage)
        }

        return 0
    }

    private suspend fun getUserFollowing(userUUID: String): Int {
        try {
            val task = followersCollection.whereEqualTo("userUUID", userUUID).get().await()
            return task.toObjects(Followers::class.java).size
        } catch (e: Exception) {
            val errorMessage = e.localizedMessage ?: "Error fetching user following."
            Log.e(TAG, errorMessage, e)
            e.printStackTrace()
            postError(errorMessage)
        }

        return 0
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
                withContext(Dispatchers.Main) {
                    _followState.value = true
                    refreshFollowersAndFollowing(followedUserUUID)
                    Notifications.sendNotification(
                        toUserUUID = followedUserUUID,
                        fromUserUUID = currentUser.uid,
                        text = Constants.NEW_FOLLOWER
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = e.localizedMessage ?: "Error following user."
                    Log.e(TAG, errorMessage, e)
                    e.printStackTrace()
                    postError(errorMessage)
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
                withContext(Dispatchers.Main) {
                    _followState.value = false
                    refreshFollowersAndFollowing(unfollowedUserUUID)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = e.localizedMessage ?: "Error unfollowing user."
                    Log.e(TAG, errorMessage, e)
                    e.printStackTrace()
                    postError(errorMessage)
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
            val errorMessage = e.localizedMessage ?: "Error checking follow status."
            Log.e(TAG, errorMessage, e)
            e.printStackTrace()
            postError(errorMessage)
            false
        }
    }

    private suspend fun getUserDataPair(userUUID: String): Pair<String, MutableMap<String, Any>?> {
        return Pair(HomeViewModel.getAccountType(null, userUUID), getUserDataMap(userUUID))
    }
    private fun postError(message: String) {
        (errorMessages as MutableLiveData).postValue(message)
    }

    fun clearError() {
        (errorMessages as MutableLiveData).postValue("")
    }
}
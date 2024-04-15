package com.example.bend.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.model.events.AddReviewUIEvent
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventArtist
import com.example.bend.model.EventFounder
import com.example.bend.model.Review
import com.example.bend.view.ui_state.ReviewUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddReviewViewModel : ViewModel() {
    private val TAG = "ADD REVIEW SCREEN"
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser
    var reviewUiState = mutableStateOf(ReviewUiState())


    private val eventsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event")
    private val artistsCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("artist")
    private val eventFounderCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event_founder")
    private val eventArtistCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection("event_artist")

    private var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    lateinit var navController: NavController

    var event: LiveData<Event> = MutableLiveData()
    var founder: LiveData<EventFounder> = MutableLiveData()
    var artists: LiveData<List<Artist>> = MutableLiveData(emptyList())

    fun onEvent(review: AddReviewUIEvent) {

        when (review) {
            is AddReviewUIEvent.SlidersChanged -> {

                val updatedRates = reviewUiState.value.rates.toMutableList()
                updatedRates[review.sliderNo] = review.sliderValue
                reviewUiState.value = reviewUiState.value.copy(rates = updatedRates)
                printState()

            }

            is AddReviewUIEvent.ReviewsChanged -> {
                val updatedReview = reviewUiState.value.reviews.toMutableList()
                updatedReview[review.reviewNo] = review.reviewText
                reviewUiState.value = reviewUiState.value.copy(reviews = updatedReview)
                printState()
            }

            is AddReviewUIEvent.AddReviewButtonClicked -> {
                navController = review.navController

                addReview(navController)
            }
        }
    }

    private fun updateFounderRating() {

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val founderDocRef = eventFounderCollection.document(founder.value!!.uuid)
            val snapshot = transaction.get(founderDocRef)

            val currentRating = snapshot.getDouble("rating") ?: 0.0
            val currentRatingNumber = snapshot.getLong("ratingsNumber") ?: 0

            val newRatingSum =
                currentRating + (reviewUiState.value.rates.getOrNull(0) ?: 0.0f).toDouble()
            val newRatingsNumber = currentRatingNumber + 1

            transaction.update(founderDocRef, "rating", newRatingSum)
            transaction.update(founderDocRef, "ratingsNumber", newRatingsNumber)
        }
            .addOnSuccessListener {
                viewModelScope.launch(Dispatchers.IO) {
                    Notifications.notifySingleUser(
                        fromUser = currentUser!!.uid,
                        toUserUUID = founder.value!!.uuid,
                        event = event.value!!,
                        notificationText = "You received a new rating: ${String.format("%.2f", reviewUiState.value.rates.getOrNull(0) ?: 0.0f)}"
                    )
                }
                // TODO:Handle success
            }
            .addOnFailureListener { e ->
                // TODO:Handle failure
            }
    }

    private fun updateArtistRating(artist: Artist, ratingNo: Int) {

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val artistDocRef = artistsCollection.document(artist.uuid)
            val snapshot = transaction.get(artistDocRef)

            val currentRating = snapshot.getDouble("rating") ?: 0.0
            val currentRatingNumber = snapshot.getLong("ratingsNumber") ?: 0

            val newRatingSum =
                currentRating + (reviewUiState.value.rates.getOrNull(ratingNo) ?: 0.0f).toDouble()
            val newRatingsNumber = currentRatingNumber + 1

            transaction.update(artistDocRef, "rating", newRatingSum)
            transaction.update(artistDocRef, "ratingsNumber", newRatingsNumber)
        }
            .addOnSuccessListener {
                viewModelScope.launch(Dispatchers.IO) {
                    Notifications.notifySingleUser(
                        fromUser = currentUser!!.uid,
                        toUserUUID = artist.uuid,
                        event = event.value!!,
                        notificationText = "You received a new rating: ${String.format("%.2f", reviewUiState.value.rates.getOrNull(ratingNo) ?: 0.0f)}"
                    )
                }
                // TODO:Handle success
            }
            .addOnFailureListener { e ->
                // TODO:Handle failure
            }
    }

    private fun addReview(userUUID: String, reviewNo: Int) {
        val review = Review(
            uuid = UUID.randomUUID().toString(),
            writerUUID = currentUser!!.uid,
            eventUUID = event.value!!.uuid,
            userUUID = userUUID,
            reviewText = reviewUiState.value.reviews[reviewNo],
            creationTimestamp = System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("review").add(review)
            .addOnSuccessListener {
                viewModelScope.launch(Dispatchers.IO) {
                    Notifications.notifySingleUser(
                        fromUser = currentUser.uid,
                        toUserUUID = userUUID,
                        event = event.value!!,
                        notificationText = "You received a new review: \"${review.reviewText}\""
                    )
                }
//                TODO: handle success
            }
            .addOnFailureListener {
//                TODO: handle failure
            }
    }

    private fun addReview(navController: NavController) {
        updateFounderRating()
        if (reviewUiState.value.reviews[0] != "") {
            addReview(founder.value!!.uuid, 0)
        }
        artists.value?.forEachIndexed { index, artist ->
            updateArtistRating(artist, index + 1)
            if (reviewUiState.value.reviews[index + 1] != "") {
                addReview(artist.uuid, index + 1)
            }
        }
        navController.navigate(Constants.NAVIGATION_MY_EVENTS)
    }

    private fun printState() {
        Log.d(TAG, "Inside_printState")
        Log.d(TAG, reviewUiState.toString())
    }

    fun loadData(eventUUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            val fetchEventDeferred = async { fetchEventByUUID(eventUUID) }
            val fetchArtistsDeferred = async { fetchEventArtists(eventUUID) }

            awaitAll(
                fetchArtistsDeferred,
                fetchEventDeferred,
            )
            val fetchFounderDeferred = async { fetchFounderByUUID(event.value!!.founderUUID) }
            fetchFounderDeferred.await()

            _isLoading.value = false
        }
    }

    private suspend fun fetchEventByUUID(eventUUID: String) {
        try {
            val task = eventsCollection.whereEqualTo("uuid", eventUUID).get().await()

            val events = task.toObjects(Event::class.java)

            if (events.isNotEmpty()) {
                (event as MutableLiveData).postValue(events.first())
            } else {
                Log.e("fetchAndPostEventByUUID", "No events found with UUID: $eventUUID")
            }
        } catch (e: Exception) {
            Log.e("fetchAndPostEventByUUID", "Error fetching event by UUID: $eventUUID", e)
        }
    }

    private suspend fun fetchFounderByUUID(founderUUID: String) {
        try {
            val task = eventFounderCollection.whereEqualTo("uuid", founderUUID).get().await()

            val founders = task.toObjects(EventFounder::class.java)

            if (founders.isNotEmpty()) {
                (founder as MutableLiveData).postValue(founders.first())
            } else {
                Log.e("fetchAndPostEventFounderByUUID", "No Founder found with UUID: $founderUUID")
            }
        } catch (e: Exception) {
            Log.e(
                "fetchAndPostEventFounderByUUID",
                "Error fetching Founder by UUID: $founderUUID",
                e
            )
        }
    }

    private suspend fun fetchEventArtists(eventUUID: String) {
        val _artists: MutableList<Artist> = mutableListOf()
        try {
            val task = eventArtistCollection.whereEqualTo("eventUUID", eventUUID).get().await()

            val eventArtists = task.toObjects(EventArtist::class.java)
            for (eventArtist in eventArtists) {
                try {
                    val task1 =
                        artistsCollection.whereEqualTo("uuid", eventArtist.artistUUID).get().await()

                    _artists.add(task1.toObjects(Artist::class.java).first())

                } catch (e: Exception) {
                    Log.e("fetchEventArtists", "Error fetching artists by UUID: $eventUUID", e)
                }
            }

        } catch (e: Exception) {
            Log.e("fetchEventArtists", "Error fetching eventArtists by UUID: $eventUUID", e)
        }
        (artists as MutableLiveData).postValue(_artists)
    }


}
package com.example.bend.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.model.events.CreateEventUIEvent
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventArtist
import com.example.bend.model.validators.CreateEventValidator
import com.example.bend.view.ui_state.CreateEventUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class AddEditEventViewModel : ViewModel() {
    private val TAG = AddEditEventViewModel::class.simpleName
    var createEventUiState = mutableStateOf(CreateEventUiState())

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser

    val artistsLiveData: MutableLiveData<List<Artist>> = MutableLiveData()

    private var posterUriValidationsPassed = mutableStateOf(false)
    private var entranceFeeValidationsPassed = mutableStateOf(false)
    private var locationValidationsPassed = mutableStateOf(false)
    private var startDateValidationsPassed = mutableStateOf(false)
    private var endDateValidationsPassed = mutableStateOf(false)
    private var startTimeValidationsPassed = mutableStateOf(false)
    private var endTimeValidationsPassed = mutableStateOf(false)
    private var artistsValidationsPassed = mutableStateOf(false)

    private var eventCreationInProgress = mutableStateOf(false)
    lateinit var navController: NavController

    val errorMessages: LiveData<String> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        fetchArtistsFromFirestore()
    }

    fun onEvent(event: CreateEventUIEvent) {

        when (event) {
            is CreateEventUIEvent.PosterChanged -> {
                createEventUiState.value =
                    createEventUiState.value.copy(posterUri = event.posterUri)
                validatePosterDataWithRules()
                printState()
            }

            is CreateEventUIEvent.LocationChanged -> {
                createEventUiState.value = createEventUiState.value.copy(location = event.location)
                validateLocationDataWithRules()
                printState()
            }

            is CreateEventUIEvent.EntranceFeeChanged -> {
                createEventUiState.value =
                    createEventUiState.value.copy(entranceFee = event.entranceFee)
                validateEntranceFeeDataWithRules()
                printState()
            }

            is CreateEventUIEvent.StartDateChanged -> {
                createEventUiState.value =
                    createEventUiState.value.copy(startDate = event.startDate)
                validateStartDateDataWithRules()
                printState()
            }

            is CreateEventUIEvent.EndDateChanged -> {
                createEventUiState.value = createEventUiState.value.copy(endDate = event.endDate)
                validateEndDateDataWithRules()
                printState()
            }

            is CreateEventUIEvent.StartTimeChanged -> {
                createEventUiState.value =
                    createEventUiState.value.copy(startTime = event.startTime)
                validateStartTimeDataWithRules()
                printState()
            }

            is CreateEventUIEvent.EndTimeChanged -> {
                createEventUiState.value = createEventUiState.value.copy(endTime = event.endTime)
                validateEndTimeDataWithRules()
                printState()
            }

            is CreateEventUIEvent.ArtistsChanged -> {
                createEventUiState.value =
                    createEventUiState.value.copy(artists = event.artistsUsernames)
                validateArtistsDataWithRules()
                printState()
            }


            is CreateEventUIEvent.CreateEventButtonClicked -> {
                validateAll()
                navController = event.navController
                if (checkErrors()) {
                    createEvent()
                }
            }

            is CreateEventUIEvent.EditEventButtonClicked -> {
                validateAll()
                navController = event.navController
                if (checkErrors()) {
                    editEvent(navController)
                }
            }
        }
    }

    private fun postError(message: String) {
        (errorMessages as MutableLiveData).postValue(message)
    }

    fun clearError() {
        (errorMessages as MutableLiveData).postValue("")
    }

    private fun editEvent(navController: NavController) {
        val currentUserUID = currentUser?.uid ?: ""
        val editEventState = createEventUiState.value
        val posterUUID = editEventState.uuid

        val storageRef: StorageReference = storage.reference.child("events_posters/$posterUUID")

        try {
            isLoading.value = true

            if (createEventUiState.value.posterUri.toString().startsWith("http")) {
                val updatedEvent = buildEvent(
                    editEventState,
                    currentUserUID,
                    editEventState.posterUri.toString()
                )
                updateEventInFirestore(updatedEvent, navController)
            } else {
                createEventUiState.value.posterUri?.let { posterUri ->
                    uploadPosterAndUpdateEvent(
                        posterUri,
                        editEventState,
                        currentUserUID,
                        storageRef,
                        navController
                    )
                } ?: postError("Poster URI is null")
            }
            isLoading.value = false
        } catch (e: Exception) {
            postError("Error processing event: ${e.localizedMessage}")
            Log.e(TAG, "Error in editEvent function: $e")
        }
    }

    private fun buildEvent(
        editEventState: CreateEventUiState,
        currentUserUID: String,
        posterDownloadLink: String
    ): Event {
        return Event(
            uuid = editEventState.uuid,
            location = editEventState.location,
            entranceFee = editEventState.entranceFee,
            startDate = editEventState.startDate.toString(),
            endDate = editEventState.endDate.toString(),
            startTime = editEventState.startTime.toString(),
            endTime = editEventState.endTime.toString(),
            founderUUID = currentUserUID,
            posterDownloadLink = posterDownloadLink,
            creationTimestamp = System.currentTimeMillis()
        )
    }

    private fun updateEventInFirestore(event: Event, navController: NavController) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("event").document(event.uuid)
                    .set(event, SetOptions.merge())
                    .addOnSuccessListener {
                        updateEventArtists(event, navController)
                    }
                    .addOnFailureListener { exception ->
                        postError("Failed to update event: ${exception.message}")
                        Log.e(TAG, "Failed to merge event document: ${exception.message}")
                    }
            } catch (e: Exception) {
                postError("Error updating event in Firestore: ${e.localizedMessage}")
                Log.e(TAG, "Error in database operation: $e")
            }
        }
    }

    private fun uploadPosterAndUpdateEvent(
        posterUri: Uri,
        editEventState: CreateEventUiState,
        currentUserUID: String,
        storageRef: StorageReference,
        navController: NavController
    ) {
        storageRef.putFile(posterUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val updatedEvent =
                        buildEvent(editEventState, currentUserUID, downloadUrl.toString())
                    updateEventInFirestore(updatedEvent, navController)
                }.addOnFailureListener { exception ->
                    postError("Failed to get download URL: ${exception.message}")
                    Log.e(TAG, "Error getting download URL: $exception")
                }
            }.addOnFailureListener { exception ->
                postError("Failed to upload poster: ${exception.message}")
                Log.e(TAG, "Error uploading file to Firebase: $exception")
            }
    }

    private fun updateEventArtists(event: Event, navController: NavController) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                deleteRemovedArtistsFromFirebase(
                    eventUUID = event.uuid,
                    artists = createEventUiState.value.artists,
                    event = event
                )
                for (artist in createEventUiState.value.artists) {
                    val existingDoc = firestore.collection("event_artist")
                        .whereEqualTo("eventUUID", event.uuid)
                        .whereEqualTo("artistUUID", artist.uuid)
                        .get()
                        .await()

                    if (existingDoc.isEmpty) {
                        firestore.collection("event_artist")
                            .add(EventArtist(artist.uuid, event.uuid))
                        Notifications.notifySingleUser(
                            fromUser = event.founderUUID,
                            toUserUUID = artist.uuid,
                            event = event,
                            notificationText = Constants.ARTIST_ADDED_TO_NEW_EVENT,
                            sensitive = true
                        )
                    }
                }
            } catch (e: Exception) {
                postError("Failed to manage artists after event update: ${e.localizedMessage}")
                Log.e(TAG, "Error managing artists after update: $e")
            }
            withContext(Dispatchers.Main) {
                navController.navigate(Constants.NAVIGATION_MY_EVENTS)
            }
        }
    }

    private suspend fun deleteRemovedArtistsFromFirebase(
        eventUUID: String,
        artists: List<Artist>,
        event: Event
    ) {
        val artistUUIDs = artists.map { it.uuid }
        try {
            val querySnapshot = firestore.collection("event_artist")
                .whereEqualTo("eventUUID", eventUUID)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val artistUUID = document.getString("artistUUID") ?: ""
                if (artistUUID !in artistUUIDs) {
                    try {
                        firestore.collection("event_artist").document(document.id).delete().await()
                        Notifications.notifySingleUser(
                            toUserUUID = artistUUID,
                            fromUser = event.founderUUID,
                            event = event,
                            notificationText = Constants.ARTIST_REMOVED_FROM_EVENT,
                            sensitive = true
                        )
                        Log.d(
                            TAG,
                            "Document with ID ${document.id} and artistUUID $artistUUID deleted successfully."
                        )
                    } catch (e: Exception) {
                        Log.e(
                            TAG,
                            "Error deleting artist document with ID ${document.id}: ${e.localizedMessage}"
                        )
                    }
                } else {
                    Log.d(
                        TAG,
                        "Document with ID ${document.id} and artistUUID $artistUUID was not deleted as it is still linked to an existing artist."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching or processing artist documents: ${e.localizedMessage}")
            postError("Failed to update artist information for the event: ${e.localizedMessage}")
        }
    }


    private fun checkErrors(): Boolean {
        return (
                posterUriValidationsPassed.value &&
                        entranceFeeValidationsPassed.value &&
                        locationValidationsPassed.value &&
                        startDateValidationsPassed.value &&
                        endDateValidationsPassed.value &&
                        startTimeValidationsPassed.value &&
                        endTimeValidationsPassed.value &&
                        artistsValidationsPassed.value
                )
    }

    private fun validateAll() {
        validatePosterDataWithRules(finalCheck = true)
        validateLocationDataWithRules(finalCheck = true)
        validateEntranceFeeDataWithRules(finalCheck = true)
        validateStartDateDataWithRules(finalCheck = true)
        validateEndDateDataWithRules(finalCheck = true)
        validateStartTimeDataWithRules(finalCheck = true)
        validateEndTimeDataWithRules(finalCheck = true)
        validateArtistsDataWithRules(finalCheck = true)
    }

    private fun createEvent() {
        try {
            isLoading.value = true
            eventCreationInProgress.value = true

            val eventUUID = UUID.randomUUID()
            val storageRef: StorageReference = storage.reference.child("events_posters/$eventUUID")

            createEventUiState.value.posterUri?.let { posterUri ->
                storageRef.putFile(posterUri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            createEventInDatabase(eventUUID, downloadUrl.toString())
                        }
                    }
                    .addOnFailureListener { exception ->
                        postError("Failed to update artist information for the event: ${exception.localizedMessage}")
                    }
            }
            isLoading.value = false
        } catch (e: Exception) {
            postError("Failed to update artist information for the event: ${e.localizedMessage}")
            Log.e(TAG, "Error creating event: ${e.localizedMessage}", e)
        }
    }

    private fun createEventInDatabase(eventUUID: UUID, posterDownloadLink: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserUID = currentUser?.uid ?: return@launch
                val createEventState = createEventUiState.value

                val event = Event(
                    uuid = eventUUID.toString(),
                    location = createEventState.location,
                    entranceFee = createEventState.entranceFee,
                    startDate = createEventState.startDate.toString(),
                    endDate = createEventState.endDate.toString(),
                    startTime = createEventState.startTime.toString(),
                    endTime = createEventState.endTime.toString(),
                    founderUUID = currentUserUID,
                    posterDownloadLink = posterDownloadLink,
                    creationTimestamp = System.currentTimeMillis()
                )

                val db = FirebaseFirestore.getInstance()

                db.collection("event").document(eventUUID.toString()).set(event).await()

                val artists: MutableList<Artist> = mutableListOf()
                createEventState.artists.forEach { artist ->
                    val eventArtist = EventArtist(
                        artistUUID = artist.uuid,
                        eventUUID = eventUUID.toString()
                    )
                    db.collection("event_artist").add(eventArtist).await()
                    Notifications.notifySingleUser(
                        toUserUUID = artist.uuid,
                        fromUser = event.founderUUID,
                        event = event,
                        notificationText = Constants.ARTIST_ADDED_TO_NEW_EVENT,
                        sensitive = true
                    )
                    artists.add(artist)
                }

                Notifications.notifyFollowersOfNewEvent(db, event)
                Notifications.notifyFollowersOfEventPerformance(db, artists, event)

                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Event and event artists added successfully")
                    navController.navigate(Constants.userProfileNavigation(currentUserUID))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    postError("Failed to create event or add artists: ${e.localizedMessage}")
                    Log.e(TAG, "Error adding event or event artists", e)
                }
            }
        }
    }

    private fun validateStartDateDataWithRules(finalCheck: Boolean = false) {
        val result = createEventUiState.value.startDate?.let {
            CreateEventValidator.validateStartDate(
                startDate = it
            )
        }

        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                startDateError = result.status
            )
            startDateValidationsPassed.value = result.status

            if (!result.status && finalCheck) {
                postError("Failed to create event: ${result.message}")
            }
        } else {
            if (finalCheck)
                postError("Failed to create event: Start date is not provided.")
        }
    }

    private fun validateEndDateDataWithRules(finalCheck: Boolean = false) {
        val result = createEventUiState.value.endDate?.let {
            CreateEventValidator.validateEndDate(endDate = it)
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                endDateError = result.status
            )
            endDateValidationsPassed.value = result.status
            if (!result.status && finalCheck) {
                postError("Failed to create event: ${result.message ?: "Invalid end date"}")
            }
        } else {
            if (finalCheck)
                postError("Failed to create event: End date is not provided.")
        }
    }

    private fun validateStartTimeDataWithRules(finalCheck: Boolean = false) {
        val result = createEventUiState.value.startTime?.let {
            CreateEventValidator.validateStartTime(startTime = it)
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                startTimeError = result.status
            )
            startTimeValidationsPassed.value = result.status
            if (!result.status && finalCheck) {
                postError("Failed to create event: ${result.message ?: "Invalid start time"}")
            }
        } else {
            if (finalCheck)
                postError("Failed to create event: Start time is not provided.")
        }
    }


    private fun validateEndTimeDataWithRules(finalCheck: Boolean = false) {
        val result = createEventUiState.value.endTime?.let {
            CreateEventValidator.validateEndTime(endTime = it)
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                endTimeError = result.status
            )
            endTimeValidationsPassed.value = result.status
            if (!result.status && finalCheck) {
                postError("Failed to create event: ${result.message ?: "Invalid end time"}")
            }
        } else {
            if (finalCheck)
                postError("Failed to create event: End time is not provided.")
        }
    }


    private fun validateArtistsDataWithRules(finalCheck: Boolean = false) {
        val result = createEventUiState.value.artists?.let {
            CreateEventValidator.validateArtists(artistsUsernames = it)
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                artistsError = result.status
            )
            artistsValidationsPassed.value = result.status
            if (!result.status && finalCheck) {
                postError("Failed to create event: ${result.message ?: "No artists selected or invalid artists"}")
            }
        } else {
            if (finalCheck)
                postError("Failed to create event: Artist details are not provided.")
        }
    }


    private fun validateEntranceFeeDataWithRules(finalCheck: Boolean = false) {
        val result = createEventUiState.value.entranceFee?.let {
            CreateEventValidator.validateEntranceFee(entranceFee = it)
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                entranceFeeError = result.status
            )
            entranceFeeValidationsPassed.value = result.status
            if (!result.status && finalCheck) {
                postError("Failed to create event: ${result.message ?: "Invalid entrance fee"}")
            }
        } else {
            if (finalCheck)
                postError("Failed to create event: Entrance fee is not provided.")
        }
    }


    private fun validateLocationDataWithRules(finalCheck: Boolean = false) {
        val result = createEventUiState.value.location?.let {
            CreateEventValidator.validateLocation(location = it)
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                locationError = result.status
            )
            locationValidationsPassed.value = result.status
            if (!result.status && finalCheck) {
                postError("Failed to create event: ${result.message ?: "Invalid location"}")
            }
        } else {
            if (finalCheck)
                postError("Failed to create event: Location is not provided.")
        }
    }


    private fun validatePosterDataWithRules(finalCheck: Boolean = false) {
        val result = createEventUiState.value.posterUri?.let {
            CreateEventValidator.validatePoster(uri = it)
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                posterError = result.status
            )
            posterUriValidationsPassed.value = result.status
            if (!result.status && finalCheck) {
                postError("Failed to create event: ${result.message ?: "Invalid poster URI"}")
            }
        } else {
            if (finalCheck)
                postError("Failed to create event: Poster URI is not provided.")
        }
    }


    private fun printState() {
        Log.d(TAG, "Inside_printState")
        Log.d(TAG, createEventUiState.toString())
    }

    private fun fetchArtistsFromFirestore() {
        try {
            firestore.collection("artist")
                .get()
                .addOnSuccessListener { result ->
                    try {
                        val artistsList = mutableListOf<Artist>()
                        for (document in result) {
                            val artist = document.toObject(Artist::class.java)
                            artistsList.add(artist)
                        }
                        artistsLiveData.value = artistsList
                        Log.d(TAG, "Fetched artists successfully: ${artistsLiveData.value}")
                    } catch (e: Exception) {
                        postError("Failed to process artist data: ${e.localizedMessage}")
                        Log.e(TAG, "Error processing artists", e)
                    }
                }
                .addOnFailureListener { exception ->
                    postError("Failed to fetch artists from Firestore: ${exception.localizedMessage}")
                    Log.e(TAG, "Error fetching artists", exception)
                }
        } catch (e: Exception) {
            postError("Unexpected error occurred when fetching artists: ${e.localizedMessage}")
            Log.e(TAG, "Unexpected error fetching artists", e)
        }
    }


    suspend fun populateUiState(eventUUID: String?) {
        val artistsList: MutableList<Artist> = mutableListOf()

        try {
            if (eventUUID == null) {
                postError("Event UUID is null, cannot fetch event details.")
                return
            }

            val event = firestore.collection("event").whereEqualTo("uuid", eventUUID).get().await()
                .toObjects(Event::class.java).firstOrNull()
                ?: throw IllegalStateException("No event found for the provided UUID.")

            val eventArtists =
                firestore.collection("event_artist").whereEqualTo("eventUUID", eventUUID).get()
                    .await()
                    .toObjects(EventArtist::class.java)

            eventArtists.forEach { eventArtist ->
                firestore.collection("artist").whereEqualTo("uuid", eventArtist.artistUUID).get()
                    .await()
                    .toObjects(Artist::class.java).firstOrNull()?.let { artist ->
                        artistsList.add(artist)
                    }
                    ?: throw IllegalStateException("Artist details could not be fetched for UUID: ${eventArtist.artistUUID}")
            }

            updateUiState(event, artistsList)
        } catch (e: Exception) {
            Log.e(TAG, "Error populating UI state", e)
            postError("Failed to populate UI state: ${e.localizedMessage}")
        }
    }

    private fun updateUiState(event: Event, artists: List<Artist>) {
        createEventUiState.value = createEventUiState.value.copy(
            uuid = event.uuid,
            location = event.location,
            entranceFee = event.entranceFee,
            startDate = LocalDate.parse(event.startDate),
            endDate = LocalDate.parse(event.endDate),
            startTime = LocalTime.parse(event.startTime),
            endTime = LocalTime.parse(event.endTime),
            artists = artists,
            posterUri = Uri.parse(event.posterDownloadLink)
        )
    }


}
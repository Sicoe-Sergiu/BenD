package com.example.bend.view_models

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.events.CreateEventUIEvent
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventArtist
import com.example.bend.register_login.CreateEventValidator
import com.example.bend.ui_state.CreateEventUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
                    createEvent(navController)
                }
            }

            is CreateEventUIEvent.EditEventButtonClicked -> {
                validateAll()
                navController = event.navController
                if (checkErrors()) {
                    editEvent(navController)
                }
            }

            else -> {}
        }
    }

    private fun editEvent(navController: NavController) {
        val currentUserUID = currentUser?.uid ?: ""
        val editEventState = createEventUiState.value
        val posterUUID = editEventState.uuid

        val storageRef: StorageReference =
            storage.reference.child("events_posters/$posterUUID")

        if (createEventUiState.value.posterUri.toString().startsWith("http")) {
            val updatedEvent = Event(
                uuid = editEventState.uuid,
                location = editEventState.location,
                entranceFee = editEventState.entranceFee,
                startDate = editEventState.startDate.toString(),
                endDate = editEventState.endDate.toString(),
                startTime = editEventState.startTime.toString(),
                endTime = editEventState.endTime.toString(),
                founderUUID = currentUserUID,
                posterDownloadLink = editEventState.posterUri.toString(),
                creationTimestamp = System.currentTimeMillis()
            )
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    firestore.collection("event").document(updatedEvent.uuid)
                        .set(updatedEvent, SetOptions.merge())
                        .addOnSuccessListener {
                            viewModelScope.launch(Dispatchers.IO){
                                deleteEventArtistWithEventUUID(eventUUID = updatedEvent.uuid)
                                for (artist in createEventUiState.value.artists){
                                    firestore.collection("event_artist").add(EventArtist(artist.uuid, updatedEvent.uuid))
                                }
                                navController.navigate(Constants.NAVIGATION_MY_EVENTS)

                            }
                        }
                    println("Document update successful!")
                } catch (e: Exception) {
                    println("Error updating document: $e")
                }
            }
        } else {
            createEventUiState.value.posterUri?.let { posterUri ->
                storageRef.putFile(posterUri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            val updatedEvent = Event(
                                uuid = editEventState.uuid,
                                location = editEventState.location,
                                entranceFee = editEventState.entranceFee,
                                startDate = editEventState.startDate.toString(),
                                endDate = editEventState.endDate.toString(),
                                startTime = editEventState.startTime.toString(),
                                endTime = editEventState.endTime.toString(),
                                founderUUID = currentUserUID,
                                posterDownloadLink = downloadUrl.toString(),
                                creationTimestamp = System.currentTimeMillis()
                            )
                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    firestore.collection("event").document(updatedEvent.uuid)
                                        .set(updatedEvent, SetOptions.merge())
                                        .addOnSuccessListener {
                                            viewModelScope.launch(Dispatchers.IO){
                                                deleteEventArtistWithEventUUID(eventUUID = updatedEvent.uuid)
                                                for (artist in createEventUiState.value.artists){
                                                    firestore.collection("event_artist").add(EventArtist(artist.uuid, updatedEvent.uuid))
                                                }
                                                navController.navigate(Constants.NAVIGATION_MY_EVENTS)
                                            }
                                        }
                                    println("Document update successful!")
                                } catch (e: Exception) {
                                    println("Error updating document: $e")
                                }
                            }

                        }
                    }
                    .addOnFailureListener { exception ->
                        // TODO: Handle unsuccessful upload
                        Log.e("EVENT", "Error uploading image: $exception")
                    }
            }
        }


    }
    private suspend fun deleteEventArtistWithEventUUID(eventUUID: String) {

        val querySnapshot = firestore.collection("event_artist")
            .whereEqualTo("eventUUID", eventUUID)
            .get()
            .await()

        for (document in querySnapshot.documents) {
            firestore.collection("event_artist").document(document.id).delete().await()
            println("Document with ID ${document.id} deleted successfully.")
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
        validatePosterDataWithRules()
        validateLocationDataWithRules()
        validateEntranceFeeDataWithRules()
        validateStartDateDataWithRules()
        validateEndDateDataWithRules()
        validateStartTimeDataWithRules()
        validateEndTimeDataWithRules()
        validateArtistsDataWithRules()
    }

    private fun createEvent(navController: NavController) {
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
                    // TODO: Handle unsuccessful upload
                    Log.e("EVENT", "Error uploading image: $exception")
                }
        }
    }

    private fun createEventInDatabase(eventUUID: UUID, posterDownloadLink: String) {
        val currentUserUID = currentUser?.uid ?: ""
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
        val eventDocRef = db.collection("event").document(eventUUID.toString())

        db.runBatch { batch ->
            batch.set(eventDocRef, event)

            createEventState.artists.forEach { artist ->
                val eventArtist = EventArtist(
                    artistUUID = artist.uuid,
                    eventUUID = eventUUID.toString()
                )
                val eventArtistDocRef = db.collection("event_artist").document()

                batch.set(eventArtistDocRef, eventArtist)
            }
        }.addOnSuccessListener {
            Log.d("EVENT", "Event and event artists added successfully")
            navController.navigate(Constants.userProfileNavigation(currentUserUID))
        }.addOnFailureListener { e ->
            Log.e("EVENT", "Error adding event or event artists: $e")
            // Handle failure
        }
    }


    private fun validateStartDateDataWithRules() {
        val result = createEventUiState.value.startDate?.let {
            CreateEventValidator.validateStartDate(
                startDate = it
            )
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                startDateError = result.status
            )
        }
        if (result != null) {
            startDateValidationsPassed.value = result.status
        }
    }

    private fun validateEndDateDataWithRules() {
        val result = createEventUiState.value.endDate?.let {
            CreateEventValidator.validateEndDate(
                endDate = it
            )
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                endDateError = result.status
            )
        }
        if (result != null) {
            endDateValidationsPassed.value = result.status
        }
    }

    private fun validateStartTimeDataWithRules() {
        val result = createEventUiState.value.startTime?.let {
            CreateEventValidator.validateStartTime(
                startTime = it
            )
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                startTimeError = result.status
            )
        }
        if (result != null) {
            startTimeValidationsPassed.value = result.status
        }
    }

    private fun validateEndTimeDataWithRules() {
        val result = createEventUiState.value.endTime?.let {
            CreateEventValidator.validateEndTime(
                endTime = it
            )
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                endTimeError = result.status
            )
        }
        if (result != null) {
            endTimeValidationsPassed.value = result.status
        }
    }

    private fun validateArtistsDataWithRules() {
        val result = createEventUiState.value.artists?.let {
            CreateEventValidator.validateArtists(
                artistsUsernames = it
            )
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                artistsError = result.status
            )
        }
        if (result != null) {
            artistsValidationsPassed.value = result.status
        }
    }

    private fun validateEntranceFeeDataWithRules() {
        val result = createEventUiState.value.entranceFee?.let {
            CreateEventValidator.validateEntranceFee(
                entranceFee = it
            )
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                entranceFeeError = result.status
            )
        }
        if (result != null) {
            entranceFeeValidationsPassed.value = result.status
        }
    }

    private fun validateLocationDataWithRules() {
        val result = createEventUiState.value.location?.let {
            CreateEventValidator.validateLocation(
                location = it
            )
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                locationError = result.status
            )
        }
        if (result != null) {
            locationValidationsPassed.value = result.status
        }
    }

    private fun validatePosterDataWithRules() {
        val result = createEventUiState.value.posterUri?.let {
            CreateEventValidator.validatePoster(
                uri = it
            )
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                posterError = result.status
            )
        }
        if (result != null) {
            posterUriValidationsPassed.value = result.status
        }
    }


    private fun printState() {
        Log.d(TAG, "Inside_printState")
        Log.d(TAG, createEventUiState.toString())
    }

    private fun getArtistStageNames(): LiveData<List<String>> {
        val stageNamesLiveData: MutableLiveData<List<String>> = MutableLiveData()

        artistsLiveData.observeForever { artistsList ->
            val stageNameList = artistsList.map { it.stageName }
            stageNamesLiveData.value = stageNameList
        }
        Log.d("LOG@222 ", stageNamesLiveData.value.toString())
        return stageNamesLiveData
    }

    private fun fetchArtistsFromFirestore() {
        firestore.collection("artist")
            .get()
            .addOnSuccessListener { result ->
                val artistsList = mutableListOf<Artist>()
                for (document in result) {
                    val artist = document.toObject(Artist::class.java)
                    artistsList.add(artist)
                }
                artistsLiveData.value = artistsList
                Log.d("FETCH", artistsLiveData.value.toString())
            }
            .addOnFailureListener { exception ->


                //TODO: Handle error
            }
    }

    suspend fun populateUiState(eventUUID: String?) {
        val artistsList: MutableList<Artist> = mutableListOf()

        try {
            val event = firestore.collection("event").whereEqualTo("uuid", eventUUID).get().await()
                .toObjects(Event::class.java).firstOrNull() ?: return

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
            }

            updateUiState(event, artistsList)
            Log.d("BLABLA", "UI state populated successfully.")
        } catch (e: Exception) {
            Log.e("populateUiState", "Error populating UI state", e)
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
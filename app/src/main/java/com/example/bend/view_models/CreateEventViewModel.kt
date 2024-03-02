package com.example.bend.view_models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.bend.events.CreateEventUIEvent
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.register_login.CreateEventValidator
import com.example.bend.ui_state.CreateEventUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class CreateEventViewModel : ViewModel() {
    private val TAG = CreateEventViewModel::class.simpleName
    var createEventUiState = mutableStateOf(CreateEventUiState())

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser

    private val artistsLiveData: MutableLiveData<List<Artist>> = MutableLiveData()
    val artistsStageNamesLiveData: LiveData<List<String>> = getArtistStageNames()

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

            is CreateEventUIEvent.ArtistsUsernamesChanged -> {
                createEventUiState.value =
                    createEventUiState.value.copy(artistsStageNames = event.artistsUsernames)
                validateArtistsDataWithRules()
                printState()
            }


            is CreateEventUIEvent.CreateEventButtonClicked -> {
                validateAll()
                navController = event.navController
                if (checkErrors()){
                    createEvent(navController)
                }
            }

            else -> {}
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
        createEventUiState.value.run {
            val event = Event(
                uuid = eventUUID,
                location = location,
                entranceFee = entranceFee,
                startDate = startDate.toString(),
                endDate = endDate.toString(),
                startTime = startTime.toString(),
                endTime = endTime.toString(),
                artistStageNames = artistsStageNames,
                organizerUUID = currentUser?.uid ?: "",
                posterDownloadLink = posterDownloadLink
            )

            val db = FirebaseFirestore.getInstance()
            db.collection("event")
                .document(eventUUID.toString()).set(event)
                .addOnSuccessListener {
                    // TODO: Handle succes
                    Log.d("EVENT", "Event added successfully")
                    eventCreationInProgress.value = false
                }
                .addOnFailureListener { e ->
                    // TODO: Handle unsuccessful upload
                    Log.e("EVENT", "Error adding event: $e")
                }
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
        val result = createEventUiState.value.artistsStageNames?.let {
            CreateEventValidator.validateArtists(
                artistsUsernames = it
            )
        }
        if (result != null) {
            createEventUiState.value = createEventUiState.value.copy(
                artistsUsernamesError = result.status
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


}
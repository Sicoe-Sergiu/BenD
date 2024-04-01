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
import com.example.bend.events.RegistrationUIEvent
import com.example.bend.model.Artist
import com.example.bend.model.EventFounder
import com.example.bend.model.User
import com.example.bend.register_login.RegisterLoginValidator
import com.example.bend.ui_state.RegistrationUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val TAG = RegisterViewModel::class.simpleName
    var registration_ui_state = mutableStateOf(RegistrationUiState())

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser

    var photoUriValidationsPassed = mutableStateOf(false)
    var first_name_validations_passed = mutableStateOf(false)
    var last_name_validations_passed = mutableStateOf(false)
    var username_validations_passed = mutableStateOf(false)
    var email_validations_passed = mutableStateOf(false)
    var password_validations_passed = mutableStateOf(false)
    var phone_validations_passed = mutableStateOf(false)
    var stage_name_validations_passed = mutableStateOf(false)

    var sign_up_in_progress = mutableStateOf(false)

    lateinit var navController: NavController

    private val usersCollectionNames = listOf("artist", "event_founder", "user")


    var founder: LiveData<EventFounder> = MutableLiveData(null)
    var artist: LiveData<Artist> = MutableLiveData(null)
    var user: LiveData<User> = MutableLiveData(null)
    var userType: LiveData<String> = MutableLiveData(null)

    private val _isUserSet = MutableStateFlow(false)
    val isUserSet: StateFlow<Boolean> = _isUserSet.asStateFlow()
    fun onEvent(event: RegistrationUIEvent) {

        when (event) {
            is RegistrationUIEvent.FirstNameChanged -> {
                registration_ui_state.value =
                    registration_ui_state.value.copy(first_name = event.first_name)
                validateFirstNameDataWithRules()
                printState()
            }

            is RegistrationUIEvent.LastNameChanged -> {
                registration_ui_state.value =
                    registration_ui_state.value.copy(last_name = event.last_name)
                validateLastNameDataWithRules()
                printState()
            }

            is RegistrationUIEvent.UsernameChanged -> {
                registration_ui_state.value =
                    registration_ui_state.value.copy(username = event.username)
                validateUsernameDataWithRules()
                printState()
            }

            is RegistrationUIEvent.EmailChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(email = event.email)
                validateEmailDataWithRules()
                printState()
            }

            is RegistrationUIEvent.PasswordChanged -> {
                registration_ui_state.value =
                    registration_ui_state.value.copy(password = event.password)
                validatePasswordDataWithRules()
                printState()
            }

            is RegistrationUIEvent.AccountTypeChanged -> {
                registration_ui_state.value =
                    registration_ui_state.value.copy(account_type = event.account_type)
                printState()
            }

//            conditionals
            is RegistrationUIEvent.PhoneChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(phone = event.phone)
                validatePhoneDataWithRules()
                printState()
            }

            is RegistrationUIEvent.StageNameChanged -> {
                registration_ui_state.value =
                    registration_ui_state.value.copy(stage_name = event.stage_name)
                validateStageNameDataWithRules()
                printState()
            }

            is RegistrationUIEvent.ProfilePhotoChanged -> {
                registration_ui_state.value =
                    registration_ui_state.value.copy(photoUri = event.photoUri.toString())
//                validatePosterDataWithRules()
                printState()
            }

            is RegistrationUIEvent.RegisterButtonClicked -> {
                validateFirstNameDataWithRules()
                validateLastNameDataWithRules()
                validateUsernameDataWithRules()
                validateEmailDataWithRules()
                validatePasswordDataWithRules()
                validatePhoneDataWithRules()
                validateStageNameDataWithRules()

                navController = event.navController
                if (first_name_validations_passed.value &&
                    last_name_validations_passed.value &&
                    username_validations_passed.value &&
                    email_validations_passed.value &&
                    password_validations_passed.value &&
                    (
                            (registration_ui_state.value.account_type == "Event Organizer account" && phone_validations_passed.value) ||
                                    (registration_ui_state.value.account_type == "Artist account" && stage_name_validations_passed.value) ||
                                    registration_ui_state.value.account_type == "Regular Account"
                            )
                )
                    signUp(navController)
            }

            is RegistrationUIEvent.LogOutButtonClicked -> {
                navController = event.navController
                logOutUser(navController)
            }

            is RegistrationUIEvent.SaveEditChangesButtonClicked -> {
                validateEdit()
                navController = event.navController
                if (checkEditErrors()) {
                    editProfile(navController)
                }
            }
        }
    }

    private fun editProfile(navController: NavController) {
        val editUserState = registration_ui_state.value
        val profilePhotoUUID = editUserState.uuid

        val storageRef: StorageReference =
            storage.reference.child("profile_photos/$profilePhotoUUID")

        if (registration_ui_state.value.photoUri.startsWith("http")) {
            when (userType.value) {
                "event_founder" -> {
                    val updatedFounder = EventFounder(
                        uuid = editUserState.uuid,
                        username = editUserState.username,
                        firstName = editUserState.first_name,
                        lastName = editUserState.last_name,
                        phone = editUserState.phone,
                        email = editUserState.email,
                        profilePhotoURL = editUserState.photoUri,
                        rating = founder.value!!.rating,
                        ratingsNumber = founder.value!!.ratingsNumber
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            firestore.collection("event_founder").document(updatedFounder.uuid)
                                .set(updatedFounder, SetOptions.merge())
                                .addOnSuccessListener {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        navController.navigate(Constants.userProfileNavigation(updatedFounder.uuid))
                                    }
                                }
                            println("Document update successful!")
                        } catch (e: Exception) {
                            println("Error updating document: $e")
                        }
                    }
                }

                "artist" -> {
                    val updatedArtist = Artist(
                        uuid = editUserState.uuid,
                        username = editUserState.username,
                        firstName = editUserState.first_name,
                        lastName = editUserState.last_name,
                        email = editUserState.email,
                        profilePhotoURL = editUserState.photoUri,
                        rating = founder.value!!.rating,
                        ratingsNumber = founder.value!!.ratingsNumber,
                        stageName = editUserState.stage_name
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            firestore.collection("artist").document(updatedArtist.uuid)
                                .set(updatedArtist, SetOptions.merge())
                                .addOnSuccessListener {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        navController.navigate(Constants.userProfileNavigation(updatedArtist.uuid))
                                    }
                                }
                            println("Document update successful!")
                        } catch (e: Exception) {
                            println("Error updating document: $e")
                        }
                    }
                }

                "user" -> {
                    val updatedUser = User(
                        uuid = editUserState.uuid,
                        username = editUserState.username,
                        firstName = editUserState.first_name,
                        lastName = editUserState.last_name,
                        email = editUserState.email,
                        profilePhotoURL = editUserState.photoUri,
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            firestore.collection("user").document(updatedUser.uuid)
                                .set(updatedUser, SetOptions.merge())
                                .addOnSuccessListener {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        navController.navigate(Constants.userProfileNavigation(updatedUser.uuid))
                                    }
                                }
                            println("Document update successful!")
                        } catch (e: Exception) {
                            println("Error updating document: $e")
                        }
                    }
                }
            }
        } else {
            registration_ui_state.value.photoUri.let { posterUri ->
                storageRef.putFile(Uri.parse(posterUri))
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            when (userType.value) {
                                "event_founder" -> {
                                    val updatedFounder = EventFounder(
                                        uuid = editUserState.uuid,
                                        username = editUserState.username,
                                        firstName = editUserState.first_name,
                                        lastName = editUserState.last_name,
                                        phone = editUserState.phone,
                                        email = editUserState.email,
                                        profilePhotoURL = downloadUrl.toString(),
                                        rating = founder.value!!.rating,
                                        ratingsNumber = founder.value!!.ratingsNumber
                                    )
                                    viewModelScope.launch(Dispatchers.IO) {
                                        try {
                                            firestore.collection("event_founder").document(updatedFounder.uuid)
                                                .set(updatedFounder, SetOptions.merge())
                                                .addOnSuccessListener {
                                                    viewModelScope.launch(Dispatchers.Main) {
                                                        navController.navigate(Constants.userProfileNavigation(updatedFounder.uuid))
                                                    }
                                                }
                                            println("Document update successful!")
                                        } catch (e: Exception) {
                                            println("Error updating document: $e")
                                        }
                                    }
                                }

                                "artist" -> {
                                    val updatedArtist = Artist(
                                        uuid = editUserState.uuid,
                                        username = editUserState.username,
                                        firstName = editUserState.first_name,
                                        lastName = editUserState.last_name,
                                        email = editUserState.email,
                                        profilePhotoURL = downloadUrl.toString(),
                                        rating = founder.value!!.rating,
                                        ratingsNumber = founder.value!!.ratingsNumber,
                                        stageName = editUserState.stage_name
                                    )
                                    viewModelScope.launch(Dispatchers.IO) {
                                        try {
                                            firestore.collection("artist").document(updatedArtist.uuid)
                                                .set(updatedArtist, SetOptions.merge())
                                                .addOnSuccessListener {
                                                    viewModelScope.launch(Dispatchers.Main) {
                                                        navController.navigate(Constants.userProfileNavigation(updatedArtist.uuid))
                                                    }
                                                }
                                            println("Document update successful!")
                                        } catch (e: Exception) {
                                            println("Error updating document: $e")
                                        }
                                    }
                                }

                                "user" -> {
                                    val updatedUser = User(
                                        uuid = editUserState.uuid,
                                        username = editUserState.username,
                                        firstName = editUserState.first_name,
                                        lastName = editUserState.last_name,
                                        email = editUserState.email,
                                        profilePhotoURL = downloadUrl.toString(),
                                    )
                                    viewModelScope.launch(Dispatchers.IO) {
                                        try {
                                            firestore.collection("user").document(updatedUser.uuid)
                                                .set(updatedUser, SetOptions.merge())
                                                .addOnSuccessListener {
                                                    viewModelScope.launch(Dispatchers.Main) {
                                                        navController.navigate(Constants.userProfileNavigation(updatedUser.uuid))
                                                    }
                                                }
                                            println("Document update successful!")
                                        } catch (e: Exception) {
                                            println("Error updating document: $e")
                                        }
                                    }
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

    fun validateEdit() {
        validatePhotoDataWithRules()
        validateUsernameDataWithRules()
        validateFirstNameDataWithRules()
        validateLastNameDataWithRules()
        if (userType.value == "event_founder") {
            validatePhoneDataWithRules()
        }
        if (userType.value == "artist") {
            validateStageNameDataWithRules()
        }
    }

    private fun checkEditErrors(): Boolean {
        return (
                photoUriValidationsPassed.value &&
                        username_validations_passed.value &&
                        first_name_validations_passed.value &&
                        last_name_validations_passed.value &&
                        ((userType.value == "event_founder" && phone_validations_passed.value) ||
                                (userType.value == "artist" && stage_name_validations_passed.value) ||
                                userType.value == "user")
                )
    }


    private fun signUp(navController: NavController) {
        Log.d(TAG, "Inside_signUp")
        printState()
        createUserInFirebase(
            navController = navController,
            registration_ui_state = registration_ui_state.value
        )
    }

    private fun validatePhotoDataWithRules() {
        val result = registration_ui_state.value.photoUri?.let {
            RegisterLoginValidator.validatePhoto(
                uri = Uri.parse(it)
            )
        }
        if (result != null) {
            registration_ui_state.value = registration_ui_state.value.copy(
                photoError = result.status
            )
        }
        if (result != null) {
            photoUriValidationsPassed.value = result.status
        }
    }

    private fun validateFirstNameDataWithRules() {
        val result = RegisterLoginValidator.validateFirstName(
            first_name = registration_ui_state.value.first_name
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            first_name_error = result.status
        )
        first_name_validations_passed.value = result.status
    }

    private fun validateLastNameDataWithRules() {
        val result = RegisterLoginValidator.validateLastName(
            last_name = registration_ui_state.value.last_name
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            last_name_error = result.status
        )
        last_name_validations_passed.value = result.status
    }

    private fun validateUsernameDataWithRules() {
        val result = RegisterLoginValidator.validateUsername(
            username = registration_ui_state.value.username
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            username_error = result.status
        )
        username_validations_passed.value = result.status
    }

    private fun validateEmailDataWithRules() {
        val result = RegisterLoginValidator.validateEmail(
            email = registration_ui_state.value.email
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            email_error = result.status
        )
        email_validations_passed.value = result.status
    }

    private fun validatePasswordDataWithRules() {
        val result = RegisterLoginValidator.validatePassword(
            password = registration_ui_state.value.password
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            password_error = result.status
        )
        password_validations_passed.value = result.status
    }

    private fun validatePhoneDataWithRules() {
        val result = RegisterLoginValidator.validatePhone(
            phone = registration_ui_state.value.phone
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            phone_error = result.status
        )
        phone_validations_passed.value = result.status
    }

    private fun validateStageNameDataWithRules() {
        val result = RegisterLoginValidator.validateStageName(
            stage_name = registration_ui_state.value.stage_name
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            stage_name_error = result.status
        )
        stage_name_validations_passed.value = result.status

    }


    private fun printState() {
        Log.d(TAG, "Inside_printState")
        Log.d(TAG, registration_ui_state.toString())
    }

    private fun createUserInFirebase(
        navController: NavController,
        registration_ui_state: RegistrationUiState
    ) {

        sign_up_in_progress.value = true

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(
                registration_ui_state.email,
                registration_ui_state.password
            )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    user?.uid?.let { uid ->
                        val db = FirebaseFirestore.getInstance()

                        if (registration_ui_state.account_type == "Event Organizer account") {
                            val founder = EventFounder(
                                uuid = uid,
                                username = registration_ui_state.username,
                                firstName = registration_ui_state.first_name,
                                lastName = registration_ui_state.last_name,
                                phone = registration_ui_state.phone,
                                email = registration_ui_state.email,
                                profilePhotoURL = "",
                                rating = 0f,
                                ratingsNumber = 0
                            )

                            db.collection("event_founder")
                                .document(uid)
                                .set(founder)
                                .addOnSuccessListener {
                                    Log.d(TAG, "User added to Firestore successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding user to Firestore", e)
                                }
                        } else if (registration_ui_state.account_type == "Artist account") {
                            val artist = Artist(
                                uuid = uid,
                                username = registration_ui_state.username,
                                email = registration_ui_state.email,
                                firstName = registration_ui_state.first_name,
                                lastName = registration_ui_state.last_name,
                                stageName = registration_ui_state.stage_name,
                                profilePhotoURL = "",
                                rating = 0f,
                                ratingsNumber = 0
                            )
                            db.collection("artist")
                                .document(uid)
                                .set(artist)
                                .addOnSuccessListener {
                                    Log.d(TAG, "User added to Firestore successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding user to Firestore", e)
                                }
                        } else if (registration_ui_state.account_type == "Regular Account") {
                            val user = User(
                                uuid = uid,
                                username = registration_ui_state.username,
                                email = registration_ui_state.email,
                                firstName = registration_ui_state.first_name,
                                lastName = registration_ui_state.last_name,
                                profilePhotoURL = ""
                            )
                            db.collection("user")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener {
                                    Log.d(TAG, "User added to Firestore successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding user to Firestore", e)
                                }
                        }


                        sign_up_in_progress.value = false
                        navController.navigate(Constants.NAVIGATION_HOME_PAGE)
                    }
                }
                Log.d(TAG, "InCompleteListener")
                Log.d(TAG, "isSuccessful  = ${task.isSuccessful}")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "InFailureListener")
                Log.d(TAG, "Exception = ${exception.message}")
                Log.d(TAG, "Exception = ${exception.localizedMessage}")
                sign_up_in_progress.value = false
            }
    }

    private fun logOutUser(navController: NavController) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        val authStateListener = AuthStateListener {
            if (it.currentUser == null) {
                navController.navigate(Constants.NAVIGATION_LOGIN_PAGE)
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)
    }

    //EDIT
    fun setUser(userId: String) {
        val db = FirebaseFirestore.getInstance()

        for (collectionName in usersCollectionNames) {
            val documentReference = db.collection(collectionName).document(userId)
            documentReference.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {

                    when (collectionName) {
                        "artist" -> {
                            val localArtist = snapshot.toObject(Artist::class.java)
                            (artist as MutableLiveData).postValue(localArtist)

                            registration_ui_state.value = registration_ui_state.value.copy(
                                uuid = localArtist!!.uuid,
                                first_name = localArtist.firstName,
                                last_name = localArtist.lastName,
                                username = localArtist.username,
                                email = localArtist.email,
                                stage_name = localArtist.stageName,
                                photoUri = localArtist.profilePhotoURL
                            )
                            (userType as MutableLiveData).postValue(collectionName)
                        }

                        "user" -> {
                            val localUser = snapshot.toObject(User::class.java)
                            (user as MutableLiveData).postValue(localUser)

                            registration_ui_state.value = registration_ui_state.value.copy(
                                uuid = localUser!!.uuid,
                                first_name = localUser.firstName,
                                last_name = localUser.lastName,
                                username = localUser.username,
                                email = localUser.email,
                                photoUri = localUser.profilePhotoURL
                            )
                            (userType as MutableLiveData).postValue(collectionName)
                        }

                        "event_founder" -> {
                            val localFounder = snapshot.toObject(EventFounder::class.java)
                            (founder as MutableLiveData).postValue(localFounder)
                            registration_ui_state.value = registration_ui_state.value.copy(
                                uuid = localFounder!!.uuid,
                                first_name = localFounder.firstName,
                                last_name = localFounder.lastName,
                                username = localFounder.username,
                                email = localFounder.email,
                                phone = localFounder.phone,
                                photoUri = localFounder.profilePhotoURL
                            )
                            (userType as MutableLiveData).postValue(collectionName)
                        }
                    }
                }
                _isUserSet.value = true
            }.addOnFailureListener { exception ->
                // Log or handle the exception
                Log.e("Firestore Error", exception.message.toString())
            }
        }

    }
}
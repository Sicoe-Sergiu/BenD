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
import com.example.bend.model.events.RegistrationUIEvent
import com.example.bend.model.Artist
import com.example.bend.model.EventFounder
import com.example.bend.model.User
import com.example.bend.model.validators.RegisterLoginValidator
import com.example.bend.view.ui_state.RegistrationUiState
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
    var registrationUiState = mutableStateOf(RegistrationUiState())

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var photoUriValidationsPassed = mutableStateOf(false)
    var firstNameValidationsPassed = mutableStateOf(false)
    var lastNameValidationsPassed = mutableStateOf(false)
    var usernameValidationsPassed = mutableStateOf(false)
    var emailValidationsPassed = mutableStateOf(false)
    var passwordValidationsPassed = mutableStateOf(false)
    var phoneValidationsPassed = mutableStateOf(false)
    var stageNameValidationsPassed = mutableStateOf(false)

    var signUpInProgress = mutableStateOf(false)

    lateinit var navController: NavController

    private val usersCollectionNames = listOf("artist", "event_founder", "user")


    var founder: LiveData<EventFounder> = MutableLiveData(null)
    var artist: LiveData<Artist> = MutableLiveData(null)
    var user: LiveData<User> = MutableLiveData(null)
    var userType: LiveData<String> = MutableLiveData(null)

    val errorMessages: LiveData<String> = MutableLiveData()


    private val _isUserSet = MutableStateFlow(false)
    val isUserSet: StateFlow<Boolean> = _isUserSet.asStateFlow()
    fun onEvent(event: RegistrationUIEvent) {

        when (event) {
            is RegistrationUIEvent.FirstNameChanged -> {
                registrationUiState.value =
                    registrationUiState.value.copy(firstName = event.first_name)
                validateFirstNameDataWithRules()
                printState()
            }

            is RegistrationUIEvent.LastNameChanged -> {
                registrationUiState.value =
                    registrationUiState.value.copy(lastName = event.last_name)
                validateLastNameDataWithRules()
                printState()
            }

            is RegistrationUIEvent.UsernameChanged -> {
                registrationUiState.value =
                    registrationUiState.value.copy(username = event.username)
                validateUsernameDataWithRules()
                printState()
            }

            is RegistrationUIEvent.EmailChanged -> {
                registrationUiState.value = registrationUiState.value.copy(email = event.email)
                validateEmailDataWithRules()
                printState()
            }

            is RegistrationUIEvent.PasswordChanged -> {
                registrationUiState.value =
                    registrationUiState.value.copy(password = event.password)
                validatePasswordDataWithRules()
                printState()
            }

            is RegistrationUIEvent.AccountTypeChanged -> {
                registrationUiState.value =
                    registrationUiState.value.copy(accountType = event.account_type)
                printState()
            }

//            conditionals
            is RegistrationUIEvent.PhoneChanged -> {
                registrationUiState.value = registrationUiState.value.copy(phone = event.phone)
                validatePhoneDataWithRules()
                printState()
            }

            is RegistrationUIEvent.StageNameChanged -> {
                registrationUiState.value =
                    registrationUiState.value.copy(stageName = event.stage_name)
                validateStageNameDataWithRules()
                printState()
            }

            is RegistrationUIEvent.ProfilePhotoChanged -> {
                registrationUiState.value =
                    registrationUiState.value.copy(photoUri = event.photoUri.toString())
//                validatePosterDataWithRules()
                printState()
            }

            is RegistrationUIEvent.RegisterButtonClicked -> {
                validateFirstNameDataWithRules(finalCheck = true)
                validateLastNameDataWithRules(finalCheck = true)
                validateUsernameDataWithRules(finalCheck = true)
                validateEmailDataWithRules(finalCheck = true)
                validatePasswordDataWithRules(finalCheck = true)

                if (registrationUiState.value.accountType == "Event Organizer account")
                    validatePhoneDataWithRules(finalCheck = true)

                if (registrationUiState.value.accountType == "Artist account")
                    validateStageNameDataWithRules(finalCheck = true)

                navController = event.navController
                if (firstNameValidationsPassed.value &&
                    lastNameValidationsPassed.value &&
                    usernameValidationsPassed.value &&
                    emailValidationsPassed.value &&
                    passwordValidationsPassed.value &&
                    (
                            (registrationUiState.value.accountType == "Event Organizer account" && phoneValidationsPassed.value) ||
                                    (registrationUiState.value.accountType == "Artist account" && stageNameValidationsPassed.value) ||
                                    registrationUiState.value.accountType == "Regular Account"
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
        val editUserState = registrationUiState.value
        val profilePhotoUUID = editUserState.uuid

        val storageRef: StorageReference =
            storage.reference.child("profile_photos/$profilePhotoUUID")

        if (registrationUiState.value.photoUri.startsWith("http")) {
            when (userType.value) {
                "event_founder" -> {
                    val updatedFounder = EventFounder(
                        uuid = editUserState.uuid,
                        username = editUserState.username,
                        firstName = editUserState.firstName,
                        lastName = editUserState.lastName,
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
                                        navController.navigate(
                                            Constants.userProfileNavigation(
                                                updatedFounder.uuid
                                            )
                                        )
                                    }
                                }
                            println("Document update successful!")
                        } catch (e: Exception) {
                            val errorMessage = e.localizedMessage ?: "Error updating document."
                            Log.e(TAG, errorMessage, e)
                            e.printStackTrace()
                            postError(errorMessage)
                        }
                    }
                }

                "artist" -> {
                    val updatedArtist = Artist(
                        uuid = editUserState.uuid,
                        username = editUserState.username,
                        firstName = editUserState.firstName,
                        lastName = editUserState.lastName,
                        email = editUserState.email,
                        profilePhotoURL = editUserState.photoUri,
                        rating = founder.value!!.rating,
                        ratingsNumber = founder.value!!.ratingsNumber,
                        stageName = editUserState.stageName
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            firestore.collection("artist").document(updatedArtist.uuid)
                                .set(updatedArtist, SetOptions.merge())
                                .addOnSuccessListener {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        navController.navigate(
                                            Constants.userProfileNavigation(
                                                updatedArtist.uuid
                                            )
                                        )
                                    }
                                }
                            println("Document update successful!")
                        } catch (e: Exception) {
                            val errorMessage = e.localizedMessage ?: "Error updating document."
                            Log.e(TAG, errorMessage, e)
                            e.printStackTrace()
                            postError(errorMessage)
                        }
                    }
                }

                "user" -> {
                    val updatedUser = User(
                        uuid = editUserState.uuid,
                        username = editUserState.username,
                        firstName = editUserState.firstName,
                        lastName = editUserState.lastName,
                        email = editUserState.email,
                        profilePhotoURL = editUserState.photoUri,
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            firestore.collection("user").document(updatedUser.uuid)
                                .set(updatedUser, SetOptions.merge())
                                .addOnSuccessListener {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        navController.navigate(
                                            Constants.userProfileNavigation(
                                                updatedUser.uuid
                                            )
                                        )
                                    }
                                }
                            println("Document update successful!")
                        } catch (e: Exception) {
                            val errorMessage = e.localizedMessage ?: "Error updating document."
                            Log.e(TAG, errorMessage, e)
                            e.printStackTrace()
                            postError(errorMessage)
                        }
                    }
                }
            }
        } else {
            registrationUiState.value.photoUri.let { posterUri ->
                storageRef.putFile(Uri.parse(posterUri))
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            when (userType.value) {
                                "event_founder" -> {
                                    val updatedFounder = EventFounder(
                                        uuid = editUserState.uuid,
                                        username = editUserState.username,
                                        firstName = editUserState.firstName,
                                        lastName = editUserState.lastName,
                                        phone = editUserState.phone,
                                        email = editUserState.email,
                                        profilePhotoURL = downloadUrl.toString(),
                                        rating = founder.value!!.rating,
                                        ratingsNumber = founder.value!!.ratingsNumber
                                    )
                                    viewModelScope.launch(Dispatchers.IO) {
                                        try {
                                            firestore.collection("event_founder")
                                                .document(updatedFounder.uuid)
                                                .set(updatedFounder, SetOptions.merge())
                                                .addOnSuccessListener {
                                                    viewModelScope.launch(Dispatchers.Main) {
                                                        navController.navigate(
                                                            Constants.userProfileNavigation(
                                                                updatedFounder.uuid
                                                            )
                                                        )
                                                    }
                                                }
                                            println("Document update successful!")
                                        } catch (e: Exception) {
                                            val errorMessage =
                                                e.localizedMessage ?: "Error updating document."
                                            Log.e(TAG, errorMessage, e)
                                            e.printStackTrace()
                                            postError(errorMessage)
                                        }
                                    }
                                }

                                "artist" -> {
                                    val updatedArtist = Artist(
                                        uuid = editUserState.uuid,
                                        username = editUserState.username,
                                        firstName = editUserState.firstName,
                                        lastName = editUserState.lastName,
                                        email = editUserState.email,
                                        profilePhotoURL = downloadUrl.toString(),
                                        rating = founder.value!!.rating,
                                        ratingsNumber = founder.value!!.ratingsNumber,
                                        stageName = editUserState.stageName
                                    )
                                    viewModelScope.launch(Dispatchers.IO) {
                                        try {
                                            firestore.collection("artist")
                                                .document(updatedArtist.uuid)
                                                .set(updatedArtist, SetOptions.merge())
                                                .addOnSuccessListener {
                                                    viewModelScope.launch(Dispatchers.Main) {
                                                        navController.navigate(
                                                            Constants.userProfileNavigation(
                                                                updatedArtist.uuid
                                                            )
                                                        )
                                                    }
                                                }
                                            println("Document update successful!")
                                        } catch (e: Exception) {
                                            val errorMessage =
                                                e.localizedMessage ?: "Error updating document."
                                            Log.e(TAG, errorMessage, e)
                                            e.printStackTrace()
                                            postError(errorMessage)
                                        }
                                    }
                                }

                                "user" -> {
                                    val updatedUser = User(
                                        uuid = editUserState.uuid,
                                        username = editUserState.username,
                                        firstName = editUserState.firstName,
                                        lastName = editUserState.lastName,
                                        email = editUserState.email,
                                        profilePhotoURL = downloadUrl.toString(),
                                    )
                                    viewModelScope.launch(Dispatchers.IO) {
                                        try {
                                            firestore.collection("user").document(updatedUser.uuid)
                                                .set(updatedUser, SetOptions.merge())
                                                .addOnSuccessListener {
                                                    viewModelScope.launch(Dispatchers.Main) {
                                                        navController.navigate(
                                                            Constants.userProfileNavigation(
                                                                updatedUser.uuid
                                                            )
                                                        )
                                                    }
                                                }
                                            println("Document update successful!")
                                        } catch (e: Exception) {
                                            val errorMessage =
                                                e.localizedMessage ?: "Error updating document."
                                            Log.e(TAG, errorMessage, e)
                                            e.printStackTrace()
                                            postError(errorMessage)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        val errorMessage = exception.localizedMessage ?: "Error uploading image."
                        Log.e(TAG, errorMessage, exception)
                        postError(errorMessage)
                    }
            }
        }
    }


    fun validateEdit() {
        validatePhotoDataWithRules(finalCheck = true)
        validateUsernameDataWithRules(finalCheck = true)
        validateFirstNameDataWithRules(finalCheck = true)
        validateLastNameDataWithRules(finalCheck = true)
        if (userType.value == "event_founder") {
            validatePhoneDataWithRules(finalCheck = true)
        }
        if (userType.value == "artist") {
            validateStageNameDataWithRules(finalCheck = true)
        }
    }

    private fun checkEditErrors(): Boolean {
        return (
                photoUriValidationsPassed.value &&
                        usernameValidationsPassed.value &&
                        firstNameValidationsPassed.value &&
                        lastNameValidationsPassed.value &&
                        ((userType.value == "event_founder" && phoneValidationsPassed.value) ||
                                (userType.value == "artist" && stageNameValidationsPassed.value) ||
                                userType.value == "user")
                )
    }


    private fun signUp(navController: NavController) {
        Log.d(TAG, "Inside_signUp")
        printState()
        createUserInFirebase(
            navController = navController,
            registrationUiState = registrationUiState.value
        )
    }

    private fun validatePhotoDataWithRules(finalCheck: Boolean = false) {
        val result = registrationUiState.value.photoUri?.let {
            RegisterLoginValidator.validatePhoto(
                uri = Uri.parse(it)
            )
        }
        if (result != null) {
            registrationUiState.value = registrationUiState.value.copy(
                photoError = result.status
            )
            photoUriValidationsPassed.value = result.status

            if (!result.status && finalCheck) {
                postError("Failed to create user: ${result.message}")
            }
        } else {
            if (finalCheck)
                postError("Failed to save photo: Photo is not provided.")
        }
    }

    private fun validateFirstNameDataWithRules(finalCheck: Boolean = false) {
        val firstName = registrationUiState.value.firstName
        val result = RegisterLoginValidator.validateFirstName(firstName)

        registrationUiState.value = registrationUiState.value.copy(
            firstNameError = result.status
        )
        firstNameValidationsPassed.value = result.status

        if (!result.status && finalCheck) {
            postError("Failed to validate first name: ${result.message}")
        }
    }

    private fun validateLastNameDataWithRules(finalCheck: Boolean = false) {
        val lastName = registrationUiState.value.lastName
        val result = RegisterLoginValidator.validateLastName(lastName)

        registrationUiState.value = registrationUiState.value.copy(
            lastNameError = result.status
        )
        lastNameValidationsPassed.value = result.status

        if (!result.status && finalCheck) {
            postError("Failed to validate last name: ${result.message}")
        }
    }

    private fun validateUsernameDataWithRules(finalCheck: Boolean = false) {
        val username = registrationUiState.value.username
        val result = RegisterLoginValidator.validateUsername(username)

        registrationUiState.value = registrationUiState.value.copy(
            userNameError = result.status
        )
        usernameValidationsPassed.value = result.status

        if (!result.status && finalCheck) {
            postError("Failed to validate username: ${result.message}")
        }
    }

    private fun validateStageNameDataWithRules(finalCheck: Boolean = false) {
        val stageName = registrationUiState.value.stageName
        val result = RegisterLoginValidator.validateStageName(stageName)

        registrationUiState.value = registrationUiState.value.copy(
            stageNameError = result.status
        )
        stageNameValidationsPassed.value = result.status

        if (!result.status && finalCheck) {
            postError("Failed to validate stage name: ${result.message}")
        }
    }

    private fun validateEmailDataWithRules(finalCheck: Boolean = false) {
        val email = registrationUiState.value.email
        val result = RegisterLoginValidator.validateEmail(email)

        registrationUiState.value = registrationUiState.value.copy(
            emailError = result.status
        )
        emailValidationsPassed.value = result.status

        if (!result.status && finalCheck) {
            postError("Failed to validate email: ${result.message}")
        }
    }

    private fun validatePasswordDataWithRules(finalCheck: Boolean = false) {
        val password = registrationUiState.value.password
        val result = RegisterLoginValidator.validatePassword(password)

        registrationUiState.value = registrationUiState.value.copy(
            passwordError = result.status
        )
        passwordValidationsPassed.value = result.status

        if (!result.status && finalCheck) {
            postError("Failed to validate password: ${result.message}")
        }
    }

    private fun validatePhoneDataWithRules(finalCheck: Boolean = false) {
        val phone = registrationUiState.value.phone
        val result = RegisterLoginValidator.validatePhone(phone)

        registrationUiState.value = registrationUiState.value.copy(
            phoneError = result.status
        )
        phoneValidationsPassed.value = result.status

        if (!result.status && finalCheck) {
            postError("Failed to validate phone: ${result.message}")
        }
    }


    private fun printState() {
        Log.d(TAG, "Inside_printState")
        Log.d(TAG, registrationUiState.toString())
    }

    private fun createUserInFirebase(
        navController: NavController,
        registrationUiState: RegistrationUiState
    ) {
        signUpInProgress.value = true

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(
                registrationUiState.email,
                registrationUiState.password
            )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    user?.uid?.let { uid ->
                        val db = FirebaseFirestore.getInstance()

                        when (registrationUiState.accountType) {
                            "Event Organizer account" -> {
                                val founder = EventFounder(
                                    uuid = uid,
                                    username = registrationUiState.username,
                                    firstName = registrationUiState.firstName,
                                    lastName = registrationUiState.lastName,
                                    phone = registrationUiState.phone,
                                    email = registrationUiState.email,
                                    profilePhotoURL = Constants.DEFAULT_PROFILE_PHOTO_URL,
                                    rating = 0f,
                                    ratingsNumber = 0
                                )

                                db.collection("event_founder")
                                    .document(uid)
                                    .set(founder)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "User added to Firestore successfully")
                                        signUpInProgress.value = false
                                        navController.navigate(Constants.NAVIGATION_SET_PROFILE_PHOTO_PAGE)
                                    }
                                    .addOnFailureListener { e ->
                                        val errorMessage =
                                            e.localizedMessage ?: "Error adding user to Firestore."
                                        Log.e(TAG, errorMessage, e)
                                        postError(errorMessage)
                                        signUpInProgress.value = false
                                    }
                            }

                            "Artist account" -> {
                                val artist = Artist(
                                    uuid = uid,
                                    username = registrationUiState.username,
                                    email = registrationUiState.email,
                                    firstName = registrationUiState.firstName,
                                    lastName = registrationUiState.lastName,
                                    stageName = registrationUiState.stageName,
                                    profilePhotoURL = Constants.DEFAULT_PROFILE_PHOTO_URL,
                                    rating = 0f,
                                    ratingsNumber = 0
                                )
                                db.collection("artist")
                                    .document(uid)
                                    .set(artist)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "User added to Firestore successfully")
                                        signUpInProgress.value = false
                                        navController.navigate(Constants.NAVIGATION_SET_PROFILE_PHOTO_PAGE)
                                    }
                                    .addOnFailureListener { e ->
                                        val errorMessage =
                                            e.localizedMessage ?: "Error adding user to Firestore."
                                        Log.e(TAG, errorMessage, e)
                                        postError(errorMessage)
                                        signUpInProgress.value = false
                                    }
                            }

                            "Regular Account" -> {
                                val user = User(
                                    uuid = uid,
                                    username = registrationUiState.username,
                                    email = registrationUiState.email,
                                    firstName = registrationUiState.firstName,
                                    lastName = registrationUiState.lastName,
                                    profilePhotoURL = Constants.DEFAULT_PROFILE_PHOTO_URL
                                )
                                db.collection("user")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "User added to Firestore successfully")
                                        signUpInProgress.value = false
                                        navController.navigate(Constants.NAVIGATION_SET_PROFILE_PHOTO_PAGE)
                                    }
                                    .addOnFailureListener { e ->
                                        val errorMessage =
                                            e.localizedMessage ?: "Error adding user to Firestore."
                                        Log.e(TAG, errorMessage, e)
                                        postError(errorMessage)
                                        signUpInProgress.value = false
                                    }
                            }

                            else -> {}
                        }
                    }
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Unknown error occurred."
                    Log.e(TAG, errorMessage, task.exception)
                    postError(errorMessage)
                    signUpInProgress.value = false
                }
                Log.d(TAG, "InCompleteListener")
                Log.d(TAG, "isSuccessful  = ${task.isSuccessful}")
            }
            .addOnFailureListener { exception ->
                val errorMessage = exception.localizedMessage ?: "Unknown error occurred."
                Log.e(TAG, errorMessage, exception)
                postError(errorMessage)
                signUpInProgress.value = false
                Log.d(TAG, "InFailureListener")
                Log.d(TAG, "Exception = ${exception.message}")
                Log.d(TAG, "Exception = ${exception.localizedMessage}")
            }
    }


    private fun logOutUser(navController: NavController) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        val authStateListener = AuthStateListener { auth ->
            if (auth.currentUser == null) {
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

                            registrationUiState.value = registrationUiState.value.copy(
                                uuid = localArtist!!.uuid,
                                firstName = localArtist.firstName,
                                lastName = localArtist.lastName,
                                username = localArtist.username,
                                email = localArtist.email,
                                stageName = localArtist.stageName,
                                photoUri = localArtist.profilePhotoURL
                            )
                            (userType as MutableLiveData).postValue(collectionName)
                        }

                        "user" -> {
                            val localUser = snapshot.toObject(User::class.java)
                            (user as MutableLiveData).postValue(localUser)

                            registrationUiState.value = registrationUiState.value.copy(
                                uuid = localUser!!.uuid,
                                firstName = localUser.firstName,
                                lastName = localUser.lastName,
                                username = localUser.username,
                                email = localUser.email,
                                photoUri = localUser.profilePhotoURL
                            )
                            (userType as MutableLiveData).postValue(collectionName)
                        }

                        "event_founder" -> {
                            val localFounder = snapshot.toObject(EventFounder::class.java)
                            (founder as MutableLiveData).postValue(localFounder)
                            registrationUiState.value = registrationUiState.value.copy(
                                uuid = localFounder!!.uuid,
                                firstName = localFounder.firstName,
                                lastName = localFounder.lastName,
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
                Log.e("Firestore Error", exception.message.toString())
                postError(exception.localizedMessage ?: "Error Fetching User details.")
            }
        }

    }

    private fun postError(message: String) {
        (errorMessages as MutableLiveData).postValue(message)
    }

    fun clearError() {
        (errorMessages as MutableLiveData).postValue("")
    }
}
package com.example.bend.view_models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
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

class RegisterViewModel : ViewModel() {
    private val TAG = RegisterViewModel::class.simpleName
    var registration_ui_state = mutableStateOf(RegistrationUiState())

    var first_name_validations_passed = mutableStateOf(false)
    var last_name_validations_passed = mutableStateOf(false)
    var username_validations_passed = mutableStateOf(false)
    var email_validations_passed = mutableStateOf(false)
    var password_validations_passed = mutableStateOf(false)
    var phone_validations_passed = mutableStateOf(false)
    var stage_name_validations_passed = mutableStateOf(false)

    var sign_up_in_progress = mutableStateOf(false)

    lateinit var navController:NavController
    fun onEvent(event: RegistrationUIEvent){

        when(event){
            is RegistrationUIEvent.FirstNameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(first_name = event.first_name)
                validateFirstNameDataWithRules()
                printState()
            }
            is RegistrationUIEvent.LastNameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(last_name = event.last_name)
                validateLastNameDataWithRules()
                printState()
            }
            is RegistrationUIEvent.UsernameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(username = event.username)
                validateUsernameDataWithRules()
                printState()
            }
            is RegistrationUIEvent.EmailChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(email = event.email)
                validateEmailDataWithRules()
                printState()
            }
            is RegistrationUIEvent.PasswordChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(password = event.password)
                validatePasswordDataWithRules()
                printState()
            }
            is RegistrationUIEvent.AccountTypeChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(account_type = event.account_type)
                printState()
            }

//            conditionals
            is RegistrationUIEvent.PhoneChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(phone = event.phone)
                validatePhoneDataWithRules()
                printState()
            }
            is RegistrationUIEvent.StageNameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(stage_name = event.stage_name)
                validateStageNameDataWithRules()
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
                if(first_name_validations_passed.value &&
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

            else -> {}
        }
    }


    private fun signUp(navController: NavController){
        Log.d(TAG,"Inside_signUp")
        printState()
        createUserInFirebase(
            navController = navController,
            registration_ui_state = registration_ui_state.value
        )
    }
    private fun validateFirstNameDataWithRules(){
        val result = RegisterLoginValidator.validateFirstName(
            first_name = registration_ui_state.value.first_name
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            first_name_error = result.status
        )
        first_name_validations_passed.value = result.status
    }

    private fun validateLastNameDataWithRules(){
        val result = RegisterLoginValidator.validateLastName(
            last_name = registration_ui_state.value.last_name
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            last_name_error = result.status
        )
        last_name_validations_passed.value = result.status
    }

    private fun validateUsernameDataWithRules(){
        val result = RegisterLoginValidator.validateUsername(
            username = registration_ui_state.value.username
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            username_error = result.status
        )
        username_validations_passed.value = result.status
    }

    private fun validateEmailDataWithRules(){
        val result = RegisterLoginValidator.validateEmail(
            email = registration_ui_state.value.email
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            email_error = result.status
        )
        email_validations_passed.value = result.status
    }

    private fun validatePasswordDataWithRules(){
        val result = RegisterLoginValidator.validatePassword(
            password = registration_ui_state.value.password
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            password_error = result.status
        )
        password_validations_passed.value = result.status
    }

    private fun validatePhoneDataWithRules(){
        val result = RegisterLoginValidator.validatePhone(
            phone = registration_ui_state.value.phone
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            phone_error = result.status
        )
        phone_validations_passed.value = result.status
    }

    private fun validateStageNameDataWithRules(){
        val result = RegisterLoginValidator.validateStageName(
            stage_name = registration_ui_state.value.stage_name
        )
        registration_ui_state.value = registration_ui_state.value.copy(
            stage_name_error = result.status
        )
        stage_name_validations_passed.value = result.status

    }


    private fun printState(){
        Log.d(TAG,"Inside_printState")
        Log.d(TAG,registration_ui_state.toString())
    }

    private fun createUserInFirebase(navController: NavController, registration_ui_state: RegistrationUiState){

        sign_up_in_progress.value = true

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(registration_ui_state.email, registration_ui_state.password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val user = task.result?.user
                    user?.uid?.let { uid ->
                        val db = FirebaseFirestore.getInstance()

                        if(registration_ui_state.account_type == "Event Organizer account"){
//                            val userMap = hashMapOf(
//                                "email" to registration_ui_state.email,
//                                "username" to registration_ui_state.username,
//                                "first_name" to registration_ui_state.first_name,
//                                "last_name" to registration_ui_state.last_name,
//                                "rating" to 0.0,
//                                "phone" to registration_ui_state.phone,
//                            )
                            val founder = EventFounder(
                                uuid = uid,
                                username = registration_ui_state.username,
                                firstName = registration_ui_state.first_name,
                                lastName = registration_ui_state.last_name,
                                phone = registration_ui_state.phone,
                                email = registration_ui_state.email,
                                rating = 0.0,
                                profilePhotoURL = ""
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
                        }else if(registration_ui_state.account_type == "Artist account"){
//                            val userMap = hashMapOf(
//                                "email" to registration_ui_state.email,
//                                "username" to registration_ui_state.username,
//                                "first_name" to registration_ui_state.first_name,
//                                "last_name" to registration_ui_state.last_name,
//                                "rating" to 0.0,
//                                "stage_name" to registration_ui_state.stage_name,
//                                "genre" to null,
//
//                            )
                            val artist = Artist(
                                uuid = uid,
                                username = registration_ui_state.username,
                                email = registration_ui_state.email,
                                firstName = registration_ui_state.first_name,
                                lastName = registration_ui_state.last_name,
                                stageName = registration_ui_state.stage_name,
                                profilePhotoURL = ""

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
                        }else if(registration_ui_state.account_type == "Regular Account"){
//                            val userMap = hashMapOf(
//                                "email" to registration_ui_state.email,
//                                "username" to registration_ui_state.username,
//                                "first_name" to registration_ui_state.first_name,
//                                "last_name" to registration_ui_state.last_name,
//                                )

                            val user = User(
                                uuid = uid,
                                username = registration_ui_state.username,
                                email = registration_ui_state.email,
                                firstName = registration_ui_state.first_name,
                                lastName = registration_ui_state.last_name,
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
                Log.d(TAG,"InCompleteListener")
                Log.d(TAG,"isSuccessful  = ${task.isSuccessful}")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG,"InFailureListener")
                Log.d(TAG,"Exception = ${exception.message}")
                Log.d(TAG,"Exception = ${exception.localizedMessage}")
                sign_up_in_progress.value = false
            }
    }

    private fun logOutUser(navController: NavController){
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        val authStateListener = AuthStateListener{
            if(it.currentUser == null){
                navController.navigate(Constants.NAVIGATION_LOGIN_PAGE)
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)
    }
}
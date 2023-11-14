package com.example.bend.view_models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.events.RegistrationUIEvent
import com.example.bend.register_login.Validator
import com.example.bend.ui_state.RegistrationUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener

class RegisterViewModel : ViewModel() {
    private val TAG = RegisterViewModel::class.simpleName
    var registration_ui_state = mutableStateOf(RegistrationUiState())
    var all_validations_passed = mutableStateOf(false)

    var sign_up_in_progress = mutableStateOf(false)

    lateinit var navController:NavController
    fun onEvent(event: RegistrationUIEvent){
        validateDataWithRules()
        when(event){
            is RegistrationUIEvent.FirstNameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(first_name = event.first_name)
                printState()
            }
            is RegistrationUIEvent.LastNameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(last_name = event.last_name)
                printState()
            }
            is RegistrationUIEvent.UsernameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(username = event.username)
                printState()
            }
            is RegistrationUIEvent.EmailChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(email = event.email)
                printState()
            }
            is RegistrationUIEvent.PasswordChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(password = event.password)
                printState()
            }
            is RegistrationUIEvent.AccountTypeChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(account_type = event.account_type)
                printState()
            }

//            conditionals
            is RegistrationUIEvent.PhoneChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(phone = event.phone)
                printState()
            }
            is RegistrationUIEvent.StageNameChanged -> {
                registration_ui_state.value = registration_ui_state.value.copy(stage_name = event.stage_name)
                printState()
            }

            is RegistrationUIEvent.RegisterButtonClicked -> {
                navController = event.navController
                signUp(navController)
            }

            is RegistrationUIEvent.LogOutButtonClicked -> {
                navController = event.navController
                logOutUser(navController)
            }
        }
    }

    private fun signUp(navController: NavController){
        Log.d(TAG,"Inside_signUp")
        printState()
        createUserInFirebase(
            navController = navController,
            email = registration_ui_state.value.email,
            password = registration_ui_state.value.password
        )
    }

    private fun validateDataWithRules() {
        val first_name_result = Validator.validateFirstName(
            first_name = registration_ui_state.value.first_name
        )
        val last_name_result = Validator.validateLastName(
            last_name = registration_ui_state.value.last_name
        )
        val username_result = Validator.validateUsername(
            username = registration_ui_state.value.username
        )
        val email_result = Validator.validateEmail(
            email = registration_ui_state.value.email
        )
        val password_result = Validator.validatePassword(
            password = registration_ui_state.value.password
        )
        val phone_result = Validator.validatePhone(
            phone = registration_ui_state.value.phone
        )
        val stage_name_result = Validator.validateStageName(
            stage_name = registration_ui_state.value.stage_name
        )

        registration_ui_state.value = registration_ui_state.value.copy(
            first_name_error = first_name_result.status,
            last_name_error = last_name_result.status,
            username_error = username_result.status,
            email_error = email_result.status,
            password_error = password_result.status,
            phone_error = phone_result.status,
            stage_name_error = stage_name_result.status
        )

        if(first_name_result.status && last_name_result.status && username_result.status && email_result.status && password_result.status){
            if(registration_ui_state.value.account_type == "Event Organizer account" && phone_result.status){
                all_validations_passed.value = true
            }else if (registration_ui_state.value.account_type == "Artist account" && stage_name_result.status){
                all_validations_passed.value = true
            }else all_validations_passed.value = registration_ui_state.value.account_type == "Regular Account"
        }

    }

    private fun printState(){
        Log.d(TAG,"Inside_printState")
        Log.d(TAG,registration_ui_state.toString())
    }

    private fun createUserInFirebase(navController: NavController,email:String, password:String){

        sign_up_in_progress.value = true

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(){
                if(it.isSuccessful){
                    sign_up_in_progress.value = false
                    navController.navigate(Constants.NAVIGATION_HOME_PAGE)
                }
                Log.d(TAG,"InCompleteListener")
                Log.d(TAG,"isSuccesfull  = ${it.isSuccessful}")

            }
            .addOnFailureListener(){
                Log.d(TAG,"InFailureListener")
                Log.d(TAG,"Exception = ${it.message}")
                Log.d(TAG,"Exception = ${it.localizedMessage}")
            }
    }
    fun logOutUser(navController: NavController){
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
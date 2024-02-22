package com.example.bend.view_models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.events.LoginUIEvent
import com.example.bend.events.RegistrationUIEvent
import com.example.bend.register_login.Validator
import com.example.bend.ui_state.LoginUiState
import com.example.bend.ui_state.RegistrationUiState
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel: ViewModel() {
    private val TAG = LoginViewModel::class.simpleName
    var login_ui_state = mutableStateOf(LoginUiState())

    var email_validation_passed = mutableStateOf(false)
    var password_validations_passed = mutableStateOf(false)

    var sign_in_in_progress = mutableStateOf(false)

    lateinit var navController: NavController

    fun onEvent(event: LoginUIEvent) {
        when (event) {
            is LoginUIEvent.EmailChanged -> {
                login_ui_state.value = login_ui_state.value.copy(email = event.email)
                validateEmailDataWithRules()
                printState()
            }

            is LoginUIEvent.PasswordChanged -> {
                login_ui_state.value = login_ui_state.value.copy(password = event.password)
                validatePassDataWithRules()
                printState()
            }

            is LoginUIEvent.LoginButtonClicked -> {
                validateEmailDataWithRules()
                validatePassDataWithRules()
                navController = event.navController
                if(password_validations_passed.value && email_validation_passed.value)
                    signIn(navController)
            }

            else -> {}
        }
    }
    private fun validateEmailDataWithRules(){
        val email_result = Validator.validateEmail(
            email = login_ui_state.value.email
        )
        login_ui_state.value = login_ui_state.value.copy(
            email_error = email_result.status,
        )
        email_validation_passed.value = email_result.status
    }
    private fun validatePassDataWithRules() {

        val password_result = Validator.validatePassword(
            password = login_ui_state.value.password
        )
        login_ui_state.value = login_ui_state.value.copy(
            password_error = password_result.status,
        )
        password_validations_passed.value = password_result.status
    }
    private fun printState() {
        Log.d(TAG,"Inside_printState")
        Log.d(TAG,login_ui_state.toString())
    }

    private fun signIn(navController: NavController) {

        val email = login_ui_state.value.email
        val pass = login_ui_state.value.password

        FirebaseAuth
            .getInstance()
            .signInWithEmailAndPassword(email,pass)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    sign_in_in_progress.value = false
                    navController.navigate(Constants.NAVIGATION_HOME_PAGE)
                }
                Log.d(TAG,"InCompleteListener")
                Log.d(TAG,"isSuccesfull  = ${it.isSuccessful}")
            }
            .addOnFailureListener{
                Log.d(TAG,"InFailureListener")
                Log.d(TAG,"Exception = ${it.message}")
                Log.d(TAG,"Exception = ${it.localizedMessage}")
                sign_in_in_progress.value = false
            }
    }
}


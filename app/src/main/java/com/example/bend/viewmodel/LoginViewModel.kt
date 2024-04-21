package com.example.bend.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.bend.Constants
import com.example.bend.model.events.LoginUIEvent
import com.example.bend.model.validators.RegisterLoginValidator
import com.example.bend.view.ui_state.LoginUiState
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {
    private val TAG = LoginViewModel::class.simpleName
    var loginUiState = mutableStateOf(LoginUiState())
    var emailValidationPassed = mutableStateOf(false)
    var passwordValidationsPassed = mutableStateOf(false)
    var signInInProgress = mutableStateOf(false)
    lateinit var navController: NavController
    val errorMessages: LiveData<String> = MutableLiveData()


    fun onEvent(event: LoginUIEvent) {
        when (event) {
            is LoginUIEvent.EmailChanged -> {
                loginUiState.value = loginUiState.value.copy(email = event.email)
                validateEmailDataWithRules()
                printState()
            }

            is LoginUIEvent.PasswordChanged -> {
                loginUiState.value = loginUiState.value.copy(password = event.password)
                validatePassDataWithRules()
                printState()
            }

            is LoginUIEvent.LoginButtonClicked -> {
                validateEmailDataWithRules(finalCheck = true)
                validatePassDataWithRules(finalCheck = true)
                navController = event.navController
                if (passwordValidationsPassed.value && emailValidationPassed.value)
                    signIn(navController)
            }
        }
    }

    private fun validateEmailDataWithRules(finalCheck: Boolean = false) {
        val email = loginUiState.value.email
        val result = RegisterLoginValidator.validateEmail(email)

        loginUiState.value = loginUiState.value.copy(
            emailError = result.status
        )
        emailValidationPassed.value = result.status

        if (!result.status && finalCheck) {
            postError("Failed to validate email: ${result.message}")
        }
    }

    private fun validatePassDataWithRules(finalCheck: Boolean = false) {
        val password = loginUiState.value.password
        val result = RegisterLoginValidator.validatePassword(password)

        loginUiState.value = loginUiState.value.copy(
            passwordError = result.status
        )
        passwordValidationsPassed.value = result.status

        if (!result.status && finalCheck) {
            postError("Failed to validate password: ${result.message}")
        }
    }

    private fun printState() {
        Log.d(TAG, "Inside_printState")
        Log.d(TAG, loginUiState.toString())
    }

    private fun signIn(navController: NavController) {

        val email = loginUiState.value.email
        val pass = loginUiState.value.password
        signInInProgress.value = true
        FirebaseAuth
            .getInstance()
            .signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    signInInProgress.value = false
                    navController.navigate(Constants.NAVIGATION_HOME_PAGE)
                }
                Log.d(TAG, "InCompleteListener")
                Log.d(TAG, "isSuccessful  = ${it.isSuccessful}")
            }
            .addOnFailureListener {
                Log.d(TAG, "InFailureListener")
                Log.d(TAG, "Exception = ${it.message}")
                Log.d(TAG, "Exception = ${it.localizedMessage}")
                it.localizedMessage?.let { it1 -> postError(it1) }
                signInInProgress.value = false
            }
    }

    private fun postError(message: String) {
        (errorMessages as MutableLiveData).postValue(message)
    }

    fun clearError() {
        (errorMessages as MutableLiveData).postValue("")
    }
}


package com.example.bend.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.bend.model.events.ForgotPassUIEvent
import com.example.bend.model.validators.RegisterLoginValidator
import com.example.bend.view.ui_state.ForgotPasswordUiState
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordViewModel: ViewModel() {

    private val TAG = ForgotPasswordViewModel::class.simpleName
    var forgotPassUiState = mutableStateOf(ForgotPasswordUiState())
    var emailValidationPassed = mutableStateOf(false)
    var forgotPassInProgress = mutableStateOf(false)
    lateinit var navController: NavController
    val errorMessages: LiveData<String> = MutableLiveData()


    fun onEvent(event: ForgotPassUIEvent) {
        when (event) {
            is ForgotPassUIEvent.EmailChanged -> {
                forgotPassUiState.value = forgotPassUiState.value.copy(email = event.email)
                validateEmailDataWithRules()
                printState()
            }

            is ForgotPassUIEvent.ResetButtonClicked -> {
                validateEmailDataWithRules(finalCheck = true)
                navController = event.navController
                if(emailValidationPassed.value)
                    resetPass(navController)
            }
        }
    }
    private fun postError(message: String) {
        (errorMessages as MutableLiveData).postValue(message)
    }

    fun clearError() {
        (errorMessages as MutableLiveData).postValue("")
    }
    private fun validateEmailDataWithRules(finalCheck: Boolean = false) {
        val email = forgotPassUiState.value.email
        val result = RegisterLoginValidator.validateEmail(email)

        forgotPassUiState.value = forgotPassUiState.value.copy(
            emailError = result.status
        )
        emailValidationPassed.value = result.status

        if (!result.status && finalCheck) {
            postError("Failed to validate email: ${result.message}")
        }
    }

    private fun printState() {
        Log.d(TAG,"Inside_printState")
        Log.d(TAG,forgotPassUiState.toString())
    }

    private fun resetPass(navController: NavController) {
        val email = forgotPassUiState.value.email

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    forgotPassInProgress.value = false
                    navController.popBackStack()
                }
                Log.d(TAG, "InCompleteListener")
                Log.d(TAG, "isSuccessful  = ${task.isSuccessful}")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "InFailureListener")
                Log.d(TAG, "Exception = ${e.message}")
                Log.d(TAG, "Exception = ${e.localizedMessage}")
                postError(e.localizedMessage ?: "Unknown error occurred")
                forgotPassInProgress.value = false
            }
    }
}

